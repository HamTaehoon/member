package com.thham.survey.domain.member.service;

import com.thham.survey.controller.member.model.MemberRequest;
import com.thham.survey.domain.member.dto.MemberDto;
import com.thham.survey.domain.member.dto.MemberMapper;
import com.thham.survey.domain.member.entity.Member;
import com.thham.survey.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    private MemberDto validDto;
    private MemberDto encodedDto;
    private Member member;

    @BeforeEach
    void setUp() {
        validDto = new MemberDto(
                "thham",
                "password1!",
                "Taehoon Ham",
                "1234567890123",
                "01012345678",
                "서울특별시 OO구 OO로"
        );

        encodedDto = new MemberDto(
                "thham",
                "$2a$10$hashedPassword",
                "Taehoon Ham",
                "1234567890123",
                "01012345678",
                "서울특별시 OO구 OO로"
        );

        member = Member.builder()
                .account("thham")
                .password("$2a$10$hashedPassword")
                .name("Taehoon Ham")
                .residentId("1234567890123")
                .phoneNumber("01012345678")
                .address("서울특별시 OO구 OO로")
                .build();
    }

    @Test
    @DisplayName("회원 등록 성공 테스트")
    void registerMember_Success() {
        // Given
        when(memberRepository.existsByAccount("thham")).thenReturn(false);
        when(memberRepository.existsByResidentId("1234567890123")).thenReturn(false);
        when(passwordEncoder.encode("password1!")).thenReturn("$2a$10$hashedPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        MemberDto result = memberService.registerMember(validDto);

        // Then
        assertNotNull(result);
        assertEquals("thham", result.account());
        verify(passwordEncoder).encode("password1!");
        verify(memberRepository).save(any(Member.class));
        verify(memberRepository).existsByAccount("thham");
        verify(memberRepository).existsByResidentId("1234567890123");
    }

    @Test
    @DisplayName("이미 존재하는 계정으로 회원 등록 시 예외 발생 테스트")
    void registerMember_AccountAlreadyExists_ThrowsIllegalArgumentException() {
        // Given
        when(memberRepository.existsByAccount("thham")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> memberService.registerMember(validDto));
        assertEquals("Account already exists", exception.getMessage());
        verify(memberRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("존재하지 않는 계정과 이미 존재하는 주민번호로 회원 등록 시 예외 발생 테스트")
    void registerMember_ResidentIdAlreadyExists_ThrowsIllegalArgumentException() {
        // Given
        when(memberRepository.existsByAccount("thham")).thenReturn(false);
        when(memberRepository.existsByResidentId("1234567890123")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> memberService.registerMember(validDto));
        assertEquals("Resident ID already exists", exception.getMessage());
        verify(memberRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}