package com.acuvity.alert.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record AlertDefinition(
        @JsonProperty("ID") String id,
        String cooldown,
        Instant createTime,
        String description,
        String friendlyName,
        String message,
        String name,
        String namespace,
        boolean propagate,
        String severity,
        Instant updateTime) {
}
