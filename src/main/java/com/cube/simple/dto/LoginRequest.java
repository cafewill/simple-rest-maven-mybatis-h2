package com.cube.simple.dto;

import java.time.Clock;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
	
    @NotBlank(message = "아이디는 필수입니다.")
    private String id;
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
    
    @Builder.Default  
    private final LocalDateTime timestamp = LocalDateTime.now(Clock.systemUTC());
}
