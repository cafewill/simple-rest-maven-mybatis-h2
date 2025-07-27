package com.cube.simple.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;

@Component
public class JwtUtil {
	
    @Getter
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Getter
    @Value("${jwt.expiration}")
    private long expiration;

	public String generateToken(String username, String role) {
        return Jwts.builder()
            .setSubject(username)
            .claim("role", role)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + getExpiration ()))
            .signWith(SignatureAlgorithm.HS256, getSecretKey ())
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody();
    }
}