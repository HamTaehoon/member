package com.thham.survey.common.client.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

@Slf4j
@Component
public class MessageClient {

    private final RestClient restClient;
    private final String kakaoApiUrl;
    private final String smsApiUrl;
    private final String kakaoUsername;
    private final String kakaoPassword;
    private final String smsUsername;
    private final String smsPassword;

    public MessageClient(
            RestClient restClient,
            @Value("${kakao.api.url:http://localhost:8081}") String kakaoApiUrl,
            @Value("${sms.api.url:http://localhost:8082}") String smsApiUrl,
            @Value("${kakao.api.username:autoever}") String kakaoUsername,
            @Value("${kakao.api.password:1234}") String kakaoPassword,
            @Value("${sms.api.username:autoever}") String smsUsername,
            @Value("${sms.api.password:5678}") String smsPassword
    ) {
        this.restClient = restClient;
        this.kakaoApiUrl = kakaoApiUrl;
        this.smsApiUrl = smsApiUrl;
        this.kakaoUsername = kakaoUsername;
        this.kakaoPassword = kakaoPassword;
        this.smsUsername = smsUsername;
        this.smsPassword = smsPassword;
    }

    public ResponseEntity<Void> sendKakaoTalkMessage(String phone, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((kakaoUsername + ":" + kakaoPassword).getBytes(StandardCharsets.UTF_8)));
        Object body = Collections.singletonMap("message", message);

        log.info("KakaoTalk POST to {} with headers: {}, body: {}", kakaoApiUrl + "/kakaotalk-messages?phone=" + phone, headers, body);

        /*
        restClient.post()
                .uri(kakaoApiUrl + "/kakaotalk-messages?phone=" + phone)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(headers))
                .body(body)
                .retrieve()
                .toBodilessEntity();
        */

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<String> sendSmsMessage(String phone, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((smsUsername + ":" + smsPassword).getBytes(StandardCharsets.UTF_8)));
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("message", message);

        log.info("SMS POST to {} with headers: {}, body: {}", smsApiUrl + "/sms?phone=" + phone, headers, body);

        /*
        restClient.post()
                .uri(smsApiUrl + "/sms?phone=" + phone)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(h -> h.addAll(headers))
                .body(body)
                .retrieve()
                .toEntity(String.class);
        */

        return new ResponseEntity<>("{\"result\": \"OK\"}", HttpStatus.OK);
    }
}