package com.cube.simple.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import lombok.extern.slf4j.Slf4j;

@ActiveProfiles("local")
@Slf4j
@SpringBootTest
class SHAUtilUnitTests {

    private final SHAUtil sha = new SHAUtil();

    @Test
    @DisplayName("encrypt(): 64자리 소문자 16진수 해시")
    void encryptReturns64LowerHex() {
        String hex = sha.encrypt("password123!");
        assertNotNull(hex);
        assertEquals(64, hex.length(), "SHA-256 해시는 64자리여야 함");
        assertTrue(hex.matches("^[0-9a-f]{64}$"), "소문자 16진수 형식이어야 함");
    }

    @Test
    @DisplayName("encrypt(): 결정적(같은 입력 → 같은 해시)")
    void encryptIsDeterministic() {
        String h1 = sha.encrypt("same-input");
        String h2 = sha.encrypt("same-input");
        assertEquals(h1, h2);
    }

    @Test
    @DisplayName("encrypt(): 서로 다른 입력 → 서로 다른 해시(높은 확률)")
    void encryptDifferentInputsProduceDifferentHashes() {
        String h1 = sha.encrypt("input-1");
        String h2 = sha.encrypt("input-2");
        assertNotEquals(h1, h2);
    }

    @Test
    @DisplayName("equals(): 평문 vs 저장 해시 일치 시 true")
    void equalsReturnsTrueOnMatch() {
        String stored = sha.encrypt("secret");
        assertTrue(sha.equals("secret", stored));
    }

    @Test
    @DisplayName("equals(): 불일치 혹은 null 인자 시 false")
    void equalsReturnsFalseOnMismatchOrNull() {
        String stored = sha.encrypt("secret");
        assertFalse(sha.equals("SECRET", stored));   // 대소문자 차이로 다른 문자열
        assertFalse(sha.equals(null, stored));       // input null
        assertFalse(sha.equals("secret", null));     // stored null
        assertFalse(sha.equals(null, null));         // 둘 다 null
    }

    @Test
    @DisplayName("encrypt(null): IllegalArgumentException 발생")
    void encryptNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> sha.encrypt(null));
    }
}
