package com.acuvity.pipeline.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acuvity.pipeline.service.namespace.NamespaceRegistry;
import com.acuvity.pipeline.serviceconfig.PipelineProperties;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class NamespaceRegistryTest {

    @Test
    void rejectsDisabledNamespace() {
        var properties = new PipelineProperties(
                new PipelineProperties.LogService(URI.create("http://logs"), Duration.ofSeconds(30),
                        Duration.ofSeconds(2), Duration.ofSeconds(10)),
                new PipelineProperties.Topics("alerts.enriched.v1", "logs.synced.v1"),
                URI.create("http://pipeline"), List.of("/org/test"), Duration.ofSeconds(10));
        var registry = new NamespaceRegistry(properties);

        assertThatThrownBy(() -> registry.requireEnabled("/org/disabled"))
                .isInstanceOf(com.acuvity.pipeline.api.NamespaceNotEnabledException.class);
    }
}
