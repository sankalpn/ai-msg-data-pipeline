package com.acuvity.log.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record Principal(
        @JsonProperty("IP") String ip,
        String authType,
        List<String> teams,
        String type,
        User user) {
}
