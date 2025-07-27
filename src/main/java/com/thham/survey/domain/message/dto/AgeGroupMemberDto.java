package com.thham.survey.domain.message.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class AgeGroupMemberDto {

    private final String name;
    private final String phoneNumber;
    private final int birthYear;

    @QueryProjection
    public AgeGroupMemberDto(String name, String phoneNumber, int birthYear) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birthYear = birthYear;
    }
}