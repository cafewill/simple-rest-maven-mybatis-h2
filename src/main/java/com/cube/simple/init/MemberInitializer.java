package com.cube.simple.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.cube.simple.model.Member;
import com.cube.simple.service.MemberService;

import jakarta.annotation.PostConstruct;

@Profile("local")
@Component
public class MemberInitializer {

    @Autowired
    private MemberService memberService;

    @PostConstruct
    public void initialize() {
        memberService.insert(Member.builder()
            .role("ADMIN")
            .id("admin")
            .password("2580")
            .name("관리자")
            .phone("010-1234-2580")
            .description("모든 권한 관리자")
            .build());
        memberService.insert(Member.builder()
            .role("HOST")
            .id("host")
            .password("8282")
            .name("판매자")
            .phone("010-1234-8282")
            .description("모든 권한 관리자")
            .build());
        memberService.insert(Member.builder()
            .role("USER")
            .id("jeju")
            .password("1234")
            .name("제주")
            .phone("010-5678-1001")
            .description("일반 권한 사용자")
            .build());
    }
}
