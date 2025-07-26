package com.thham.survey.domain.member.repository;

import com.thham.survey.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByAccount(String account);
    boolean existsByResidentId(String residentId);
}