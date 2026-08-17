package com.acuvity.log.models;

import java.time.Instant;
import java.util.List;

public record LogRecord(
        List<LogAlert> alerts,
        String decision,
        List<Extraction> extractions,
        Principal principal,
        String provider,
        String providerType,
        List<String> reasons,
        LogSummary summary,
        Instant time,
        String type) {
}
