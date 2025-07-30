package com.cube.simple.init;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cube.simple.mapper.write.WriteMemberMapper;
import com.cube.simple.model.Member;

import jakarta.annotation.PostConstruct;

@Component
public class MemberInitializer {

    @Autowired
    private WriteMemberMapper writeMemberMapper;

    @PostConstruct
    public void initialize() {
        writeMemberMapper.insert(Member.builder()
            .role("ADMIN")
            .id("admin")
            .password(encryptSHA("2580"))
            .name("관리자")
            .description("모든 권한 관리자")
            .build());
        writeMemberMapper.insert(Member.builder()
                .role("HOST")
                .id("host")
                .password(encryptSHA("8282"))
                .name("판매자")
                .description("모든 권한 관리자")
                .build());
        writeMemberMapper.insert(Member.builder()
            .role("USER")
            .id("jeju")
            .password(encryptSHA("1234"))
            .name("제주")
            .description("일반 권한 사용자")
            .build());
        writeMemberMapper.insert(Member.builder()
                .role("USER")
                .id("bill")
                .password(encryptSHA("1234"))
                .name("빌")
                .description("일반 권한 사용자")
                .build());
        writeMemberMapper.insert(Member.builder()
            .role("USER")
            .id("steve")
            .password(encryptSHA("1234"))
            .name("스티브")
            .description("일반 권한 사용자")
            .build());
    }

    private String encryptSHA(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("암호화 실패", e);
        }
    }
}
