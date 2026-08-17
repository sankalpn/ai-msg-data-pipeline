package com.acuvity.pipeline.api;

import com.acuvity.alert.models.Alert;
import com.acuvity.pipeline.models.AcceptedResponse;
import com.acuvity.pipeline.service.PipelineService;
import com.acuvity.pipeline.serviceconfig.PipelineProperties;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/alerts")
public class AlertController {

    private final PipelineService service;
    private final PipelineProperties properties;

    public AlertController(PipelineService service, PipelineProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @PostMapping
    public Mono<ResponseEntity<AcceptedResponse>> admit(@Valid @RequestBody Alert alert) {
        return service.admit(alert)
                .map(ignored -> ResponseEntity.accepted().body(AcceptedResponse.published(
                        properties.topics().enrichedAlerts(), 1)));
    }
}
