package com.acuvity.pipeline.api;

import com.acuvity.pipeline.service.PipelineService;
import com.acuvity.pipeline.service.sync.SyncFanOutService;
import com.acuvity.pipeline.serviceconfig.PipelineProperties;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
class ApiControllerTestConfiguration {

    @Bean
    PipelineService pipelineService() {
        return org.mockito.Mockito.mock(PipelineService.class);
    }

    @Bean
    SyncFanOutService syncFanOutService() {
        return org.mockito.Mockito.mock(SyncFanOutService.class);
    }

    @Bean
    @Primary
    PipelineProperties pipelineProperties() {
        return new PipelineProperties(
                new PipelineProperties.LogService(
                        URI.create("http://logs"), Duration.ofSeconds(30),
                        Duration.ofSeconds(2), Duration.ofSeconds(10)),
                new PipelineProperties.Topics("alerts.enriched.v1", "logs.synced.v1"),
                URI.create("http://pipeline"), List.of("/org/test"), Duration.ofSeconds(10));
    }
}
