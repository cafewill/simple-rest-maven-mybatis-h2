package com.cube.simple.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 클라이언트 설정 (AWS SDK v2)
 *
 * - application.properties(yml)의 cloud.aws.* 값을 주입받아 S3Client를 생성합니다.
 * - 운영환경에서는 가능하면 액세스 키/시크릿 키 대신 IAM Role(or DefaultCredentialsProvider) 사용을 권장합니다.
 */
@Configuration
public class S3Config {

    // 액세스 키 (로컬/개발 환경에서만 사용 권장)
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    // 시크릿 키 (절대 코드/레포지토리에 하드코딩 금지, 설정/비밀관리 사용)
    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    // 리전 (예: ap-northeast-2)
    @Value("${cloud.aws.region.static}")
    private String region;

    /**
     * S3Client 빈 등록
     * - StaticCredentialsProvider: 명시적 키를 사용해 인증
     *   (운영에선 DefaultCredentialsProvider로 대체하면 IAM Role, env, 프로파일 순으로 자동 탐색)
     * - Region.of(region): 설정에 지정된 리전을 사용
     */
    @Bean
    public S3Client s3Client() {
        // 단순 키/시크릿 기반 자격증명 생성
        AwsBasicCredentials creds = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                // 고정 크리덴셜 프로바이더 설정
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                // 리전 설정 (예: ap-northeast-2)
                .region(Region.of(region))
                .build();
    }
}
