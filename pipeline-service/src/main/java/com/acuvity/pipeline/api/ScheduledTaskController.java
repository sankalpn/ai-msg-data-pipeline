package com.acuvity.pipeline.api;

import com.acuvity.pipeline.service.sync.SyncFanOutService;
import com.acuvity.pipeline.models.SyncSummaryResponse;
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
@RequestMapping("/v1/scheduler")
public class ScheduledTaskController {

    private final SyncFanOutService fanOutService;

    public ScheduledTaskController(SyncFanOutService fanOutService) {
        this.fanOutService = fanOutService;
    }

    @PostMapping("/sync-logs")
    public Mono<ResponseEntity<SyncSummaryResponse>> syncAllLogs(
            @RequestParam @NotNull Instant from,
            @RequestParam @NotNull Instant to) {
        TimeRangeValidator.validate(from, to);
        return fanOutService.syncAll(from, to)
                .map(summary -> ResponseEntity.accepted().body(summary));
    }
}
