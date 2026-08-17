package com.acuvity.pipeline.service.sync;

import com.acuvity.pipeline.models.AcceptedResponse;
import java.time.Instant;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange(accept = "application/json")
public interface PipelineSyncApi {

    @PostExchange("/v1/sync-logs")
    Mono<AcceptedResponse> syncLogs(
            @RequestParam String namespace,
            @RequestParam Instant from,
            @RequestParam Instant to);
}
