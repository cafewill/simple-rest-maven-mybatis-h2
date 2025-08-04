package com.cube.simple;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.cube.simple.util.JWTUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ActiveProfiles("local")
class JWTUtilTests {

    @Autowired
	JWTUtil jwtUtil;

    @Test
	void contextLoads() {
	}

	@Test
	void testJWTUtilGenerateSecret() {
		
		log.info ("Check JWTUtil.generateSecret : {}", jwtUtil.generateSecret());
    }
}
