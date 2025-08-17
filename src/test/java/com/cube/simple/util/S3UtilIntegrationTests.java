package com.cube.simple.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@ActiveProfiles("local")
@Slf4j
@SpringBootTest(classes = { S3UtilIntegrationTests.S3TestConfig.class })
@Testcontainers
@TestPropertySource(properties = {
        "cloud.aws.s3.bucket=int-test-bucket"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class S3UtilIntegrationTests {

    @Container
    static final LocalStackContainer LOCALSTACK = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:2.3"))
            .withServices(LocalStackContainer.Service.S3);

    @TestConfiguration
    static class S3TestConfig {
        @Bean
        public S3Client s3Client() {
            return S3Client.builder()
                    .endpointOverride(LOCALSTACK.getEndpointOverride(LocalStackContainer.Service.S3))
                    .region(Region.of(LOCALSTACK.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(LOCALSTACK.getAccessKey(), LOCALSTACK.getSecretKey())))
                    .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                    .build();
        }
    }

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Util s3Util;

    @org.springframework.beans.factory.annotation.Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @BeforeEach
    void ensureBucketExists() {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        } catch (BucketAlreadyOwnedByYouException | BucketAlreadyExistsException ignored) {
        }
    }

    @Test
    @DisplayName("업로드 → 리스트 → 다운로드 → 삭제 → 리스트 재확인 (E2E)")
    void s3EndToEndFlowOk() throws Exception {
        // 1) 업로드
        Path temp = Files.createTempFile("inttest-", ".txt");
        Files.writeString(temp, "S3 integration test");
        String key = s3Util.upload("demo/", temp.toString());

        assertTrue(key.startsWith("demo/"));
        assertTrue(key.endsWith("-" + temp.getFileName().getFileName().toString()));

        // 2) 리스트 확인
        List<String> keysAfterUpload = s3Util.list("demo/");
        assertTrue(keysAfterUpload.contains(key));

        // 3) 다운로드
        byte[] downloaded = s3Util.download(key);
        assertEquals("S3 integration test", new String(downloaded));

        // 4) 삭제
        s3Util.delete(key);

        // 5) 리스트 재확인(키가 없어야 함)
        List<String> keysAfterDelete = s3Util.list("demo/");
        assertFalse(keysAfterDelete.contains(key));
    }
}
