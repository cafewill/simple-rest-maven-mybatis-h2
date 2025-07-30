package com.cube.simple.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SHAUtil {

    private SHAUtil() {
        // Utility 클래스이므로 인스턴스화 방지
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
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("SHA-256 암호화 실패", e);
        }
    }
}
