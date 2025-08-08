package com.cube.simple.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import lombok.extern.slf4j.Slf4j;

@ActiveProfiles("local")
@Slf4j
@SpringBootTest
@TestPropertySource(properties = {
        // 32바이트(256bit) 제로키를 Base64로 인코딩 → aes.key 로 주입
        // (운영에선 제로키 금지! 테스트 용도)
        "aes.key=" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=" // ← 32바이트 제로(Base64: 길이 44)
})
class AESUtilIntegrationTests {

    @Autowired
    private AESUtil aesUtil;

    @Test
    @DisplayName("통합: 암호화→복호화 라운드트립")
    void encryptDecryptRoundTripOk() {
        String plain = "Hello, Integration!";
        String cipher = aesUtil.encrypt(plain);
        assertNotNull(cipher);

        String recovered = aesUtil.decrypt(cipher);
        assertEquals(plain, recovered);
    }

    @Test
    @DisplayName("통합: 변조 시 복호화 실패")
    void tamperCiphertextFails() {
        String cipher = aesUtil.encrypt("check");
        byte[] raw = Base64.getDecoder().decode(cipher);
        raw[raw.length - 1] ^= 0x01;
        String tampered = Base64.getEncoder().encodeToString(raw);

        assertThrows(RuntimeException.class, () -> aesUtil.decrypt(tampered));
    }
}
