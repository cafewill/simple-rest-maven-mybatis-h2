package com.cube.simple.util;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AESUtil {

    private static final String ALGORITHM       = "AES";
    private static final String TRANSFORMATION  = "AES/GCM/NoPadding";
    
    private static final int    KEY_LENGTH      = 256;
    private static final int    GCM_TAG_LENGTH  = 128;
    private static final int    GCM_IV_LENGTH   = 12;

    private final SecureRandom secureRandom = new SecureRandom();
    private SecretKeySpec      keySpec;

    @Value("${aes.key}")
    private String base64Key;

    /**
     * 빈 생성 후 호출되어, 프로퍼티로부터 주입된 Base64 키를 디코딩하고
     * {@link SecretKeySpec} 객체를 초기화합니다.
     *
     * @throws IllegalArgumentException 주입된 키가 Base64 형식이 아니거나
     *                                  길이가 16, 24, 32 바이트가 아닐 경우
     */
    @PostConstruct
    private void init() {
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        if (!(decoded.length == 16 || decoded.length == 24 || decoded.length == 32)) {
            throw new IllegalArgumentException(
                "AES 키 길이가 올바르지 않습니다 (16,24,32 바이트): 실제 길이=" + decoded.length
            );
        }
        this.keySpec = new SecretKeySpec(decoded, ALGORITHM);
    }

    /**
     * 기본 256비트 크기의 AES 키를 생성합니다.
     * 내부적으로 {@link #generateKey(int)}를 호출합니다.
     *
     * @return Base64로 인코딩된 AES 키 문자열 (256비트)
     * @throws NoSuchAlgorithmException AES 알고리즘을 지원하지 않을 경우 발생
     */
    public static String generateKey() throws NoSuchAlgorithmException {
        return generateKey(KEY_LENGTH);
    }

    /**
     * 지정된 크기의 AES 키를 생성합니다.
     *
     * @param keySize 생성할 키 길이 (128, 192, 256 중 하나)
     * @return Base64로 인코딩된 AES 키 문자열
     * @throws NoSuchAlgorithmException   AES 알고리즘을 지원하지 않을 경우
     * @throws IllegalArgumentException   지원하지 않는 키 길이를 요청했을 경우
     */
    public static String generateKey(int keySize) throws NoSuchAlgorithmException {
        if (!(keySize == 128 || keySize == 192 || keySize == 256)) {
            throw new IllegalArgumentException("지원하지 않는 키 길이: " + keySize);
        }
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(keySize);
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * 주어진 평문을 AES/GCM/NoPadding 모드로 암호화합니다.
     * 생성된 IV를 앞에 붙여 Base64로 인코딩된 문자열을 반환합니다.
     *
     * @param plainText 암호화할 평문 (null일 경우 null 반환)
     * @return Base64(IV + CipherText) 형식의 암호문
     * @throws RuntimeException 암호화 실패 시 발생
     */
    public String encrypt(String plainText) {
        if (plainText == null) return null;
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined,       0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("AES encrypt error", e);
            throw new RuntimeException("암호화 실패", e);
        }
    }

    /**
     * Base64(IV + CipherText) 형식의 암호문을 받아 복호화된 평문을 반환합니다.
     *
     * @param cipherText Base64(IV + CipherText) 문자열 (null일 경우 null 반환)
     * @return 복호화된 평문
     * @throws RuntimeException 복호화 실패 시 발생
     */
    public String decrypt(String cipherText) {
        if (cipherText == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(cipherText);

            byte[] iv        = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0,            iv,        0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

            byte[] original = cipher.doFinal(encrypted);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES decrypt error", e);
            throw new RuntimeException("복호화 실패", e);
        }
    }
}
