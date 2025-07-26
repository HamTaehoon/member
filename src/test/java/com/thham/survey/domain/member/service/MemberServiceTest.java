package com.thham.survey.domain.member.service;

import com.thham.survey.controller.admin.model.UpdateMemberRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

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
                .id(1L)
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

    @Test
    @DisplayName("모든 회원 페이징 조회 성공 테스트")
    void getAllMembers_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> memberPage = new PageImpl<>(List.of(member));
        when(memberRepository.findAll(pageable)).thenReturn(memberPage);
        when(memberMapper.memberToMemberDto(member)).thenReturn(encodedDto);

        // When
        Page<MemberDto> result = memberService.getAllMembers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("thham", result.getContent().get(0).account());
        verify(memberRepository).findAll(pageable);
        verify(memberMapper).memberToMemberDto(member);
    }

    @Test
    @DisplayName("회원 정보 수정 성공 테스트 - 비밀번호와 주소 모두 수정")
    void updateMember_Success_BothPasswordAndAddress() {
        // Given
        UpdateMemberRequest request = new UpdateMemberRequest("newPassword1!", "서울특별시 새주소로");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.encode("newPassword1!")).thenReturn("$2a$10$newHashedPassword");
        when(memberMapper.memberToMemberDto(member)).thenReturn(encodedDto);

        // When
        MemberDto result = memberService.updateMember(1L, request);

        // Then
        assertNotNull(result);
        assertEquals("thham", result.account());
        verify(memberRepository).findById(1L);
        verify(passwordEncoder).encode("newPassword1!");
        verify(memberMapper).memberToMemberDto(member);
        assertEquals("$2a$10$newHashedPassword", member.getPassword());
        assertEquals("서울특별시 새주소로", member.getAddress());
    }

    @Test
    @DisplayName("회원 정보 수정 성공 테스트 - 비밀번호만 수정")
    void updateMember_Success_PasswordOnly() {
        // Given
        UpdateMemberRequest request = new UpdateMemberRequest("newPassword1!", null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.encode("newPassword1!")).thenReturn("$2a$10$newHashedPassword");
        when(memberMapper.memberToMemberDto(member)).thenReturn(encodedDto);

        // When
        MemberDto result = memberService.updateMember(1L, request);

        // Then
        assertNotNull(result);
        assertEquals("thham", result.account());
        verify(memberRepository).findById(1L);
        verify(passwordEncoder).encode("newPassword1!");
        verify(memberMapper).memberToMemberDto(member);
        assertEquals("$2a$10$newHashedPassword", member.getPassword());
        assertEquals("서울특별시 OO구 OO로", member.getAddress());
    }

    @Test
    @DisplayName("회원 정보 수정 성공 테스트 - 주소만 수정")
    void updateMember_Success_AddressOnly() {
        // Given
        UpdateMemberRequest request = new UpdateMemberRequest(null, "서울특별시 새주소로");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberMapper.memberToMemberDto(member)).thenReturn(encodedDto);

        // When
        MemberDto result = memberService.updateMember(1L, request);

        // Then
        assertNotNull(result);
        assertEquals("thham", result.account());
        verify(memberRepository).findById(1L);
        verify(passwordEncoder, never()).encode(any());
        verify(memberMapper).memberToMemberDto(member);
        assertEquals("$2a$10$hashedPassword", member.getPassword());
        assertEquals("서울특별시 새주소로", member.getAddress());
    }

    @Test
    @DisplayName("존재하지 않는 회원 수정 시 예외 발생 테스트")
    void updateMember_MemberNotFound_ThrowsIllegalArgumentException() {
        // Given
        UpdateMemberRequest request = new UpdateMemberRequest("newPassword1!", "서울특별시 새주소로");
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> memberService.updateMember(1L, request));
        assertEquals("Member not found", exception.getMessage());
        verify(memberRepository).findById(1L);
        verify(passwordEncoder, never()).encode(any());
        verify(memberMapper, never()).memberToMemberDto(any());
    }

    @Test
    @DisplayName("회원 삭제 성공 테스트")
    void deleteMember_Success() {
        // Given
        when(memberRepository.existsById(1L)).thenReturn(true);

        // When
        memberService.deleteMember(1L);

        // Then
        verify(memberRepository).existsById(1L);
        verify(memberRepository).deleteById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 회원 삭제 시 예외 발생 테스트")
    void deleteMember_MemberNotFound_ThrowsIllegalArgumentException() {
        // Given
        when(memberRepository.existsById(1L)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> memberService.deleteMember(1L));
        assertEquals("Member not found", exception.getMessage());
        verify(memberRepository).existsById(1L);
        verify(memberRepository, never()).deleteById(any());
    }
}