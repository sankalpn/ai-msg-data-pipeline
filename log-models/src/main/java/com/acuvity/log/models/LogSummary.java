package com.acuvity.log.models;

import java.util.Map;

public record LogSummary(
        Map<String, Count> detections,
        Map<String, Count> modalities) {
}
