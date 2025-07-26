package com.thham.survey.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MemberDto(
        String account,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password,
        String name,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String residentId,
        String phoneNumber,
        String address
) {}