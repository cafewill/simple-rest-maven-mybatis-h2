package com.cube.simple.model;

import com.cube.simple.aspect.AESData;

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
    private String role;

    @NotBlank(message = "아이디는 필수입니다.")
    private String id;
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
    
    @AESData
    private String name;
    @AESData
    private String phone;
    
    private String description;
}
