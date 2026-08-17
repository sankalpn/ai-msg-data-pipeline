package com.acuvity.log.models;

import java.util.List;

public record LogRecords(List<LogRecord> logs) {

    public LogRecords {
        logs = logs == null ? List.of() : List.copyOf(logs);
    }
}
