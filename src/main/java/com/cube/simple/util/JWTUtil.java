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

    @Getter
    @Value("${jwt.secret}")
    private String base64Secret;

    @Getter
    @Value("${jwt.expiration}")
    private long expiration;

    // 실제 서명/검증에 사용할 Key 객체
    private Key signingKey;

    @PostConstruct
    public void init() {
        // 1) Base64 디코딩
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        // 2) HMAC-SHA256용 Key 객체 생성
        signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT signing key initialized ({} bytes)", keyBytes.length);
    }
    
    public String generateSecret () {

        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String base64Secret = Encoders.BASE64.encode(key.getEncoded());
        
        return base64Secret;
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(exp)
                // 개선: Key 객체와 Algorithm 지정
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                // 개선: Key 객체 직접 주입
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
