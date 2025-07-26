package com.thham.survey.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thham.survey.common.exception.GlobalExceptionHandler;
import com.thham.survey.controller.admin.model.UpdateMemberRequest;
import com.thham.survey.domain.member.dto.MemberDto;
import com.thham.survey.domain.member.dto.MemberMapper;
import com.thham.survey.domain.member.service.MemberService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @InjectMocks
    private AdminController adminController;

    @Mock
    private MemberService memberService;

    @Mock
    private MemberMapper memberMapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private String basicAuthHeader;

    private MemberDto memberDto;
    private UpdateMemberRequest validUpdateRequest;
    private UpdateMemberRequest invalidUpdateRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        Filter authFilter = (request, response, chain) -> {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.equals("Basic " + Base64.getEncoder().encodeToString("admin:1212".getBytes()))) {
                httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                httpResponse.getWriter().write("{\"error\":\"Unauthorized\"}");
                return;
            }
            chain.doFilter(request, response);
        };

        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilter(authFilter, "/*")
                .build();

        // Basic Auth header for admin:1212
        String credentials = "admin:1212";
        basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

        memberDto = new MemberDto(
                "thham",
                null,
                "Taehoon Ham",
                null,
                "01012345678",
                "서울특별시 OO구 OO로"
        );

        validUpdateRequest = new UpdateMemberRequest(
                "newPassword1!",
                "서울특별시 새주소로"
        );

        invalidUpdateRequest = new UpdateMemberRequest(
                "short", // Too short (min 8)
                null
        );
    }

    @Test
    @DisplayName("모든 회원을 페이지네이션으로 조회 성공 테스트")
    void getAllMembers_Success() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberDto> memberPage = new PageImpl<>(List.of(memberDto), pageable, 1);
        when(memberService.getAllMembers(pageable)).thenReturn(memberPage);

        // When & Then
        mockMvc.perform(get("/v1/api/admin/members")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", basicAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].account").value("thham"))
                .andExpect(jsonPath("$.content[0].name").value("Taehoon Ham"))
                .andExpect(jsonPath("$.content[0].phoneNumber").value("01012345678"))
                .andExpect(jsonPath("$.content[0].address").value("서울특별시 OO구 OO로"))
                .andExpect(jsonPath("$.content[0].password").doesNotExist())
                .andExpect(jsonPath("$.content[0].residentId").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @DisplayName("인증 없이 회원 조회 시 401 에러 테스트")
    void getAllMembers_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/api/admin/members")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("회원 정보 업데이트 성공 테스트")
    void updateMember_Success() throws Exception {
        // Given
        Long id = 1L;
        when(memberService.updateMember(eq(id), any(UpdateMemberRequest.class))).thenReturn(memberDto);

        // When & Then
        mockMvc.perform(patch("/v1/api/admin/members/{id}", id)
                        .header("Authorization", basicAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account").value("thham"))
                .andExpect(jsonPath("$.name").value("Taehoon Ham"))
                .andExpect(jsonPath("$.phoneNumber").value("01012345678"))
                .andExpect(jsonPath("$.address").value("서울특별시 OO구 OO로"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.residentId").doesNotExist());
    }

    @Test
    @DisplayName("유효하지 않은 입력으로 회원 업데이트 시 400 에러 테스트")
    void updateMember_InvalidInput_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(patch("/v1/api/admin/members/{id}", 1L)
                        .header("Authorization", basicAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 회원 업데이트 시 409 에러 테스트")
    void updateMember_MemberNotFound_ReturnsConflict() throws Exception {
        // Given
        Long id = 999L;
        when(memberService.updateMember(eq(id), any(UpdateMemberRequest.class)))
                .thenThrow(new IllegalArgumentException("Member not found"));

        // When & Then
        mockMvc.perform(patch("/v1/api/admin/members/{id}", id)
                        .header("Authorization", basicAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Member not found"));
    }

    @Test
    @DisplayName("회원 삭제 성공 테스트")
    void deleteMember_Success() throws Exception {
        // Given
        Long id = 1L;
        doNothing().when(memberService).deleteMember(id);

        // When & Then
        mockMvc.perform(delete("/v1/api/admin/members/{id}", id)
                        .header("Authorization", basicAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("존재하지 않는 회원 삭제 시 409 에러 테스트")
    void deleteMember_MemberNotFound_ReturnsConflict() throws Exception {
        // Given
        Long id = 999L;
        doThrow(new IllegalArgumentException("Member not found")).when(memberService).deleteMember(id);

        // When & Then
        mockMvc.perform(delete("/v1/api/admin/members/{id}", id)
                        .header("Authorization", basicAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Member not found"));
    }

    @Test
    @DisplayName("인증 없이 회원 삭제 시 401 에러 테스트")
    void deleteMember_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(delete("/v1/api/admin/members/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    public MemberMapper getMemberMapper() {
        return memberMapper;
    }

    public void setMemberMapper(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }
}