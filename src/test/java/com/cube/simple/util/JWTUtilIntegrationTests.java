package com.cube.simple.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;

@ActiveProfiles("local")
@Slf4j
@SpringBootTest
@TestPropertySource(properties = {
        // 32바이트(256bit) ASCII 문자열 "12345678901234567890123456789012" 의 Base64
        // HS256 안전 기준(>=32바이트)에 부합
        "jwt.secret=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=",
        "jwt.expiration=3000" // 3초
})
class JWTUtilIntegrationTests {

    @Autowired
    private JWTUtil jwtUtil;

    private Long originalExpiration; // 변경 후 원복용

    @AfterEach
    void restoreExpirationIfChanged() {
        if (originalExpiration != null) {
            ReflectionTestUtils.setField(jwtUtil, "expiration", originalExpiration);
            originalExpiration = null;
        }
    }

    @Test
    @DisplayName("통합: 토큰 생성/파싱 정상 동작 (subject/role/만료)")
    void generateAndParseTokenOk() {
        String username = "jeju";
        String role = "USER";

        String token = jwtUtil.generateToken(username, role);
        assertNotNull(token);

        Claims claims = jwtUtil.parseToken(token);
        assertEquals(username, claims.getSubject());
        assertEquals(role, claims.get("role"));

        Date now = new Date();
        assertTrue(claims.getIssuedAt().getTime() <= now.getTime());
        assertTrue(claims.getExpiration().getTime() > now.getTime());
    }

    @Test
    @DisplayName("통합: 만료 토큰이면 ExpiredJwtException 발생")
    void expiredTokenThrows() {
        // 컨텍스트 재기동 없이, 테스트 범위에서 만료시간만 잠시 음수로 바꿔서 즉시 만료 토큰 생성
        originalExpiration = (Long) ReflectionTestUtils.getField(jwtUtil, "expiration");
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);

        String token = jwtUtil.generateToken("bill", "ADMIN");
        assertThrows(ExpiredJwtException.class, () -> jwtUtil.parseToken(token));
    }

    @Test
    @DisplayName("통합: 서명 변조 시 JwtException 발생")
    void tamperedSignatureThrows() {
        String token = jwtUtil.generateToken("hana", "MANAGER");
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);

        String sig = parts[2];
        char last = sig.charAt(sig.length() - 1);
        char replaced = (last == 'A') ? 'B' : 'A';
        String tampered = parts[0] + "." + parts[1] + "." + sig.substring(0, sig.length() - 1) + replaced;

        assertThrows(JwtException.class, () -> jwtUtil.parseToken(tampered));
    }
}
