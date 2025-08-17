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
public class Member {
	
    private Long seq;
    
    @Builder.Default
    private RoleCode role = RoleCode.USER;

    @NotBlank(message = "아이디는 필수입니다.")
    private String id;

    @SHAData
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
    
    @AESData
    private String name;
    @AESData
    private String phone;
    
    private String description;
    
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
