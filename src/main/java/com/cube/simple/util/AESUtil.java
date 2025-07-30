package com.cube.simple.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AESUtil {

    @Value("${aes.key}")
    private String key;

    private SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    public void init() {
        // 빈 생성 후 호출: keySpec 초기화
        Objects.requireNonNull(key, "AES 키가 설정되어야 합니다");
        keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
    }

    public String encrypt(String plain) {
        try {
            if (Objects.isNull(plain)) return null;
            byte[] ivBytes = new byte[16];
            secureRandom.nextBytes(ivBytes);
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[ivBytes.length + encrypted.length];
            System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
            System.arraycopy(encrypted, 0, combined, ivBytes.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("AES encrypt error", e);
            throw new RuntimeException("암호화 실패", e);
        }
    }

    public String decrypt(String cipherText) {
        try {
            if (Objects.isNull(cipherText)) return null;
            byte[] combined = Base64.getDecoder().decode(cipherText);
            byte[] ivBytes = new byte[16];
            System.arraycopy(combined, 0, ivBytes, 0, 16);
            byte[] encrypted = new byte[combined.length - 16];
            System.arraycopy(combined, 16, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(ivBytes));
            byte[] original = cipher.doFinal(encrypted);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES decrypt error", e);
            throw new RuntimeException("복호화 실패", e);
        }
    }
}
