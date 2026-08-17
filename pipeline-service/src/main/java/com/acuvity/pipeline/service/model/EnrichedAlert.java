package com.acuvity.pipeline.service.model;

import com.acuvity.alert.models.Alert;
import com.acuvity.log.models.LogRecord;
import java.util.List;

public record EnrichedAlert(Alert alert, List<LogRecord> correlatedLogs) {
}
