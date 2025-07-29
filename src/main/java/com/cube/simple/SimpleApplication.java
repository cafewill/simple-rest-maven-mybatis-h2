package com.cube.simple;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

/*
@OpenAPIDefinition(
		  info = @Info(
		    title       = "제주여라 백엔드 API",
		    version     = "v1.0",
		    description = "제주여라 서비스의 백엔드 API 명세입니다.",
		    contact     = @Contact(
		      name = "제주여라 개발팀",
		      url  = "https://www.instagram.com/jeju_yeora",
		      email= ""
		    )
		  )
		)
*/
@SpringBootApplication
@EnableAutoConfiguration (exclude = {ErrorMvcAutoConfiguration.class})
public class SimpleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimpleApplication.class, args);
	}

}
