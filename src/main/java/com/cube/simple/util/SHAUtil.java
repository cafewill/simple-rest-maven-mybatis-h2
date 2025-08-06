package com.cube.simple.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SHAUtil {

    /**
     * 외부에서 객체 생성을 방지하기 위한 private 생성자
     */
    private SHAUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 평문 입력값을 SHA-256으로 암호화한 뒤,
     * 이미 저장된 해시값(found)과 동일한지 비교합니다.
     *
     * @param input Plain text password
     * @param found Stored SHA-256 hash
     * @return 둘 다 null이 아니고, 암호화 결과가 일치하면 true
     */
    public static boolean equals(String input, String found) {

        if (Objects.isNull(input) || Objects.isNull(found)) {
            return false;
        }
        
        return Objects.equals(found, encrypt(input));
    }
    
    /**
     * 입력 문자열을 SHA-256 방식으로 해시한 16진수 문자열을 반환합니다.
     *
     * @param input 해시할 문자열 (null인 경우 IllegalArgumentException 발생)
     * @return 64자리 16진수 해시 문자열
     */
    public static String encrypt(String input) {
    	
        Objects.requireNonNull(input, "Input to SHAUtil.encryptSHA cannot be null");

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            log.error("SHA-256 algorithm not available", ex);
            throw new RuntimeException("SHA-256 암호화 실패", ex);
        }
    }
}
