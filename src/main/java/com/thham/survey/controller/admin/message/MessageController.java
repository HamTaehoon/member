package com.thham.survey.controller.admin.message;

import com.thham.survey.controller.admin.message.model.SendMessageRequest;
import com.thham.survey.domain.message.dto.MessageJobsDto;
import com.thham.survey.domain.message.dto.MessageMapper;
import com.thham.survey.domain.message.dto.SendMessageDto;
import com.thham.survey.domain.message.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/admin/messages")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class MessageController {

    private final MessageService messageService;
    private final MessageMapper messageMapper;

    @Operation(summary = "Send messages by age group", description = "Sends KakaoTalk or SMS messages to members in specified age groups")
    @PostMapping
    public ResponseEntity<MessageJobsDto> sendMessages(@Valid @RequestBody SendMessageRequest request) {
        SendMessageDto dto = messageMapper.sendMessageRequestToSendMessageDto(request);
        MessageJobsDto result = messageService.sendMessagesByAgeGroup(dto);
        return ResponseEntity.ok(result);
    }
}