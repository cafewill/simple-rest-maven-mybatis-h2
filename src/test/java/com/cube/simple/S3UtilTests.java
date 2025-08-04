package com.cube.simple;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.cube.simple.util.S3Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ActiveProfiles("local")
class S3UtilTests {

    @Autowired
	S3Util s3Client;

    @Test
	void contextLoads() {
	}

	@Test
	void testS3UtilList() {
		
		String prefix = "demo/";
		List <String> files = s3Client.list (prefix);
		
		log.info ("Check S3Util.list : {}", files);
	}

	@Test
	void testS3UtilUpload() throws IOException {
		
		String prefix = "demo/";
		String localFile = "logo.png";
		String key = s3Client.upload (prefix, localFile);
		
		log.info ("Check S3Util.upload : {}", key);
	}

	@Test
	void testS3UtilDelete() throws IOException {
		
		// demo/20250801204049-logo.png
		// demo/20250802102400-logo.png
		
		String key = "demo/20250802102400-logo.png";
		s3Client.delete (key);
		
		log.info ("Check S3Util.delete : {}", key);
	}
}
