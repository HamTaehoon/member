package com.thham.survey.controller.login.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(min = 4, max = 50)
        @Schema(description = "User account ID", example = "thham")
        String account,

        @NotBlank @Size(min = 8, max = 100)
        @Schema(description = "User password", example = "password1!")
        String password
) {}