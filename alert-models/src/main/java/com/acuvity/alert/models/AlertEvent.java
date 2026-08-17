package com.acuvity.alert.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record AlertEvent(
        @NotBlank String alertDefinition,
        @NotBlank String alertDefinitionNamespace,
        @Valid @NotNull Principal principal,
        @NotBlank String provider,
        @NotNull Instant timestamp) {
}
