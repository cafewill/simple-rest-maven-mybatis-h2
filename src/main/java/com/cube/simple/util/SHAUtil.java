package com.cube.simple.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SHAUtil {

    private static final String ALGORITHM = "SHA-256";

    /**
     * 평문과 저장된 해시값을 SHA-256 방식으로 비교합니다.
     *
     * @param input Plain text (비밀번호 등)
     * @param found 비교 대상으로 저장된 64자리 16진수 해시 문자열
     * @return 둘 다 null이 아니고, 해시값이 일치하면 true
     */
    public boolean equals(String input, String found) {
        if (Objects.isNull(input) || Objects.isNull(found)) {
            return false;
        }
        return Objects.equals(found, encrypt(input));
    }

    /**
     * 입력 문자열을 SHA-256으로 해시한 뒤
     * 64자리 16진수 문자열로 반환합니다.
     *
     * @param input 해시 대상 문자열 (null일 경우 IllegalArgumentException 발생)
     * @return 64자리 16진수 해시 문자열
     * @throws IllegalArgumentException SHAUtil.encrypt의 입력이 null인 경우
     */
    public String encrypt(String input) {
        Objects.requireNonNull(input, "Input to SHAUtil.encrypt cannot be null");

        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
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
