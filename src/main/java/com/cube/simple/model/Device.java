package com.cube.simple.model;

import java.time.LocalDateTime;

import com.cube.simple.aspect.AESData;
import com.cube.simple.aspect.SHAData;
import com.cube.simple.enums.RoleCode;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {
	
    private Long seq;
    
    @NotBlank(message = "아이디는 필수입니다.")
    private String id;

    @NotBlank(message = "알림 토큰은 필수입니다.")
    private String token;
    
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
