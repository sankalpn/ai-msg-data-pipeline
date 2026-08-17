package com.acuvity.pipeline.api;

import com.acuvity.pipeline.service.PipelineService;
import com.acuvity.pipeline.models.AcceptedResponse;
import com.acuvity.pipeline.serviceconfig.PipelineProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping("/v1/sync-logs")
public class LogController {

    private final PipelineService service;
    private final PipelineProperties properties;

    public LogController(PipelineService service, PipelineProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @PostMapping
    public Mono<ResponseEntity<AcceptedResponse>> syncLogs(
            @RequestParam @NotBlank String namespace,
            @RequestParam @NotNull Instant from,
            @RequestParam @NotNull Instant to) {
        TimeRangeValidator.validate(from, to);
        return service.syncLogs(namespace, from, to)
                .map(count -> ResponseEntity.accepted().body(AcceptedResponse.published(
                        properties.topics().syncedLogs(), count)));
    }
}
