package com.cube.simple.controller;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cube.simple.dto.WelcomeResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Tag(name = "Welcome", description = "루트 접속시 기본 응답 반환함")
public class WelcomeController {
	
	@GetMapping ("/")
    @Operation(summary = "루트 포워딩 (403 또는 404 오류 대신 기본 응답 반환함)",
	    description = "‘/’ 요청이 들어올 경우 HTTP 200 상태로 /welcome 으로 포워딩")
	@ApiResponses({
		@ApiResponse(responseCode = "302", description = "포워딩 성공")
	})
	public ResponseEntity<Void> home() {
	    HttpHeaders headers = new HttpHeaders();
	    headers.setLocation(URI.create("/welcome"));
	    return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
	}
	
	@GetMapping("/welcome")
    @Operation(summary = "환영 메시지 및 클라이언트 IP 반환",
            description = "요청 헤더를 통해 클라이언트 IP를 추출하고, WelcomeResponse 로 반환함")
	@ApiResponses({
	     @ApiResponse(responseCode = "200", description = "환영 메시지 반환 성공", content = @Content(schema = @Schema(implementation = WelcomeResponse.class)))
	})
	public ResponseEntity<?> welcome(HttpServletRequest request) {
	    String clientIp = request.getRemoteAddr();
	    WelcomeResponse response = WelcomeResponse.builder()
	    										.status(true)
	                                            .welcome(clientIp)
	                                            .build();
	    log.info("Welcome : {}", response.getWelcome());
	    return ResponseEntity.ok(response);
	}
}
