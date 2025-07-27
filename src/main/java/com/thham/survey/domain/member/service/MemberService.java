package com.thham.survey.domain.member.service;

import com.thham.survey.common.util.jwt.JwtUtil;
import com.thham.survey.controller.admin.member.model.UpdateMemberRequest;
import com.thham.survey.domain.member.dto.MemberDto;
import com.thham.survey.domain.member.dto.MemberMapper;
import com.thham.survey.domain.member.entity.Member;
import com.thham.survey.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public MemberDto registerMember(MemberDto dto) {
        Optional.of(dto.account())
                .filter(account -> !memberRepository.existsByAccount(account))
                .orElseThrow(() -> new IllegalArgumentException("Account already exists"));

        Optional.of(dto.residentId())
                .filter(residentId -> !memberRepository.existsByResidentId(residentId))
                .orElseThrow(() -> new IllegalArgumentException("Resident ID already exists"));

        String yearPrefix = dto.residentId().substring(0, 2);
        int year = Integer.parseInt(yearPrefix);
        int birthYear = year >= 0 && year <= 25 ? 2000 + year : 1900 + year;

        Member savedMember = memberRepository.save(Member.builder()
                .account(dto.account())
                .password(passwordEncoder.encode(dto.password()))
                .name(dto.name())
                .residentId(dto.residentId())
                .phoneNumber(dto.phoneNumber())
                .address(dto.address())
                .birthYear(birthYear)
                .build());

        return MemberDto.builder()
                .account(savedMember.getAccount())
                .build();
    }

    public Page<MemberDto> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(memberMapper::memberToMemberDto);
    }

    @Transactional
    public MemberDto updateMember(Long id, UpdateMemberRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (request.password() != null) {
            member.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.address() != null) {
            member.setAddress(request.address());
        }

        return memberMapper.memberToMemberDto(member);
    }

    @Transactional
    public void deleteMember(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new IllegalArgumentException("Member not found");
        }
        memberRepository.deleteById(id);
    }

    public MemberDto getMemberDetails(String accountId) {
        String authenticatedUsername = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!authenticatedUsername.equals(accountId)) {
//            throw new IllegalArgumentException("Access denied: Can only view own details");
            throw new IllegalArgumentException("Access denied");
        }

        Member member = memberRepository.findByAccount(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        String topLevelAddress = member.getAddress().split(" ")[0];
        return MemberDto.builder()
                .account(member.getAccount())
                .name(member.getName())
                .phoneNumber(member.getPhoneNumber())
                .address(topLevelAddress)
                .build();
    }

    public String login(MemberDto dto) {
        Member member = memberRepository.findByAccount(dto.account())
                .orElseThrow(() -> new IllegalArgumentException("Invalid account or password"));
        if (!passwordEncoder.matches(dto.password(), member.getPassword())) {
            throw new IllegalArgumentException("Invalid account or password");
        }
        return jwtUtil.generateAccessToken(member.getAccount());
    }

    public String generateRefreshToken(MemberDto dto) {
        Member member = memberRepository.findByAccount(dto.account())
                .orElseThrow(() -> new IllegalArgumentException("Invalid account"));
        return jwtUtil.generateRefreshToken(member.getAccount());
    }
}