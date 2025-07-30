package com.cube.simple.controller;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cube.simple.dto.LoginRequest;
import com.cube.simple.dto.LoginResponse;
import com.cube.simple.model.Member;
import com.cube.simple.service.AuthService;
import com.cube.simple.util.JWTUtil;
import com.cube.simple.util.SHAUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
// @Tag(name = "Auth", description = "사용자 인증 API")
public class AuthController {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "{api.auth.summary}", description = "{api.auth.description}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "{api.responses.ok}"),
            @ApiResponse(responseCode = "401", description = "{api.responses.unauthorized}"),
            @ApiResponse(responseCode = "500", description = "{api.responses.error}")
        })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("로그인 시도: {}", request.getId());

        Member found = authService.selectById(request.getId());

        log.info("Check : id {}, password [{}] (found [{}])", request.getId (), SHAUtil.encrypt(request.getPassword()), found.getPassword ());

        if (Objects.nonNull (found) && !Objects.equals(found.getPassword(), SHAUtil.encrypt(request.getPassword()))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{api.responses.unauthorized}");
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