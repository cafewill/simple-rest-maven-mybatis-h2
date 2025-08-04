package com.cube.simple.dto;

import java.time.Clock;
import java.time.LocalDateTime;

import com.cube.simple.enums.ResponseCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class WelcomeResponse <T> {
	private ResponseCode code;
	private String message;
	private T data;
	
    @Builder.Default  
    // private final LocalDateTime timestamp = LocalDateTime.now();
    private final LocalDateTime timestamp = LocalDateTime.now(Clock.systemUTC());
}
