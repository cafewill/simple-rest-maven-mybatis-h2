package com.cube.simple.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;

@ActiveProfiles("local")
@Slf4j
@SpringBootTest
class JWTUtilUnitTests {

    /**
     * 지정 바이트 배열을 Base64로 인코딩해 jwt.secret에 주입하고,
     * expiration(밀리초)도 함께 주입한 뒤 init()을 호출해 초기화한다.
     */
    private JWTUtil createJwtUtilWithKey(byte[] rawKey, long expirationMillis) {
        String base64 = Base64.getEncoder().encodeToString(rawKey);
        JWTUtil util = new JWTUtil();
        ReflectionTestUtils.setField(util, "base64Secret", base64);
        ReflectionTestUtils.setField(util, "expiration",  expirationMillis);
        ReflectionTestUtils.invokeMethod(util, "init");
        return util;
    }

    /**
     * 길이만큼 0x00으로 채운 바이트 배열
     */
    private byte[] zeros(int length) {
        return new byte[length];
    }

    @Test
    @DisplayName("토큰 생성/파싱: subject, role, 만료시간이 정상적으로 셋업된다")
    void generateAndParseTokenOk() {
        JWTUtil jwt = createJwtUtilWithKey(zeros(32), 5_000); // 256비트 키, 5초 만료

        String username = "jeju";
        String role     = "USER";
        String token    = jwt.generateToken(username, role);

        assertNotNull(token);

        Claims claims = jwt.parseToken(token);
        assertEquals(username, claims.getSubject());
        assertEquals(role, claims.get("role"));

        // iat <= now <= exp 인지 대략 검증
        Date now = new Date();
        assertTrue(claims.getIssuedAt().getTime() <= now.getTime());
        assertTrue(claims.getExpiration().getTime() > now.getTime());
    }

    @Test
    @DisplayName("만료 토큰: expiration을 음수로 설정하면 즉시 만료 처리되어 예외 발생")
    void expiredTokenThrows() {
        // exp = now + (-1000ms) => 과거시간
        JWTUtil jwt = createJwtUtilWithKey(zeros(32), -1_000);

        String token = jwt.generateToken("bill", "ADMIN");
        assertThrows(ExpiredJwtException.class, () -> jwt.parseToken(token));
    }

    @Test
    @DisplayName("서명 변조: 토큰의 서명 부분을 바꾸면 파싱 시 JwtException 발생")
    void tamperedSignatureThrows() {
        JWTUtil jwt = createJwtUtilWithKey(zeros(32), 10_000);
        String token = jwt.generateToken("hana", "MANAGER");

        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT는 header.payload.signature 3부분이어야 함");

        // 서명 부분(signature) 마지막 문자를 안전한 문자로 바꿔치기 (Base64URL 유효 범위 내)
        String sig = parts[2];
        char last = sig.charAt(sig.length() - 1);
        char replaced = (last == 'A') ? 'B' : 'A';
        String tamperedSignature = sig.substring(0, sig.length() - 1) + replaced;
        String tampered = parts[0] + "." + parts[1] + "." + tamperedSignature;

        assertThrows(JwtException.class, () -> jwt.parseToken(tampered),
                "서명이 변조된 토큰은 파싱 시 예외가 발생해야 함");
    }

    @Test
    @DisplayName("generateSecret(): HS256 시크릿(Base64) 생성 및 길이 검증(>= 32바이트)")
    void generateSecretOk() {
        JWTUtil jwt = createJwtUtilWithKey(zeros(32), 3_000);
        String b64 = jwt.generateSecret();
        assertNotNull(b64);

        byte[] raw = Base64.getDecoder().decode(b64);
        assertTrue(raw.length >= 32, "HS256 시크릿은 최소 256비트(32바이트) 이상이어야 함");
    }

    @Test
    @DisplayName("role 클레임 미스매치: 생성 시 넣은 값과 동일해야 한다")
    void roleClaimConsistencyOk() {
        JWTUtil jwt = createJwtUtilWithKey(zeros(32), 3_000);
        String token = jwt.generateToken("mimi", "USER");
        Claims claims = jwt.parseToken(token);
        assertEquals("USER", claims.get("role"));
        assertNotEquals("ADMIN", claims.get("role"));
    }
}
