package com.cube.simple.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import lombok.extern.slf4j.Slf4j;

@ActiveProfiles("local")
@Slf4j
@SpringBootTest
class AESUtilUnitTests {

    /**
     * rawKey(바이트 배열)를 Base64로 인코딩해 aes.key에 주입하고 init() 호출
     */
    private AESUtil createAesUtilWithKey(byte[] rawKey) {
        String base64 = Base64.getEncoder().encodeToString(rawKey);
        AESUtil aes = new AESUtil();
        ReflectionTestUtils.setField(aes, "base64Key", base64);
        ReflectionTestUtils.invokeMethod(aes, "init");
        return aes;
    }

    private byte[] zeros(int length) {
        return new byte[length];
    }

    @Test
    @DisplayName("암호화→복호화 라운드트립: 원문 복원")
    void encryptDecryptRoundTripOk() {
        AESUtil aes = createAesUtilWithKey(zeros(32)); // 256비트
        String plain = "안녕 Jeju-Yeora! 12345 ~!@#";
        String cipher = aes.encrypt(plain);
        assertNotNull(cipher);

        String recovered = aes.decrypt(cipher);
        assertEquals(plain, recovered);
    }

    @Test
    @DisplayName("동일 평문도 IV 랜덤성으로 서로 다른 암호문 생성")
    void encryptSamePlaintextProducesDifferentCiphertexts() {
        AESUtil aes = createAesUtilWithKey(zeros(32));
        String plain = "same text";
        String c1 = aes.encrypt(plain);
        String c2 = aes.encrypt(plain);
        assertNotEquals(c1, c2);
    }

    @Test
    @DisplayName("잘못된 키로 복호화 시 예외 발생")
    void decryptWithWrongKeyFails() {
        AESUtil aes1 = createAesUtilWithKey(zeros(32));
        AESUtil aes2 = createAesUtilWithKey(new byte[]{
                1,2,3,4,5,6,7,8, 9,10,11,12,13,14,15,16,
                17,18,19,20,21,22,23,24, 25,26,27,28,29,30,31,32
        });

        String cipher = aes1.encrypt("top-secret");
        assertThrows(RuntimeException.class, () -> aes2.decrypt(cipher));
    }

    @Test
    @DisplayName("null 입력 처리")
    void nullHandlingOk() {
        AESUtil aes = createAesUtilWithKey(zeros(16)); // 128비트
        assertNull(aes.encrypt(null));
        assertNull(aes.decrypt(null));
    }

    @Test
    @DisplayName("잘못된 키 길이(20바이트)는 init에서 IllegalArgumentException")
    void invalidKeyLengthThrows() {
        AESUtil aes = new AESUtil();
        byte[] bad = new byte[20];
        String base64 = Base64.getEncoder().encodeToString(bad);
        ReflectionTestUtils.setField(aes, "base64Key", base64);
        assertThrows(IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(aes, "init"));
    }

    @Test
    @DisplayName("암호문 변조 시 GCM 인증 실패")
    void tamperCiphertextFails() {
        AESUtil aes = createAesUtilWithKey(zeros(32));
        String cipher = aes.encrypt("integrity-check");

        byte[] raw = Base64.getDecoder().decode(cipher);
        raw[raw.length - 1] ^= 0x01; // 마지막 바이트 변조
        String tampered = Base64.getEncoder().encodeToString(raw);

        assertThrows(RuntimeException.class, () -> aes.decrypt(tampered));
    }

    @Test
    @DisplayName("generateKey(128/192/256) 길이 검증")
    void generateKeySizesOk() throws NoSuchAlgorithmException {
        String k128 = AESUtil.generateKey(128);
        String k192 = AESUtil.generateKey(192);
        String k256 = AESUtil.generateKey(256);

        assertEquals(16, Base64.getDecoder().decode(k128).length);
        assertEquals(24, Base64.getDecoder().decode(k192).length);
        assertEquals(32, Base64.getDecoder().decode(k256).length);
    }

    @Test
    @DisplayName("generateKey() 기본값 256비트")
    void generateKeyDefault256Ok() throws NoSuchAlgorithmException {
        String k = AESUtil.generateKey();
        assertEquals(32, Base64.getDecoder().decode(k).length);
    }
}
