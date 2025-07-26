package com.thham.survey.controller.admin.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record UpdateMemberRequest(
        @Size(min = 8, max = 100)
        @Schema(description = "New password (optional)", example = "newPassword1!")
        String password,

        @Schema(description = "New address (optional)", example = "서울특별시 새주소로")
        String address
) {}