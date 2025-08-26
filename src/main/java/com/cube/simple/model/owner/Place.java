package com.cube.simple.model.owner;

import java.time.LocalDateTime;
import java.util.Map;

import org.apache.ibatis.type.Alias;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Alias("ownerPlace")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Place {
    
    private Long id;
    
    @NotBlank(message = "이름은 필수입니다.")
    private Map<String, String> name;
    private Map<String, String> description;

    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}