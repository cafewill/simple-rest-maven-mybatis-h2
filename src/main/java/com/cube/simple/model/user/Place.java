package com.cube.simple.model.user;

import java.time.LocalDateTime;

import org.apache.ibatis.type.Alias;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Alias("userPlace")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Place {
    
    private Long id;
    
    @NotBlank(message = "이름은 필수입니다.")
    private String name;
    private String description;

    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}