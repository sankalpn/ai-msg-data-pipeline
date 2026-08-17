package com.acuvity.pipeline.service.log;

import com.acuvity.log.models.LogRecords;
import java.time.Instant;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@HttpExchange(accept = "application/json")
public interface LogServiceApi {

    @GetExchange("/logs")
    Mono<LogRecords> findLogs(
            @RequestParam String namespace,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String alertDefinition,
            @RequestParam Instant from,
            @RequestParam Instant to);
}
