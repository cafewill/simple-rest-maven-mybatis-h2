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

@Profile("local")
@Component
public class MemberInitializer {

    @Autowired
    private MemberService memberService;

    @Autowired
    private DeviceService deviceService;

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

        deviceService.insert(Device.builder()
                .id("admin")
                .token("e0bonP1dTtmBiHfFPg8oHY:APA91bGQBW97r_A7OKTjV9oioPL9vK4yiQCC5b2vdq2ZS8TEbOIes0BE6_PS7TBpYU4EeEzW_TsUp6WDdErFpE4o5U57uvgwtOKL5Jpym6JaL-fwgxkcPS4")
                .build());
        deviceService.insert(Device.builder()
                .id("owner")
                .token("fT-Y7VYQRwG86djqTPwbhE:APA91bFZu6iiTYOggwXVwL-8ykHx1hsPksah3Qblrn6wUoEEB_z32T6SCI5raq29S02negwJxX-dfOdFlSKeB7Vt2n27rpncYH3uaCDMDBUIUMFx30Hr3gQ")
                .build());
        deviceService.insert(Device.builder()
                .id("user")
                .token("eCZ7SJP2TUOVz-l5aLglkj:APA91bGcVpakTwP7TtlUcJDQCatCLlTGHgjDD1YYxo8Sd4dGjOIzjdadFak-ZvKZAQWHmcveJRlswYtcqC3BUgxReWnQqXfVvRhtFB9v3deCiKAtTbdzFGw")
                .build());
    }
}
