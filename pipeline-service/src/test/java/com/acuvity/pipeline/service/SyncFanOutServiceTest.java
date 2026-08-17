package com.acuvity.pipeline.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.acuvity.pipeline.models.AcceptedResponse;
import com.acuvity.pipeline.service.namespace.NamespaceRegistry;
import com.acuvity.pipeline.service.sync.PipelineSyncApi;
import com.acuvity.pipeline.service.sync.SyncFanOutService;
import com.acuvity.pipeline.serviceconfig.PipelineProperties;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SyncFanOutServiceTest {

    @Test
    void fansOutAndAggregatesNamespaceResults() {
        var api = org.mockito.Mockito.mock(PipelineSyncApi.class);
        var properties = properties();
        var service = new SyncFanOutService(api, properties, new NamespaceRegistry(properties));
        Instant from = Instant.parse("2025-09-30T13:00:00Z");
        Instant to = Instant.parse("2025-09-30T14:00:00Z");
        when(api.syncLogs("/org/a", from, to))
                .thenReturn(Mono.just(AcceptedResponse.published("logs.synced.v1", 2)));
        when(api.syncLogs("/org/b", from, to))
                .thenReturn(Mono.just(AcceptedResponse.published("logs.synced.v1", 3)));

        StepVerifier.create(service.syncAll(from, to))
                .assertNext(result -> {
                    assertThat(result.namespacesProcessed()).isEqualTo(2);
                    assertThat(result.recordsPublished()).isEqualTo(5);
                    assertThat(result.namespaces()).extracting("namespace")
                            .containsExactlyInAnyOrder("/org/a", "/org/b");
                })
                .verifyComplete();
    }

    private PipelineProperties properties() {
        return new PipelineProperties(
                new PipelineProperties.LogService(URI.create("http://logs"), Duration.ofSeconds(30),
                        Duration.ofSeconds(2), Duration.ofSeconds(10)),
                new PipelineProperties.Topics("alerts.enriched.v1", "logs.synced.v1"),
                URI.create("http://pipeline"), List.of("/org/a", "/org/b"), Duration.ofSeconds(10));
    }
}
