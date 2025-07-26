package com.thham.survey.controller.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thham.survey.common.exception.GlobalExceptionHandler;
import com.thham.survey.controller.login.model.LoginRequest;
import com.thham.survey.domain.member.dto.MemberDto;
import com.thham.survey.domain.member.dto.MemberMapper;
import com.thham.survey.domain.member.service.MemberService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @InjectMocks
    private LoginController loginController;

    @Mock
    private MemberService memberService;

    @Mock
    private MemberMapper memberMapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private LoginRequest validRequest;
    private LoginRequest invalidRequest;
    private MemberDto memberDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Jackson 모듈 등록

        validRequest = new LoginRequest("thham123", "Password123!");
        invalidRequest = new LoginRequest("", "");
        memberDto = new MemberDto("thham123", "Password123!", null, null, null, null);

        mockMvc = MockMvcBuilders.standaloneSetup(loginController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void login_Success() throws Exception {
        // Given
        when(memberMapper.loginRequestToMemberDto(any(LoginRequest.class))).thenReturn(memberDto);
        when(memberService.login(memberDto)).thenReturn("accessToken");
        when(memberService.generateRefreshToken(memberDto)).thenReturn("refreshToken");

        // When & Then
        mockMvc.perform(post("/v1/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(objectMapper.writeValueAsString(validRequest))) // 객체 → JSON
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(header().string("Access-Token", "accessToken"))
                .andExpect(cookie().value("Refresh-Token", "refreshToken"))
                .andExpect(cookie().httpOnly("Refresh-Token", true))
                .andExpect(cookie().secure("Refresh-Token", true))
                .andExpect(cookie().path("Refresh-Token", "/"))
                .andExpect(cookie().maxAge("Refresh-Token", 3600))
                .andExpect(cookie().exists("Refresh-Token"));
    }

    @Test
    @DisplayName("유효하지 않은 입력 데이터로 로그인 시 400 에러")
    void login_InvalidInput_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("잘못된 계정 또는 비밀번호로 로그인 시 409 에러")
    void login_InvalidCredentials_ReturnsConflict() throws Exception {
        // Given
        when(memberMapper.loginRequestToMemberDto(any(LoginRequest.class))).thenReturn(memberDto);
        when(memberService.login(memberDto))
                .thenThrow(new IllegalArgumentException("Invalid account or password"));

        // When & Then
        mockMvc.perform(post("/v1/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Invalid account or password"));
    }

    @Test
    @DisplayName("LoginRequest 유효성 검사")
    void validateLoginRequest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(validRequest);
        violations.forEach(v -> System.out.println("Validation Error: " + v.getPropertyPath() + ": " + v.getMessage()));
        assertTrue(violations.isEmpty(), "Valid request should pass validation");
    }
}