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

import com.cube.simple.dto.LoginRequest;
import com.cube.simple.dto.LoginResponse;
import com.cube.simple.mapper.read.ReadMemberMapper;
import com.cube.simple.model.Member;
import com.cube.simple.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "사용자 인증 CRUD API")  // 전체 컨트롤러에 태그 지정
public class AuthController {

    @Autowired
    private ReadMemberMapper readMemberMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final DigestUtils digest = new DigestUtils("SHA-256");

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "ID/PW 인증 후 JWT 반환함")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공, JWT 반환"),
            @ApiResponse(responseCode = "401", description = "ID 또는 비밀번호 불일치"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
        })
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.info("로그인 시도: {}", request.getId());

        Member found = readMemberMapper.selectById(request.getId());

        log.info("Check : id {}, password [{}] (found [{}])", request.getId (), digest.digestAsHex(request.getPassword()), found.getPassword ());

        if (Objects.nonNull (found) && !Objects.equals(found.getPassword(), digest.digestAsHex(request.getPassword()))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ID 또는 비밀번호 불일치");
        }

        String token = jwtUtil.generateToken(found.getId(), found.getRole());

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .id(found.getId())
                .role(found.getRole())
                .build();

        return ResponseEntity.ok(response);
    }
}