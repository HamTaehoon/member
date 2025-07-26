package com.thham.survey.controller.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thham.survey.common.exception.GlobalExceptionHandler;
import com.thham.survey.controller.member.model.MemberRequest;
import com.thham.survey.domain.member.dto.MemberDto;
import com.thham.survey.domain.member.dto.MemberMapper;
import com.thham.survey.domain.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @InjectMocks
    private MemberController memberController;

    @Mock
    private MemberService memberService;

    @Mock
    private MemberMapper memberMapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private MemberRequest validRequest;
    private MemberRequest invalidRequest;
    private MemberDto memberDto;
    private MemberDto resultDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        validRequest = new MemberRequest(
                "thham",
                "password1!",
                "Taehoon Ham",
                "1234567890123",
                "01012345678",
                "서울특별시 OO구 OO로"
        );

        invalidRequest = new MemberRequest(
                "th", // Too short (min 4)
                "pass", // Too short (min 8)
                "", // Blank name
                "123456789", // Too short (13 digits)
                "010123456", // Too short (11 digits)
                "" // Blank address
        );

        memberDto = new MemberDto(
                "thham",
                "password1!",
                "Taehoon Ham",
                "1234567890123",
                "01012345678",
                "서울특별시 OO구 OO로"
        );

        resultDto = new MemberDto(
                "thham",
                null,
                null,
                null,
                null,
                null
        );

        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("회원 등록 성공 테스트")
    void register_Success() throws Exception {
        // Given
        when(memberMapper.memberRequestToMemberDto(validRequest)).thenReturn(memberDto);
        when(memberService.registerMember(memberDto)).thenReturn(resultDto);

        // When & Then
        mockMvc.perform(post("/v1/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account").value("thham"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.name").doesNotExist())
                .andExpect(jsonPath("$.residentId").doesNotExist())
                .andExpect(jsonPath("$.phoneNumber").doesNotExist())
                .andExpect(jsonPath("$.address").doesNotExist());
    }

    @Test
    @DisplayName("유효하지 않은 입력 데이터로 회원 등록 시 400 에러")
    void register_InvalidInput_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이미 존재하는 계정으로 회원 등록 시 409 에러")
    void register_AccountAlreadyExists_ReturnsConflict() throws Exception {
        // Given
        when(memberMapper.memberRequestToMemberDto(validRequest)).thenReturn(memberDto);
        when(memberService.registerMember(memberDto))
                .thenThrow(new IllegalArgumentException("Account already exists"));

        // When & Then
        mockMvc.perform(post("/v1/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Account already exists"));
    }

    @Test
    @DisplayName("이미 존재하는 주민번호로 회원 등록 시 409 에러")
    void register_ResidentIdAlreadyExists_ReturnsConflict() throws Exception {
        // Given
        when(memberMapper.memberRequestToMemberDto(validRequest)).thenReturn(memberDto);
        when(memberService.registerMember(memberDto))
                .thenThrow(new IllegalArgumentException("Resident ID already exists"));

        // When & Then
        mockMvc.perform(post("/v1/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Resident ID already exists"));
    }
}