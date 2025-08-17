package com.cube.simple.util;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Slf4j
@Component
public class S3Util {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Autowired
    private S3Client s3Client;

    /**
     * 지정된 prefix 아래 객체 키 리스트 조회
     * @param prefix e.g. "demo/"
     * @return 키 목록
     */
    public List<String> list(String prefix) {
        ListObjectsV2Request req = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();
        ListObjectsV2Response res = s3Client.listObjectsV2(req);
        return res.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
    }

    /**
     * 로컬 파일을 yyyyMMddHHmmss-originalFilename 형식으로 upload
     * @param prefix S3 경로 prefix (e.g. "demo/")
     * @param localFilePath 로컬 파일 경로
     * @return 업로드된 S3 객체 키
     * @throws IOException 파일 읽기 오류
     */
    public String upload(String prefix, String localFilePath) throws IOException {
        Path path = Path.of(localFilePath);
        String filename = path.getFileName().toString();
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String key = prefix + timestamp + "-" + filename;

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.putObject(req, RequestBody.fromFile(path));
        return key;
    }

    /**
     * S3에서 객체를 읽어와 byte[]로 반환합니다.
     *
     * @param key 전체 객체 키 (e.g. "demo/20250801204049-logo.png")
     * @return 객체 바이트 배열
     * @throws IOException IO 오류
     */
    public byte[] download(String key) throws IOException {
        try {
            return s3Client.getObjectAsBytes(builder -> builder
                        .bucket(bucket)
                        .key(key)
                    )
                    .asByteArray();
        } catch (SdkException ex) {
            throw new IOException("S3 다운로드 실패 : " + key, ex);
        }
    }
    
    /**
     * S3 객체 삭제
     * @param key 전체 객체 키 (e.g. "demo/20250801-test.png")
     */
    public void delete(String key) {
        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.deleteObject(req);
    }
}
