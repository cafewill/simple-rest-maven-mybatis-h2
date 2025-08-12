package com.cube.simple.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.cube.simple.enums.FirebaseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import lombok.extern.slf4j.Slf4j;

@ActiveProfiles("local")
@Slf4j
@SpringBootTest
class FirebaseUtilUnitTests {

    private HttpServer server;

    private FirebaseUtilOld createFirebaseUtil(String baseUrl) {
        FirebaseUtilOld util = new FirebaseUtilOld();
        ReflectionTestUtils.setField(util, "firebaseServerUrl", baseUrl);
        ReflectionTestUtils.setField(util, "serverKey", "fake-server-key");
        ReflectionTestUtils.setField(util, "objectMapper", new ObjectMapper());
        return util;
    }

    private String startMockServerReturning(int status, String json) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/fcm/send", exchange -> handle(exchange, status, json));
        server.start();
        int port = server.getAddress().getPort();
        return "http://127.0.0.1:" + port + "/fcm/send";
    }

    private void handle(HttpExchange exchange, int status, String json) throws IOException {
        byte[] body = json.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
    }

    @Test
    @DisplayName("단일 토큰: success=1 → SUCCESS")
    void sendSingleTokenSuccessOk() throws Exception {
        String url = startMockServerReturning(200, "{\"success\":1}");
        FirebaseUtilOld util = createFirebaseUtil(url);
        assertEquals(FirebaseCode.SUCCESS, util.send("t1", "제목", "본문"));
    }

    @Test
    @DisplayName("단일 토큰: success=0 → FAILURE")
    void sendSingleTokenFailureOk() throws Exception {
        String url = startMockServerReturning(200, "{\"success\":0}");
        FirebaseUtilOld util = createFirebaseUtil(url);
        assertEquals(FirebaseCode.FAILURE, util.send("t1", "제목", "본문", "https://example.com"));
    }

    @Test
    @DisplayName("다중 토큰: 모두 성공 → SUCCESS")
    void sendMultiTokenAllSuccessOk() throws Exception {
        String url = startMockServerReturning(200, "{\"success\":3}");
        FirebaseUtilOld util = createFirebaseUtil(url);
        assertEquals(FirebaseCode.SUCCESS, util.send(List.of("a","b","c"), "제목", "본문"));
    }

    @Test
    @DisplayName("다중 토큰: 일부 성공 → PARTIAL")
    void sendMultiTokenPartialOk() throws Exception {
        String url = startMockServerReturning(200, "{\"success\":2}");
        FirebaseUtilOld util = createFirebaseUtil(url);
        assertEquals(FirebaseCode.PARTIAL, util.send(List.of("a","b","c"), "제목", "본문", "https://link"));
    }

    @Test
    @DisplayName("다중 토큰: 전부 실패 → FAILURE")
    void sendMultiTokenAllFailureOk() throws Exception {
        String url = startMockServerReturning(200, "{\"success\":0}");
        FirebaseUtilOld util = createFirebaseUtil(url);
        assertEquals(FirebaseCode.FAILURE, util.send(List.of("a","b"), "제목", "본문"));
    }

    @Test
    @DisplayName("서버 오류(5xx) → FAILURE")
    void sendServerErrorReturnsFailureOk() throws Exception {
        String url = startMockServerReturning(500, "{\"error\":\"boom\"}");
        FirebaseUtilOld util = createFirebaseUtil(url);
        assertEquals(FirebaseCode.FAILURE, util.send("t1", "제목", "본문"));
    }
}
