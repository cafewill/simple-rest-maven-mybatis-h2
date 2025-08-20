package com.cube.simple.controller;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cube.simple.dto.WelcomeResponse;
import com.cube.simple.enums.ResponseCode;
import com.cube.simple.util.MessageUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
// @Tag(name = "{api.operations.entity.welcome}")
// @Tag(name = "Welcome", description = "루트 접속시 기본 응답 반환함")
public class WelcomeController {

    @Autowired 
    private MessageUtil messages;

	@GetMapping ("/")
	@Operation(
		    summary     = "{api.home.summary}",
		    description = "{api.home.description}"
		)
	@ApiResponses({
	    @ApiResponse(responseCode = "200", description = "{api.response.ok}"),
	    @ApiResponse(responseCode = "302", description = "{api.response.redirect}")
	})
	public ResponseEntity<Void> home() {
	    HttpHeaders headers = new HttpHeaders();
	    headers.setLocation(URI.create("/welcome"));
	    return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
	}
	
	@GetMapping("/welcome")
	@Operation(
		    summary     = "{api.welcome.summary}",
		    description = "{api.welcome.description}"
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "{api.response.ok}", content = @Content(schema = @Schema(implementation = WelcomeResponse.class)))
	})
	public ResponseEntity<?> welcome(HttpServletRequest request, Locale locale) {
		
	    String clientIp = extractClientIp(request);
        String message = messages.get("welcome", new Object[]{clientIp});

	    WelcomeResponse<?> response = WelcomeResponse.builder()
	    										.code(ResponseCode.SUCCESS)
	                                            .message(message)
	                                            .build();
	    log.info("Check welcome => locale : {}", locale);
	    return ResponseEntity.ok(response);
	}
	
    private String extractClientIp(HttpServletRequest request) {
    	
        String clientIp = request.getRemoteAddr();
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (Objects.nonNull(xfHeader) && !xfHeader.isBlank()) {
        	clientIp = xfHeader.split(",")[0].trim();
        }
        return clientIp.replaceAll("[\n\r]", "");
    }	
}
