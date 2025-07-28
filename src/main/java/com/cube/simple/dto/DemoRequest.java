package com.cube.simple.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoRequest <T> {
    @Valid
    @NotNull(message = "Request <T> 는 필수입니다.")
	private T data;
}
