package com.acuvity.alert.models;

import jakarta.validation.constraints.NotBlank;

public record User(@NotBlank String name) {
}
