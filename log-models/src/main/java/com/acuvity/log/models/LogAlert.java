package com.acuvity.log.models;

public record LogAlert(
        String alertDefinition,
        String alertDefinitionNamespace,
        String provider) {
}
