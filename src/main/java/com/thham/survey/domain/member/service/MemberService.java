package com.thham.survey.domain.member.service;

import com.thham.survey.domain.member.dto.MemberDto;
import com.thham.survey.domain.member.dto.MemberMapper;
import com.thham.survey.domain.member.entity.Member;
import com.thham.survey.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public MemberDto registerMember(MemberDto dto) {
        Optional.of(dto.account())
                .filter(account -> !memberRepository.existsByAccount(account))
                .orElseThrow(() -> new IllegalArgumentException("Account already exists"));

        Optional.of(dto.residentId())
                .filter(residentId -> !memberRepository.existsByResidentId(residentId))
                .orElseThrow(() -> new IllegalArgumentException("Resident ID already exists"));

        Member savedMember = memberRepository.save(Member.builder()
                .account(dto.account())
                        .password(passwordEncoder.encode(dto.password()))
                        .name(dto.name())
                        .residentId(dto.residentId())
                        .phoneNumber(dto.phoneNumber())
                        .address(dto.address())
                .build());

        return MemberDto.builder()
                .account(savedMember.getAccount())
                .build();
    }
}