package com.acuvity.pipeline.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.acuvity.alert.models.Alert;
import com.acuvity.alert.models.AlertDefinition;
import com.acuvity.alert.models.AlertEvent;
import com.acuvity.alert.models.Principal;
import com.acuvity.alert.models.User;
import com.acuvity.log.models.LogRecord;
import com.acuvity.log.models.LogRecords;
import com.acuvity.pipeline.service.log.LogServiceApi;
import com.acuvity.pipeline.service.namespace.NamespaceRegistry;
import com.acuvity.pipeline.service.redpanda.PipelinePublisher;
import com.acuvity.pipeline.serviceconfig.PipelineProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers(disabledWithoutDocker = true)
class PipelineServiceTest {

    @Container
    private static final RedpandaContainer REDPANDA = new RedpandaContainer(
            "docker.redpanda.com/redpandadata/redpanda:v24.2.18");

    private static DefaultKafkaProducerFactory<String, Object> producerFactory;
    private static PipelinePublisher publisher;

    private final LogServiceApi client = org.mockito.Mockito.mock(LogServiceApi.class);
    private final PipelineProperties properties = properties();
    private PipelineService service;

    @BeforeAll
    static void configurePublisher() {
        producerFactory = new DefaultKafkaProducerFactory<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, REDPANDA.getBootstrapServers(),
                ProducerConfig.ACKS_CONFIG, "all",
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class));
        publisher = new PipelinePublisher(new KafkaTemplate<>(producerFactory), properties());
    }

    @AfterAll
    static void closeProducer() {
        producerFactory.destroy();
    }

    @BeforeEach
    void configureService() {
        service = new PipelineService(client, publisher, properties, new NamespaceRegistry(properties),
                new SimpleMeterRegistry());
    }

    @Test
    void correlatesAndPublishesAnAlert() {
        Instant start = Instant.parse("2025-09-30T13:47:29.444Z");
        Instant end = Instant.parse("2025-09-30T13:49:15.942Z");
        var principal = new Principal("24.5.75.234", "UserToken", List.of("Sales"), "User",
                new User("john@example.com"));
        var event = new AlertEvent("secrets", "/org/test", principal, "chatgpt", end);
        var definition = new AlertDefinition("definition-1", "5m", start, "description",
                "Secrets", "message", "secrets", "/org/test", true, "Critical", start);
        var alert = new Alert("id-1", definition, "secrets", List.of(event), 1, start,
                "/org/test", start, end, end);
        var logPrincipal = new com.acuvity.log.models.Principal(
                "24.5.75.234", "UserToken", List.of("Sales"), "User",
                new com.acuvity.log.models.User("john@example.com"));
        var log = new LogRecord(List.of(), "Deny", List.of(), logPrincipal, "chatgpt", "LLM",
                List.of(), null, Instant.parse("2025-09-30T13:48:00Z"), "Input");
        when(client.findLogs("/org/test", "chatgpt", "john@example.com", "secrets",
                start.minusSeconds(30), end.plusSeconds(30)))
                .thenReturn(Mono.just(new LogRecords(List.of(log))));

        StepVerifier.create(service.admit(alert)).expectNext(1).verifyComplete();

        var record = consume("alerts.enriched.v1");
        assertThat(record.key()).isEqualTo("/org/test");
        assertThat(record.value())
                .contains("\"alert\"")
                .contains("\"correlatedLogs\"")
                .contains("\"provider\":\"chatgpt\"");
    }

    @Test
    void syncPublishesEveryLogUsingNamespaceAsKey() {
        Instant from = Instant.parse("2025-09-30T13:00:00Z");
        Instant to = Instant.parse("2025-09-30T14:00:00Z");
        var log = new LogRecord(List.of(), "Allow", List.of(), null, "chatgpt", "LLM",
                List.of(), null, from, "Input");
        when(client.findLogs("/org/test", null, null, null, from, to))
                .thenReturn(Mono.just(new LogRecords(List.of(log))));

        StepVerifier.create(service.syncLogs("/org/test", from, to)).expectNext(1).verifyComplete();

        var record = consume("logs.synced.v1");
        assertThat(record.key()).isEqualTo("/org/test");
        assertThat(record.value())
                .contains("\"action\":\"Allow\"")
                .contains("\"provider\":\"chatgpt\"");
    }

    private org.apache.kafka.clients.consumer.ConsumerRecord<String, String> consume(String topic) {
        var consumerProperties = Map.<String, Object>of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, REDPANDA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        try (var consumer = new KafkaConsumer<String, String>(consumerProperties)) {
            consumer.subscribe(List.of(topic));
            Instant deadline = Instant.now().plusSeconds(10);
            while (Instant.now().isBefore(deadline)) {
                var records = consumer.poll(Duration.ofMillis(250));
                if (!records.isEmpty()) {
                    return records.iterator().next();
                }
            }
        }
        throw new AssertionError("No record received from topic " + topic);
    }

    private static PipelineProperties properties() {
        return new PipelineProperties(
                new PipelineProperties.LogService(URI.create("http://logs"), Duration.ofSeconds(30),
                        Duration.ofSeconds(2), Duration.ofSeconds(10)),
                new PipelineProperties.Topics("alerts.enriched.v1", "logs.synced.v1"),
                URI.create("http://pipeline"), List.of("/org/test"), Duration.ofSeconds(10));
    }
}
