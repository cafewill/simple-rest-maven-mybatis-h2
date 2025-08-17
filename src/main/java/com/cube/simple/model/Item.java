package com.cube.simple.model;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {
	
	private Long id;
	
	@NotNull (message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
	@Max(value = 1000000, message = "가격은 1,000,000 이하이어야 합니다.")
    private Long price;
    
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    private String image;
    private String description;

    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
