package com.cube.simple.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Demo {
	
	Long id;
	
    @NotBlank(message = "이름은 필수입니다.")
	String name;

    String image;
	String description;
}
