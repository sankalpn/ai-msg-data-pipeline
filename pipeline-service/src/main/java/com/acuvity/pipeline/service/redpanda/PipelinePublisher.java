package com.acuvity.pipeline.service.redpanda;

import com.acuvity.pipeline.serviceconfig.PipelineProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PipelinePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PipelineProperties properties;

    public PipelinePublisher(KafkaTemplate<String, Object> kafkaTemplate, PipelineProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    public Mono<Void> publish(String topic, String namespace, Object payload) {
        return Mono.fromFuture(kafkaTemplate.send(topic, namespace, payload))
                .timeout(properties.publishTimeout())
                .onErrorMap(exception -> new PublishException("Failed to publish to " + topic, exception))
                .then();
    }
}
