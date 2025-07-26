package com.thham.survey.controller.member;

import com.thham.survey.controller.member.model.MemberRequest;
import com.thham.survey.domain.member.dto.MemberDto;
import com.thham.survey.domain.member.dto.MemberMapper;
import com.thham.survey.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @Operation(summary = "Register a new member", description = "Creates a new member.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<MemberDto> register(@Valid @RequestBody MemberRequest request) {
        MemberDto dto = memberMapper.memberRequestToMemberDto(request);
        MemberDto result = memberService.registerMember(dto);
        return ResponseEntity.ok(result);
    }
}