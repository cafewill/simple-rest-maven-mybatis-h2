package com.cube.simple.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cube.simple.enums.FirebaseCode;
import com.cube.simple.util.FirebaseUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class FirebaseUtilIntegrationTests {

    @Autowired
    private FirebaseUtil firebaseUtil; // @Autowired로 FirebaseUtil 빈을 주입받습니다.

    private static final String USER_TOKEN = "eCZ7SJP2TUOVz-l5aLglkj:APA91bGcVpakTwP7TtlUcJDQCatCLlTGHgjDD1YYxo8Sd4dGjOIzjdadFak-ZvKZAQWHmcveJRlswYtcqC3BUgxReWnQqXfVvRhtFB9v3deCiKAtTbdzFGw";
    private static final String OWNER_TOKEN = "fT-Y7VYQRwG86djqTPwbhE:APA91bFZu6iiTYOggwXVwL-8ykHx1hsPksah3Qblrn6wUoEEB_z32T6SCI5raq29S02negwJxX-dfOdFlSKeB7Vt2n27rpncYH3uaCDMDBUIUMFx30Hr3gQ";
    private static final String ADMIN_TOKEN = "e0bonP1dTtmBiHfFPg8oHY:APA91bGQBW97r_A7OKTjV9oioPL9vK4yiQCC5b2vdq2ZS8TEbOIes0BE6_PS7TBpYU4EeEzW_TsUp6WDdErFpE4o5U57uvgwtOKL5Jpym6JaL-fwgxkcPS4";

    private String testTitle;
    private String testBody;
    private String testLink;

    @BeforeEach
    void setUp() {
        // 각 테스트가 실행되기 전에 공통 데이터를 설정합니다.
        Date now = new Date();
        testTitle = String.format("DEMO %s", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss #SSS").format(now));
        testBody = "DEMO Some message here!";
        testLink = String.format("https://github.com/cafewill?%s", new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now));
        
        log.info("테스트 데이터 생성 완료: title={}", testTitle);
    }

    @Test
    @DisplayName("단일 토큰으로 FCM 메시지 전송 테스트")
    void testSendSingleToken() {
    	
    	String token = ADMIN_TOKEN;
    	
        // given
        log.info("단일 전송 테스트 시작: token={}", token);

        // when
        FirebaseCode response = firebaseUtil.send(token, testTitle, testBody, null, testLink);

        // then
        log.info("전송 결과: {}", response);
        assertNotNull(response, "응답 객체는 null이 아니어야 합니다.");
        assertEquals(FirebaseCode.SUCCESS, response, "메시지 전송은 성공해야 합니다.");
    }

    @Test
    @DisplayName("여러 토큰으로 FCM 멀티캐스트 메시지 전송 테스트")
    void testSendMultipleTokens() {
    	
        // given
        List<String> tokens = Arrays.asList(USER_TOKEN, OWNER_TOKEN, ADMIN_TOKEN);
        log.info("멀티캐스트 전송 테스트 시작: {}개의 토큰", tokens.size());

        // when
        FirebaseCode response = firebaseUtil.send(tokens, testTitle, testBody);

        // then
        log.info("전송 결과: {}", response);
        assertNotNull(response, "응답 객체는 null이 아니어야 합니다.");
        assertEquals(FirebaseCode.SUCCESS, response, "모든 메시지 전송은 성공해야 합니다.");
    }
}