package com.thham.survey.controller.member;

import com.thham.survey.controller.member.model.MemberRequest;
import com.thham.survey.domain.member.dto.MemberDto;
import com.thham.survey.domain.member.dto.MemberMapper;
import com.thham.survey.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @Operation(summary = "Register a new member", description = "Creates a new member.")
    @PostMapping
    public ResponseEntity<MemberDto> register(@Valid @RequestBody MemberRequest request) {
        MemberDto dto = memberMapper.memberRequestToMemberDto(request);
        MemberDto result = memberService.registerMember(dto);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Retrieve member's details", description = "Fetches the details of the specified member, with address limited to the top-level administrative division.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{accountId}")
    public ResponseEntity<MemberDto> getMemberDetails(
            @Parameter(description = "Account ID", example = "thham")
            @PathVariable String accountId) {
        MemberDto memberDto = memberService.getMemberDetails(accountId);
        return ResponseEntity.ok(memberDto);
    }
}