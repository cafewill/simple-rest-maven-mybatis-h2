package com.cube.simple.controller;

import java.util.Locale;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cube.simple.dto.CommonResponse;
import com.cube.simple.dto.LoginData;
import com.cube.simple.dto.LoginRequest;
import com.cube.simple.enums.ResponseCode;
import com.cube.simple.model.Member;
import com.cube.simple.service.AuthService;
import com.cube.simple.util.JWTUtil;
import com.cube.simple.util.MessageUtil;
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
// @Tag(name = "{api.operations.entity.auth}")
// @Tag(name = "Auth", description = "사용자 인증 API")
public class AuthController {

    @Value("${common.error.data:false}")
    private boolean showErrorData;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private SHAUtil shaUtil;

    @Autowired
    private AuthService authService;

    @Autowired 
    private MessageUtil messages;

    @PostMapping("/login")
    @Operation(summary = "{api.auth.summary}", description = "{api.auth.description}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "{api.response.ok}"),
            @ApiResponse(responseCode = "200", description = "{api.response.authorized}"),
            @ApiResponse(responseCode = "401", description = "{api.response.unauthorized}"),
            @ApiResponse(responseCode = "500", description = "{api.response.error}")
        })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, Locale locale) {

        Member found = authService.selectById(request.getId());
        CommonResponse response = CommonResponse.builder().build();

        if (Objects.isNull(found) 
                || !shaUtil.equals(request.getPassword(), found.getPassword())) {
            log.warn("Check login failed");
        	
        	response = CommonResponse.builder()
                    .code(ResponseCode.UNAUTHORIZED)
                    .message(messages.get("api.response.unauthorized"))
                    .data(showErrorData ? ResponseCode.UNAUTHORIZED.getMessage() : null)
                    .build();
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }        
        
        String token = jwtUtil.generateToken(found.getId(), found.getRole().name());

        LoginData payload = LoginData.builder()
                .token(token)
                .id(found.getId())
                .role(found.getRole().name())
                .build();
        
        response = CommonResponse.builder()
                .code(ResponseCode.SUCCESS)
                .message(messages.get("api.response.authorized"))
                .data(payload)
                .build();

        return ResponseEntity.ok(response);
    }
}