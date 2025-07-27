package com.thham.survey.controller.admin.message.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
        @NotBlank
        @Schema(description = "Message content to append after greeting", example = "프로모션 안내드립니다.")
        String message
) {}