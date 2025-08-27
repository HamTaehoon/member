package com.thham.survey.domain.message.service;

import com.thham.survey.common.client.message.MessageClient;
import com.thham.survey.domain.message.constants.JobStatus;
import com.thham.survey.domain.message.dto.AgeGroupMemberDto;
import com.thham.survey.domain.message.dto.SendMessageDto;
import com.thham.survey.domain.member.repository.MemberRepository;
import com.thham.survey.domain.message.entity.MessageJobs;
import com.thham.survey.domain.message.repository.MessageJobRepository;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageAsyncHandler {

    private final MemberRepository memberRepository;
    private final MessageClient messageClient;
    private final MessageJobRepository messageJobRepository;
    private final Bucket kakaoTalkBucket;
    private final Bucket smsBucket;

    @Value("${message.page-size}")
    private int PAGE_SIZE;

    @Value("${message.age-groups}")
    private List<String> AGE_GROUPS;

    @Async
    @Transactional
    public void processAndSendMessagesByAgeGroup(SendMessageDto dto, Long jobId) {
        MessageJobs messageJob = messageJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid job ID: " + jobId));

        try {
            messageJob.setStatus(JobStatus.IN_PROGRESS);
            messageJob.setUpdatedAt(LocalDateTime.now());

            Map<String, Integer> ageGroupCounts = new HashMap<>();
            int currentYear = LocalDate.now().getYear();

            for (String ageGroup : AGE_GROUPS) {
                AtomicInteger page = new AtomicInteger(0);
                Page<AgeGroupMemberDto> membersPage;

                log.info("Processing age group: {}", ageGroup);
                do {
                    membersPage = memberRepository.findByAgeGroup(ageGroup, currentYear, PageRequest.of(page.getAndIncrement(), PAGE_SIZE));

                    membersPage.getContent().forEach(member -> {
                        sendMessage(member, dto.message());
                    });

                } while (membersPage.hasNext());
            }

            messageJob.setStatus(JobStatus.COMPLETED);
            messageJob.setUpdatedAt(LocalDateTime.now());
            messageJobRepository.save(messageJob);

            log.info("Messages sent to age groups: {}", ageGroupCounts);
            log.info("Message Job Entity jobId: {}, completion: {}", messageJob.getId(), messageJob.getStatus());

        } catch (Exception e) {
            messageJob.setStatus(JobStatus.FAILED);
            messageJob.setUpdatedAt(LocalDateTime.now());
            messageJob.setErrorMessage(e.getMessage());
            messageJobRepository.save(messageJob);

            log.error("Message sending failed for job {}: {}", messageJob.getId(), e.getMessage(), e);
            log.error("Message Job Entity after failure: {}", messageJob);
        }
    }

    private void sendMessage(AgeGroupMemberDto member, String messageContent) {
        String message = member.getName() + "님, 안녕하세요.\n" + messageContent;

        log.debug("KakaoTalk bucket available tokens: {}", kakaoTalkBucket.getAvailableTokens());
        try {
            if (kakaoTalkBucket.tryConsume(1)) {
                ResponseEntity<Void> response = messageClient.sendKakaoTalkMessage(member.getPhoneNumber(), message);
                log.info("KakaoTalk sent to {}, status: {}", member.getPhoneNumber(), response.getStatusCode());
            } else {
                log.warn("KakaoTalk rate limit exceeded, falling back to SMS for {}", member.getPhoneNumber());
                sendSmsMessage(member, message);
            }
        } catch (Exception e) {
            log.warn("KakaoTalk failed for {}, sending SMS: {}. Error: {}", member.getPhoneNumber(), e.getMessage(), e.getClass().getSimpleName());
            sendSmsMessage(member, message);
        }
    }

    private void sendSmsMessage(AgeGroupMemberDto member, String message) {
        log.debug("SMS bucket available tokens: {}", smsBucket.getAvailableTokens());
        if (smsBucket.tryConsume(1)) {
            ResponseEntity<String> response = messageClient.sendSmsMessage(member.getPhoneNumber(), message);
            log.info("SMS sent to {}, status: {}, response: {}", member.getPhoneNumber(), response.getStatusCode(), response.getBody());
        } else {
            log.error("SMS rate limit exceeded for {}", member.getPhoneNumber());
            throw new RuntimeException("SMS rate limit exceeded");
        }
    }
}