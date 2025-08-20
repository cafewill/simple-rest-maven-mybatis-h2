package com.cube.simple.util;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JWTUtil {

    /**
     * application.properties 에서 주입받은
     * Base64 인코딩된 JWT 서명용 시크릿 문자열
     */
    @Getter
    @Value("${jwt.secret}")
    private String base64Secret;

    /**
     * 토큰 만료 기간(밀리초 단위)
     */
    @Getter
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * refresh 토큰 만료 기간(밀리초 단위)
     */
    @Getter
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // 실제 서명/검증에 사용할 Key 객체
    private Key signingKey;

    /**
     * 빈 초기화 직후 호출됩니다.
     * 1) 주입된 base64Secret을 디코딩하여 바이트 배열로 변환
     * 2) HMAC-SHA256 알고리즘용 Key 객체를 생성
     * 3) 서명 키 길이를 로그로 출력
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT signing key initialized ({} bytes)", keyBytes.length);
    }

    /**
     * 새로운 HMAC-SHA256 시크릿 키를 생성하고
     * Base64 문자열로 인코딩하여 반환합니다.
     *
     * @return Base64로 인코딩된 새로운 시크릿 키
     */
    public String generateSecret() {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        return Encoders.BASE64.encode(key.getEncoded());
    }

    /**
     * 사용자명(username)과 권한(role)을 포함하는 JWT 토큰을 생성합니다.
     *
     * @param username 토큰의 Subject(주체)로 사용될 사용자명
     * @param role     토큰의 Claim으로 포함될 사용자 권한
     * @return 서명된 JWT 문자열
     */
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(exp)
                // Key 객체와 알고리즘을 지정하여 서명
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 사용자명(username)과 권한(role)을 포함하는 JWT 토큰을 생성합니다.
     *
     * @param email 토큰의 Subject(주체)로 사용될 사용자명
     * @param role     토큰의 Claim으로 포함될 사용자 권한
     * @return 서명된 JWT 문자열
     */
    public String generateRefreshToken(String email, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(exp)
                // Key 객체와 알고리즘을 지정하여 서명
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 전달된 JWT 문자열을 파싱하여 Claims를 반환합니다.
     * 유효하지 않거나 만료된 토큰인 경우 예외가 발생합니다.
     *
     * @param token 검증 및 파싱할 JWT 문자열
     * @return 토큰에 포함된 Claims 객체
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
