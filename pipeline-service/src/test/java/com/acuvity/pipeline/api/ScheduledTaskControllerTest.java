package com.acuvity.pipeline.api;

import static org.mockito.Mockito.when;

import com.acuvity.pipeline.models.NamespaceSyncResult;
import com.acuvity.pipeline.models.SyncSummaryResponse;
import com.acuvity.pipeline.service.sync.SyncFanOutService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(ScheduledTaskController.class)
@Import(ApiControllerTestConfiguration.class)
class ScheduledTaskControllerTest {

    @Autowired WebTestClient webClient;
    @Autowired SyncFanOutService fanOutService;

    @Test
    void returnsAggregateSummary() {
        Instant from = Instant.parse("2025-09-30T13:00:00Z");
        Instant to = Instant.parse("2025-09-30T14:00:00Z");
        when(fanOutService.syncAll(from, to)).thenReturn(Mono.just(new SyncSummaryResponse(
                "published", "logs.synced.v1", 1, 2,
                List.of(new NamespaceSyncResult("/org/test", 2)))));

        webClient.post().uri(uriBuilder -> uriBuilder.path("/v1/scheduler/sync-logs")
                        .queryParam("from", from)
                        .queryParam("to", to).build())
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.namespacesProcessed").isEqualTo(1)
                .jsonPath("$.recordsPublished").isEqualTo(2);
    }
}
