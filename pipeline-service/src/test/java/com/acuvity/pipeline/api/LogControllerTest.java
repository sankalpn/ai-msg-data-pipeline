package com.acuvity.pipeline.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acuvity.pipeline.service.PipelineService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(LogController.class)
@Import(ApiControllerTestConfiguration.class)
class LogControllerTest {

    @Autowired WebTestClient webClient;
    @Autowired PipelineService service;

    @Test
    void rejectsAnInvalidRange() {
        webClient.post().uri(uriBuilder -> uriBuilder.path("/v1/sync-logs")
                        .queryParam("namespace", "/org/test")
                        .queryParam("from", "2025-09-30T14:00:00Z")
                        .queryParam("to", "2025-09-30T13:00:00Z").build())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void returnsPublishedCount() {
        Instant from = Instant.parse("2025-09-30T13:00:00Z");
        Instant to = Instant.parse("2025-09-30T14:00:00Z");
        when(service.syncLogs("/org/test", from, to)).thenReturn(Mono.just(2));

        webClient.post().uri(uriBuilder -> uriBuilder.path("/v1/sync-logs")
                        .queryParam("namespace", "/org/test")
                        .queryParam("from", from)
                        .queryParam("to", to).build())
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.recordsPublished").isEqualTo(2);
        verify(service).syncLogs("/org/test", from, to);
    }
}
