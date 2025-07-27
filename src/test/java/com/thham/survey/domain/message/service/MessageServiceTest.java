package com.thham.survey.domain.message.service;

import com.thham.survey.common.client.message.MessageClient;
import com.thham.survey.domain.message.constants.JobStatus;
import com.thham.survey.domain.message.dto.AgeGroupMemberDto;
import com.thham.survey.domain.message.dto.SendMessageDto;
import com.thham.survey.domain.message.entity.MessageJobs;
import com.thham.survey.domain.message.repository.MessageJobRepository;
import com.thham.survey.domain.member.repository.MemberRepository;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    private MessageAsyncHandler messageAsyncHandler;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MessageClient messageClient;

    @Mock
    private MessageJobRepository messageJobRepository;

    @Mock
    private Bucket kakaoTalkBucket;

    @Mock
    private Bucket smsBucket;

    private SendMessageDto testSendMessageDto;
    private MessageJobs testMessageJob;
    private AgeGroupMemberDto testMemberDto;

    @BeforeEach
    void setUp() {
        Mockito.reset(memberRepository, messageClient, messageJobRepository, kakaoTalkBucket, smsBucket);

        testSendMessageDto = new SendMessageDto("프로모션 안내드립니다.");
        testMessageJob = MessageJobs.builder()
                .id(1L)
                .status(JobStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        testMemberDto = new AgeGroupMemberDto("thham", "01012345678", 1989);

        this.messageAsyncHandler = new MessageAsyncHandler(
                memberRepository,
                messageClient,
                messageJobRepository,
                kakaoTalkBucket,
                smsBucket
        );

        ReflectionTestUtils.setField(messageAsyncHandler, "PAGE_SIZE", 1000);
        ReflectionTestUtils.setField(messageAsyncHandler, "AGE_GROUPS", List.of("0~10", "20s", "30s", "40s", "50s", "60s", "70s+"));

        when(messageJobRepository.findById(anyLong())).thenReturn(Optional.of(testMessageJob));
        when(messageJobRepository.save(any(MessageJobs.class))).thenReturn(testMessageJob);
    }

    @Test
    @DisplayName("카카오톡 레이트 리밋 초과 시 SMS로 대체되는지 테스트")
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testKakaoTalkRateLimitExceededFallbackToSms() {
        // given
        int kakaoTalkLimit = 100;
        int membersPerPage = 1000;
        int totalMembers = kakaoTalkLimit + 5; // 105명
        List<String> ageGroupsForTest = List.of("20s");

        ReflectionTestUtils.setField(messageAsyncHandler, "AGE_GROUPS", ageGroupsForTest);

        List<AgeGroupMemberDto> members = IntStream.range(0, totalMembers)
                .mapToObj(i -> new AgeGroupMemberDto("thham" + i, "0101111222" + i, 1990))
                .collect(Collectors.toList());

        when(memberRepository.findByAgeGroup(anyString(), anyInt(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(members, PageRequest.of(0, membersPerPage), totalMembers));

        when(messageClient.sendKakaoTalkMessage(anyString(), anyString())).thenReturn(ResponseEntity.ok().build());
        when(messageClient.sendSmsMessage(anyString(), anyString())).thenReturn(ResponseEntity.ok("SMS Sent"));

        AtomicInteger kakaoTalkConsumeCount = new AtomicInteger(0);
        when(kakaoTalkBucket.tryConsume(1)).thenAnswer(invocation -> {
            if (kakaoTalkConsumeCount.getAndIncrement() < kakaoTalkLimit) {
                return true;
            }
            return false;
        });

        when(smsBucket.tryConsume(1)).thenReturn(true);

        // when
        messageAsyncHandler.processAndSendMessagesByAgeGroup(testSendMessageDto, testMessageJob.getId());

        // then
        verify(messageClient, times(kakaoTalkLimit)).sendKakaoTalkMessage(anyString(), anyString());
        verify(messageClient, times(totalMembers - kakaoTalkLimit)).sendSmsMessage(anyString(), anyString());

        assertEquals(JobStatus.COMPLETED, testMessageJob.getStatus());
    }

    @Test
    @DisplayName("SMS 레이트 리밋 초과 시 Job 상태가 FAILED로 변경되는지 테스트")
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testSmsRateLimitExceededSetsJobToFailed() {
        // given
        int smsLimit = 5;
        int membersPerPage = 1;
        int totalMembers = smsLimit + 1; // 6명
        List<String> ageGroupsForTest = List.of("20s");

        ReflectionTestUtils.setField(messageAsyncHandler, "AGE_GROUPS", ageGroupsForTest);

        List<AgeGroupMemberDto> members = IntStream.range(0, totalMembers)
                .mapToObj(i -> new AgeGroupMemberDto("thham" + i, "0103333444" + i, 1990))
                .collect(Collectors.toList());

        when(memberRepository.findByAgeGroup(anyString(), anyInt(), any(PageRequest.class)))
                .thenAnswer(invocation -> {
                    PageRequest pageRequest = invocation.getArgument(2);
                    int pageNumber = pageRequest.getPageNumber();
                    int pageSize = pageRequest.getPageSize();

                    int start = pageNumber * pageSize;
                    int end = Math.min(start + pageSize, members.size());

                    if (start < members.size()) {
                        return new PageImpl<>(members.subList(start, end), pageRequest, members.size());
                    }
                    return new PageImpl<>(Collections.emptyList(), pageRequest, 0);
                });

        when(messageClient.sendKakaoTalkMessage(anyString(), anyString())).thenReturn(ResponseEntity.ok().build());
        when(messageClient.sendSmsMessage(anyString(), anyString())).thenReturn(ResponseEntity.ok("SMS Sent"));

        when(kakaoTalkBucket.tryConsume(1)).thenReturn(false);

        AtomicInteger smsConsumeCount = new AtomicInteger(0);
        when(smsBucket.tryConsume(1)).thenAnswer(invocation -> {
            if (smsConsumeCount.getAndIncrement() < smsLimit) {
                return true;
            }
            return false;
        });

        // when
        messageAsyncHandler.processAndSendMessagesByAgeGroup(testSendMessageDto, testMessageJob.getId());

        // then
        verify(messageClient, never()).sendKakaoTalkMessage(anyString(), anyString());
        verify(messageClient, times(smsLimit)).sendSmsMessage(anyString(), anyString());

        assertEquals(JobStatus.FAILED, testMessageJob.getStatus());
        assertNotNull(testMessageJob.getErrorMessage());
        assertTrue(testMessageJob.getErrorMessage().contains("SMS rate limit exceeded"));
    }

    @Test
    @DisplayName("카카오톡 발송 실패 시 SMS로 대체되는지 테스트 (네트워크 오류 등)")
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testKakaoTalkSendFailureFallbackToSms() {
        // given
        int totalMembers = 5;
        List<String> ageGroupsForTest = List.of("20s");
        ReflectionTestUtils.setField(messageAsyncHandler, "AGE_GROUPS", ageGroupsForTest);

        List<AgeGroupMemberDto> members = IntStream.range(0, totalMembers)
                .mapToObj(i -> new AgeGroupMemberDto("thham" + i, "0105555666" + i, 1990))
                .collect(Collectors.toList());

        when(memberRepository.findByAgeGroup(anyString(), anyInt(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(members, PageRequest.of(0, totalMembers), totalMembers))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        when(messageClient.sendKakaoTalkMessage(anyString(), anyString()))
                .thenThrow(new RuntimeException("KakaoTalk network error"));
        when(messageClient.sendSmsMessage(anyString(), anyString())).thenReturn(ResponseEntity.ok("SMS Sent"));

        when(kakaoTalkBucket.tryConsume(1)).thenReturn(true);
        when(smsBucket.tryConsume(1)).thenReturn(true);

        // when
        messageAsyncHandler.processAndSendMessagesByAgeGroup(testSendMessageDto, testMessageJob.getId());

        // then
        verify(messageClient, times(totalMembers)).sendKakaoTalkMessage(anyString(), anyString());
        verify(messageClient, times(totalMembers)).sendSmsMessage(anyString(), anyString());

        assertEquals(JobStatus.COMPLETED, testMessageJob.getStatus());
    }

    @Test
    @DisplayName("모든 메시지 발송 성공 시 Job 상태 COMPLETED")
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testAllMessagesSentSuccessfully() {
        // given
        int totalMembers = 10;
        List<String> ageGroupsForTest = List.of("0~10");
        ReflectionTestUtils.setField(messageAsyncHandler, "AGE_GROUPS", ageGroupsForTest);

        List<AgeGroupMemberDto> members = IntStream.range(0, totalMembers)
                .mapToObj(i -> new AgeGroupMemberDto("thham" + i, "0107777888" + i, 1990))
                .collect(Collectors.toList());

        when(memberRepository.findByAgeGroup(anyString(), anyInt(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(members, PageRequest.of(0, totalMembers), totalMembers))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        when(messageClient.sendKakaoTalkMessage(anyString(), anyString())).thenReturn(ResponseEntity.ok().build());
        when(messageClient.sendSmsMessage(anyString(), anyString())).thenReturn(ResponseEntity.ok("SMS Sent"));

        when(kakaoTalkBucket.tryConsume(1)).thenReturn(true);

        // when
        messageAsyncHandler.processAndSendMessagesByAgeGroup(testSendMessageDto, testMessageJob.getId());

        // then
        verify(messageClient, times(totalMembers)).sendKakaoTalkMessage(anyString(), anyString());
        verify(messageClient, never()).sendSmsMessage(anyString(), anyString());

        assertEquals(JobStatus.COMPLETED, testMessageJob.getStatus());
    }

    @Test
    @DisplayName("메시지 발송 중 예외 발생 시 Job 상태 FAILED")
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testExceptionDuringMessageSendingSetsJobToFailed() {
        // given
        int totalMembers = 1;
        List<String> ageGroupsForTest = List.of("20s");
        ReflectionTestUtils.setField(messageAsyncHandler, "AGE_GROUPS", ageGroupsForTest);

        List<AgeGroupMemberDto> members = Collections.singletonList(testMemberDto);

        when(memberRepository.findByAgeGroup(anyString(), anyInt(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(members, PageRequest.of(0, 1), totalMembers))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        when(messageClient.sendKakaoTalkMessage(anyString(), anyString()))
                .thenThrow(new RuntimeException("Critical system error during KakaoTalk send"));
        when(messageClient.sendSmsMessage(anyString(), anyString()))
                .thenThrow(new RuntimeException("SMS send failed after KakaoTalk fallback"));

        when(kakaoTalkBucket.tryConsume(1)).thenReturn(true);
        when(smsBucket.tryConsume(1)).thenReturn(true);

        // when
        messageAsyncHandler.processAndSendMessagesByAgeGroup(testSendMessageDto, testMessageJob.getId());

        // then
        verify(messageClient, times(1)).sendKakaoTalkMessage(anyString(), anyString());
        verify(messageClient, times(1)).sendSmsMessage(anyString(), anyString());

        assertEquals(JobStatus.FAILED, testMessageJob.getStatus());
        assertNotNull(testMessageJob.getErrorMessage());
        assertTrue(testMessageJob.getErrorMessage().contains("SMS send failed after KakaoTalk fallback"));
    }
}