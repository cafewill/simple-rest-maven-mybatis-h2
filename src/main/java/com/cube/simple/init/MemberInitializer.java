package com.cube.simple.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.cube.simple.enums.RoleCode;
import com.cube.simple.model.Device;
import com.cube.simple.model.Member;
import com.cube.simple.service.DeviceService;
import com.cube.simple.service.MemberService;

import jakarta.annotation.PostConstruct;

@Profile({"local", "develop"})
@Component
public class MemberInitializer {

    @Autowired
    private MemberService memberService;

    @PostConstruct
    public void initialize() {
        memberService.insert(Member.builder()
            .role(RoleCode.ADMIN)
            .id("admin")
            .password("2580")
            .name("관리자")
            .phone("010-5678-2580")
            .description("모든 권한 관리자")
            .build());
        memberService.insert(Member.builder()
            .role(RoleCode.OWNER)
            .id("owner")
            .password("8282")
            .name("사장님")
            .phone("010-5678-8282")
            .description("모든 권한 사장님")
            .build());
        memberService.insert(Member.builder()
            .role(RoleCode.USER)
            .id("user")
            .password("1234")
            .name("사용자")
            .phone("010-5678-1234")
            .description("일반 권한 사용자")
            .build());
    }
}
