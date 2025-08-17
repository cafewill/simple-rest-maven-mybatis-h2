package com.cube.simple.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import lombok.extern.slf4j.Slf4j;

@ActiveProfiles("local")
@Slf4j
@SpringBootTest
class SHAUtilIntegrationTests {

    @Autowired
    private SHAUtil shaUtil;

    @Test
    @DisplayName("통합: encrypt() 64자리 소문자 16진수")
    void encryptReturns64LowerHex() {
        String hex = shaUtil.encrypt("integration-test");
        assertNotNull(hex);
        assertEquals(64, hex.length());
        assertTrue(hex.matches("^[0-9a-f]{64}$"));
    }

    @Test
    @DisplayName("통합: equals() 일치/불일치 검증")
    void equalsMatchAndMismatch() {
        String stored = shaUtil.encrypt("hello-sha");
        assertTrue(shaUtil.equals("hello-sha", stored));
        assertFalse(shaUtil.equals("hello-sha!", stored));
    }
}
