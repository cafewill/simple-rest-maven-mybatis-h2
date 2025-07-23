package com.cube.simple.controller;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cube.simple.mapper.read.ReadMemberMapper;
import com.cube.simple.model.Member;
import com.cube.simple.security.jwt.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private ReadMemberMapper readMemberMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final DigestUtils digest = new DigestUtils("SHA-256");

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Member member) {
        // 1) 아이디로 회원 조회
        Member found = readMemberMapper.selectById(member.getId());
        
        // 2) 회원 존재 및 비밀번호 검증
        if (!Objects.isNull (found) && found.getPassword().equals(digest.digestAsHex(member.getPassword()))) {
            
            // 3) JWT 생성 (sub: id, claim: role)
            String token = jwtUtil.generateToken(found.getId(), found.getRole());
            
            // 4) 토큰 반환
            return ResponseEntity.ok(Map.of("token", token));
        }
        
        // 인증 실패 시 401 Unauthorized
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }    
}