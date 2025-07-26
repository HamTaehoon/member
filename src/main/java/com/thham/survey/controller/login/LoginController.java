package com.thham.survey.controller.login;

import com.thham.survey.controller.login.model.LoginRequest;
import com.thham.survey.controller.login.model.LoginResponse;
import com.thham.survey.domain.member.dto.MemberDto;
import com.thham.survey.domain.member.dto.MemberMapper;
import com.thham.survey.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/login")
@RequiredArgsConstructor
public class LoginController {

    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @Operation(summary = "User login", description = "Authenticates a user, sets access token in response header and refresh token in HttpOnly cookie.")
    @PostMapping
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        MemberDto dto = memberMapper.loginRequestToMemberDto(request);

        String accessToken = memberService.login(dto);
        String refreshToken = memberService.generateRefreshToken(dto);

        Cookie cookie = new Cookie("Refresh-Token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);

        response.addCookie(cookie);
        response.setHeader("Access-Token", accessToken);

        return ResponseEntity.ok(new LoginResponse(true, "Login successful"));
    }
}