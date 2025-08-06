package com.cube.simple.util;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * AES-256 암호화를 위한 유틸리티 클래스.
 * CBC 운영 모드와 PKCS5Padding을 사용합니다.
 */
@Slf4j
public final class AESUtil {

	@Getter
	@Setter
    private static String base64Key;

    private static final int KEY_LENGTH = 256;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH  = 12;
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final SecureRandom secureRandom = new SecureRandom();

    
    /**
     * 외부에서 객체 생성을 방지하기 위한 private 생성자
     */
    private AESUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 필요시 256비트 AES 키를 생성합니다.
     * 생성된 키는 Base64로 인코딩된 문자열입니다.
     *
     * @return Base64로 인코딩된 AES 키 문자열
     * @throws NoSuchAlgorithmException AES 알고리즘을 지원하지 않을 경우 발생
     */
    public static String generateKey() throws NoSuchAlgorithmException {
        return generateKey(KEY_LENGTH);
    }
    public static String generateKey(int keySize) throws NoSuchAlgorithmException {
        
    	if (!(keySize == 128 || keySize == 192 || keySize == 256)) {
            throw new IllegalArgumentException("지원하지 않는 키 길이: " + keySize);
        }    	
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(keySize); // 128, 192, 256 중 선택
        
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * 문자열을 AES 알고리즘으로 암호화합니다.
     *
     * @param base64Key Base64로 인코딩된 AES 키
     * @param plainText 암호화할 평문
     * @return Base64로 인코딩된 암호문 (IV + 암호문)
     */
    public static String encrypt(String plainText) {
        try {
            if (Objects.isNull(plainText)) return null;

            SecretKeySpec keySpec = getKeySpec(getBase64Key ());

            // IV (Initialization Vector) 생성
            byte[] ivBytes = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(ivBytes);
            // IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, ivBytes);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // IV와 암호문을 합쳐서 Base64로 인코딩
            byte[] combined = new byte[ivBytes.length + encrypted.length];
            System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
            System.arraycopy(encrypted, 0, combined, ivBytes.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception ex) {
            // 실제 프로덕션 코드에서는 로깅 라이브러리를 사용하는 것이 좋습니다.
            // 예: log.error("AES encrypt error", ex);
        	log.error("AES encrypt error", ex);
            throw new RuntimeException("암호화에 실패했습니다.", ex);
        }
    }

    /**
     * AES로 암호화된 문자열을 복호화합니다.
     *
     * @param base64Key  Base64로 인코딩된 AES 키
     * @param cipherText Base64로 인코딩된 암호문 (IV + 암호문)
     * @return 복호화된 평문
     */
    public static String decrypt(String cipherText) {
        try {
            if (Objects.isNull(cipherText)) return null;

            SecretKeySpec keySpec = getKeySpec(getBase64Key ());
            byte[] combined = Base64.getDecoder().decode(cipherText);

            // IV와 암호문 분리
            byte[] ivBytes = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, ivBytes, 0, GCM_IV_LENGTH);
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            // cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(ivBytes));
            byte[] original = cipher.doFinal(encrypted);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            // 실제 프로덕션 코드에서는 로깅 라이브러리를 사용하는 것이 좋습니다.
            // 예: log.error("AES decrypt error", ex);
        	log.error("AES decrypt error", ex);
            throw new RuntimeException("복호화에 실패했습니다.", ex);
        }
    }
    
    /**
     * Base64 키 문자열로부터 SecretKeySpec 객체를 생성하고 유효성을 검증합니다.
     */
    private static SecretKeySpec getKeySpec(String encryptedKey) {
        Objects.requireNonNull(encryptedKey, "AES 키가 설정되어야 합니다.");
        byte[] decodedKey;
        try {
            decodedKey = Base64.getDecoder().decode(encryptedKey);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("AES 키가 Base64 형식이 아닙니다: ", ex);
        }

        // 키 길이 검증 (16, 24, 32바이트만 허용)
        if (!(decodedKey.length == 16 || decodedKey.length == 24 || decodedKey.length == 32)) {
            throw new IllegalArgumentException(
                "AES 키 길이가 올바르지 않습니다 (16, 24, 32바이트여야 함): 실제 길이=" + decodedKey.length
            );
        }
        return new SecretKeySpec(decodedKey, ALGORITHM);
    }
}