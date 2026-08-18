package com.acuvity.pipeline.service.redpanda;

import com.acuvity.pipeline.serviceconfig.PipelineProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PipelinePublisher {

    private static final Logger LOGGER = LogManager.getLogger(PipelinePublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PipelineProperties properties;

    public PipelinePublisher(KafkaTemplate<String, Object> kafkaTemplate, PipelineProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    public Mono<Void> publish(String topic, String namespace, Object payload) {
        return Mono.defer(() -> Mono.fromFuture(kafkaTemplate.send(topic, namespace, payload)))
                .timeout(properties.publishTimeout())
                .doOnNext(result -> LOGGER.trace(
                        "Published Kafka record topic={} key={} partition={} offset={}",
                        topic, namespace, result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()))
                .doOnError(exception -> LOGGER.error(
                        "Failed to publish Kafka record topic={} key={}",
                        topic, namespace, exception))
                .onErrorMap(exception -> new PublishException("Failed to publish to " + topic, exception))
                .then();
    }
}
