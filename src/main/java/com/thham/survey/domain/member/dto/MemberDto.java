package com.thham.survey.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MemberDto(
        String account,
        String password,
        String name,
        String residentId,
        String phoneNumber,
        String address
) {}