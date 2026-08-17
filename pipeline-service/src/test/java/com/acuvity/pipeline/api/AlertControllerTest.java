package com.acuvity.pipeline.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.acuvity.pipeline.service.PipelineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(AlertController.class)
@Import(ApiControllerTestConfiguration.class)
class AlertControllerTest {

    @Autowired WebTestClient webClient;
    @Autowired PipelineService service;

    @Test
    void acceptsValidAlert() {
        when(service.admit(any())).thenReturn(Mono.just(1));

        webClient.post().uri("/v1/alerts").contentType(MediaType.APPLICATION_JSON).bodyValue("""
                {
                  "ID":"68dbdf7181fbcb00012b3ec2",
                  "alertDefinition":{
                    "ID":"672e84970cee63000115ab5a",
                    "cooldown":"5m",
                    "createTime":"2024-11-08T21:37:27.641Z",
                    "description":"Alerting configuration for secrets.",
                    "friendlyName":"Secrets",
                    "message":"Some secrets have been detected in the input data.",
                    "name":"secrets",
                    "namespace":"/org/test",
                    "propagate":true,
                    "severity":"Critical",
                    "updateTime":"2024-11-08T21:37:27.641Z"
                  },
                  "alertDefinitionName":"secrets",
                  "alertEvents":[{
                    "alertDefinition":"secrets",
                    "alertDefinitionNamespace":"/org/test",
                    "principal":{"user":{"name":"john@example.com"}},
                    "provider":"chatgpt",
                    "timestamp":"2025-09-30T13:49:15.942Z"
                  }],
                  "counter":1,
                  "createTime":"2025-09-30T13:47:29.471Z",
                  "namespace":"/org/test",
                  "start":"2025-09-30T13:47:29.444Z",
                  "end":"2025-09-30T13:49:15.942Z",
                  "updateTime":"2025-09-30T13:49:15.978Z"
                }
                """)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.topic").isEqualTo("alerts.enriched.v1")
                .jsonPath("$.recordsPublished").isEqualTo(1);
    }
}
