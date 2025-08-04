package com.cube.simple;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.cube.simple.util.AESUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ActiveProfiles("local")
class AESUtilTests {

    @Autowired
	AESUtil aesUtil;

    @Test
	void contextLoads() {
	}

	@Test
	void testAESUtilGenerateKey() throws NoSuchAlgorithmException {
		
		log.info ("Check AESUtil.generateKey : {}", aesUtil.generateKey());
	}
}
