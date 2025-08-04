package com.cube.simple.util;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
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
    private String base64Key;

    private SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    public void init() {
        Objects.requireNonNull(base64Key, "AES 키가 설정되어야 합니다");
        byte[] decodedKey;
        try {
            // Base64 디코딩
            decodedKey = Base64.getDecoder().decode(base64Key);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("AES 키가 Base64 형식이 아닙니다: " + base64Key, ex);
        }
        // 키 길이 검증 (16, 24, 32바이트만 허용)
        if (!(decodedKey.length == 16 || decodedKey.length == 24 || decodedKey.length == 32)) {
            throw new IllegalArgumentException(
                "AES 키 길이가 올바르지 않습니다 (16, 24, 32바이트여야 함): 실제 길이=" + decodedKey.length
            );
        }
        keySpec = new SecretKeySpec(decodedKey, "AES");
    }
    
    // 필요시 키 생성용
	public String generateKey() throws NoSuchAlgorithmException {
		
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // 128, 192, 256 중 선택
        SecretKey secretKey = keyGen.generateKey();
        String base64Key = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        
        return base64Key;
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
