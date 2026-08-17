package com.acuvity.pipeline.serviceconfig;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("pipeline")
public record PipelineProperties(
        @Valid @NotNull LogService logService,
        @Valid @NotNull Topics topics,
        @NotNull URI serviceBaseUrl,
        List<String> enabledNamespaces,
        @NotNull Duration publishTimeout) {

    public PipelineProperties {
        enabledNamespaces = enabledNamespaces == null
                ? List.of()
                : List.copyOf(new LinkedHashSet<>(enabledNamespaces));
    }

    public record LogService(
            @NotNull URI baseUrl,
            @NotNull Duration correlationTolerance,
            @NotNull Duration connectTimeout,
            @NotNull Duration requestTimeout) {}

    public record Topics(@NotBlank String enrichedAlerts, @NotBlank String syncedLogs) {}

}
