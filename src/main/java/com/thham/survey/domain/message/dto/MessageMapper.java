package com.thham.survey.domain.message.dto;

import com.thham.survey.controller.admin.message.model.SendMessageRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    SendMessageDto sendMessageRequestToSendMessageDto(SendMessageRequest request);
}