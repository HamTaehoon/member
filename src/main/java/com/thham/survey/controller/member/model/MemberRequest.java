package com.thham.survey.controller.member.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberRequest(
        @NotBlank @Size(min = 4, max = 50)
        @Schema(description = "User account ID", example = "thham")
        String account,

        @NotBlank @Size(min = 8, max = 100)
        @Schema(description = "User password", example = "password1!")
        String password,

        @NotBlank @Size(max = 50)
        @Schema(description = "Full name of the member", example = "Taehoon Ham")
        String name,

        @NotBlank @Size(min = 13, max = 13)
        @Schema(description = "Resident ID (13 digits)", example = "1234567890123")
        String residentId,

        @NotBlank @Size(min = 11, max = 11)
        @Schema(description = "Phone number (11 digits)", example = "01012345678")
        String phoneNumber,

        @NotBlank
        @Schema(description = "Member's address", example = "서울특별시 OO구 OO로")
        String address
) {}