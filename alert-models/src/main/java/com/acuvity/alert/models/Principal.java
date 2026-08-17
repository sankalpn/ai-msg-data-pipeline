package com.acuvity.alert.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record Principal(
        @JsonProperty("IP") String ip,
        String authType,
        List<String> teams,
        String type,
        @Valid @NotNull User user) {
}
