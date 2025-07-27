package com.thham.survey.domain.member.repository;

import com.thham.survey.domain.message.dto.AgeGroupMemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {
    Page<AgeGroupMemberDto> findByAgeGroup(String ageGroup, int currentYear, Pageable pageable);
}