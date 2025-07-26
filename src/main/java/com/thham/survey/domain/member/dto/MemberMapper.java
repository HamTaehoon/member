package com.thham.survey.domain.member.dto;

import com.thham.survey.controller.member.model.MemberRequest;
import com.thham.survey.domain.member.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    MemberDto memberRequestToMemberDto(MemberRequest request);

    @Mapping(target = "id", ignore = true)
    Member memberDtoToMember(MemberDto dto);
}