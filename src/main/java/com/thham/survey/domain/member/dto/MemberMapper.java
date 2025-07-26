package com.thham.survey.domain.member.dto;

import com.thham.survey.controller.member.model.MemberRequest;
import com.thham.survey.domain.member.entity.Member;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    MemberDto memberRequestToMemberDto(MemberRequest request);

    MemberDto memberToMemberDto(Member member);
}