package com.cube.simple.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cube.simple.enums.FirebaseCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FirebaseUtil {

    private static final String CHARSET         = "UTF-8";
    private static final String FIREBASE_SERVER = "https://fcm.googleapis.com/fcm/send";

    @Value("${firebase.server.key}")
    private String serverKey;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 단일 토큰으로 푸시 알림을 전송합니다.
     * 내부적으로 {@link #send(String, String, String, String)}을 호출합니다.
     *
     * @param token 디바이스 토큰
     * @param title 알림 제목
     * @param body  알림 본문
     * @return 성공 시 {@link FirebaseCode#SUCCESS}, 실패 시 {@link FirebaseCode#FAILURE}
     */
    public FirebaseCode send(String token, String title, String body) {
        return send(token, title, body, null);
    }

    /**
     * 단일 토큰으로 푸시 알림을 전송합니다.
     *
     * @param token 디바이스 토큰
     * @param title 알림 제목
     * @param body  알림 본문
     * @param link  클릭 시 이동할 URL (null 허용)
     * @return 성공 시 {@link FirebaseCode#SUCCESS}, 실패 시 {@link FirebaseCode#FAILURE}
     */
    public FirebaseCode send(String token, String title, String body, String link) {
        try {
            String response = exec(token, title, body, link);
            Map<String, Object> json = objectMapper.readValue(response, Map.class);
            int success = Integer.parseInt(json.get("success").toString());
            return (success == 1) ? FirebaseCode.SUCCESS : FirebaseCode.FAILURE;
        } catch (Exception ex) {
            log.error("Firebase send error", ex);
            return FirebaseCode.FAILURE;
        }
    }

    /**
     * 다중 토큰으로 푸시 알림을 전송합니다.
     * 내부적으로 {@link #send(List, String, String, String)}을 호출합니다.
     *
     * @param tokens 디바이스 토큰 리스트
     * @param title  알림 제목
     * @param body   알림 본문
     * @return {@link FirebaseCode#SUCCESS}, {@link FirebaseCode#PARTIAL}, {@link FirebaseCode#FAILURE}
     */
    public FirebaseCode send(List<String> tokens, String title, String body) {
        return send(tokens, title, body, null);
    }

    /**
     * 다중 토큰으로 푸시 알림을 전송합니다.
     *
     * @param tokens 디바이스 토큰 리스트
     * @param title  알림 제목
     * @param body   알림 본문
     * @param link   클릭 시 이동할 URL (null 허용)
     * @return {@link FirebaseCode#SUCCESS}, {@link FirebaseCode#PARTIAL}, {@link FirebaseCode#FAILURE}
     */
    public FirebaseCode send(List<String> tokens, String title, String body, String link) {
        try {
            String response = exec(tokens, title, body, link);
            Map<String, Object> json = objectMapper.readValue(response, Map.class);
            int successCount = Integer.parseInt(json.get("success").toString());
            if (successCount == 0)             return FirebaseCode.FAILURE;
            if (successCount == tokens.size()) return FirebaseCode.SUCCESS;
            return FirebaseCode.PARTIAL;
        } catch (Exception ex) {
            log.error("Firebase send error", ex);
            return FirebaseCode.FAILURE;
        }
    }

    // ----------------------------------------------------------------
    // 내부 실행 로직
    // ----------------------------------------------------------------

    /**
     * 푸시 요청 페이로드를 생성하고 전송합니다.
     *
     * @param token 단일 토큰(String) 또는 토큰 리스트(List)
     * @param title 알림 제목
     * @param body  알림 본문
     * @param link  클릭 시 이동할 URL (null 허용)
     * @return FCM 서버로부터 받은 응답 JSON 문자열
     * @throws Exception 요청/응답 실패 시 예외 발생
     */
    private String exec(Object token, String title, String body, String link) throws Exception {
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("body",  body);
        notification.put("icon",  "default");
        notification.put("sound", "default");

        Map<String, Object> request = new HashMap<>();
        if (token instanceof List) {
            request.put("registration_ids", token);
        } else {
            request.put("to", token);
        }
        request.put("notification", notification);

        if (link != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("link", link);
            request.put("data", data);
        }

        String json = objectMapper.writeValueAsString(request);
        return post(json);
    }

    /**
     * HTTP POST로 FCM 서버에 요청을 전송합니다.
     *
     * @param payload JSON 페이로드
     * @return 서버 응답 본문 (JSON 문자열)
     * @throws Exception 네트워크 또는 스트림 처리 실패 시 예외 발생
     */
    private String post(String payload) throws Exception {
        URL url = new URL(FIREBASE_SERVER);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setUseCaches(false);
            conn.setReadTimeout(12_000);
            conn.setConnectTimeout(12_000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept-Charset", CHARSET);
            conn.setRequestProperty("Connection",    "keep-alive");
            conn.setRequestProperty("Cache-control",  "no-cache");
            conn.setRequestProperty("Authorization",  "key=" + serverKey);
            conn.setRequestProperty("Content-Type",   "application/json; charset=" + CHARSET);

            try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                out.write(payload.getBytes(CHARSET));
                out.flush();
            }

            try (InputStream in = new BufferedInputStream(conn.getInputStream());
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, CHARSET))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        } finally {
            conn.disconnect();
        }
    }
}
