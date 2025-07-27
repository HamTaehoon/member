package com.thham.survey.domain.member.repository;

import com.thham.survey.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    boolean existsByAccount(String account);
    boolean existsByResidentId(String residentId);
    Optional<Member> findByAccount(String account);
}