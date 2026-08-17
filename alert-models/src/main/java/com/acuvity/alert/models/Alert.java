package com.acuvity.alert.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;
import java.util.List;

public record Alert(
        @JsonProperty("ID") @NotBlank String id,
        @Valid @NotNull AlertDefinition alertDefinition,
        @NotBlank String alertDefinitionName,
        @NotEmpty List<@Valid AlertEvent> alertEvents,
        @PositiveOrZero int counter,
        @NotNull Instant createTime,
        @NotBlank String namespace,
        @NotNull Instant start,
        @NotNull Instant end,
        @NotNull Instant updateTime) {

    @AssertTrue(message = "end must not be before start")
    @JsonIgnore
    public boolean isTimeRangeValid() {
        return start == null || end == null || !end.isBefore(start);
    }

    @AssertTrue(message = "all alert events must belong to the alert namespace and time range")
    @JsonIgnore
    public boolean areEventsInScope() {
        if (namespace == null || start == null || end == null || alertEvents == null) {
            return true;
        }
        return alertEvents.stream().allMatch(event -> event != null
                && namespace.equals(event.alertDefinitionNamespace())
                && event.timestamp() != null
                && !event.timestamp().isBefore(start)
                && !event.timestamp().isAfter(end));
    }
}
