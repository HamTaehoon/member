package com.thham.survey.domain.message.service;

import com.thham.survey.domain.message.constants.JobStatus;
import com.thham.survey.domain.message.dto.MessageJobsDto;
import com.thham.survey.domain.message.dto.SendMessageDto;
import com.thham.survey.domain.message.entity.MessageJobs;
import com.thham.survey.domain.message.repository.MessageJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageJobRepository messageJobRepository;
    private final MessageAsyncHandler messageAsyncHandler;

    @Transactional
    public MessageJobsDto sendMessagesByAgeGroup(SendMessageDto dto) {
        MessageJobs messageJob = MessageJobs.builder()
                .status(JobStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        messageJob = messageJobRepository.save(messageJob);

        // 현재 트랜잭션이 성공적으로 커밋된 후에 비동기 작업을 시작하도록 등록
        Long jobId = messageJob.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messageAsyncHandler.processAndSendMessagesByAgeGroup(dto, jobId);
            }
        });

        return new MessageJobsDto(
                messageJob.getId(),
                messageJob.getStatus(),
                null
        );
    }
}