package com.cube.simple.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@ActiveProfiles("local")
@Slf4j
@SpringBootTest
class S3UtilUnitTests {

    private S3Util createS3UtilWithMock(S3Client mock, String bucket) {
        S3Util util = new S3Util();
        ReflectionTestUtils.setField(util, "s3Client", mock);
        ReflectionTestUtils.setField(util, "bucket", bucket);
        return util;
    }

    @Test
    @DisplayName("list(prefix): 지정 프리픽스 하위 키 목록 조회")
    void listOk() {
        S3Client s3 = mock(S3Client.class);
        S3Util util = createS3UtilWithMock(s3, "test-bucket");

        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(
                        S3Object.builder().key("demo/a.txt").build(),
                        S3Object.builder().key("demo/b.txt").build()
                )
                .build();
        when(s3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        List<String> keys = util.list("demo/");
        assertEquals(2, keys.size());
        assertTrue(keys.contains("demo/a.txt"));
        assertTrue(keys.contains("demo/b.txt"));

        ArgumentCaptor<ListObjectsV2Request> captor = ArgumentCaptor.forClass(ListObjectsV2Request.class);
        verify(s3).listObjectsV2(captor.capture());
        assertEquals("test-bucket", captor.getValue().bucket());
        assertEquals("demo/", captor.getValue().prefix());
    }

    @Test
    @DisplayName("upload(prefix, localFile): 타임스탬프-원본파일명 형식으로 업로드")
    void uploadOk() throws Exception {
        S3Client s3 = mock(S3Client.class);
        S3Util util = createS3UtilWithMock(s3, "test-bucket");

        Path temp = Files.createTempFile("logo", ".png");
        Files.writeString(temp, "dummy");

        when(s3.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(null); // 성공 시 보통 null 반환해도 무방(검증은 키와 요청 값으로 진행)

        String key = util.upload("demo/", temp.toString());
        assertNotNull(key);
        assertTrue(key.startsWith("demo/"));
        assertTrue(key.endsWith("-" + temp.getFileName().toString()));

        ArgumentCaptor<PutObjectRequest> reqCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3).putObject(reqCaptor.capture(), any(RequestBody.class));
        assertEquals("test-bucket", reqCaptor.getValue().bucket());
        assertEquals(key, reqCaptor.getValue().key());
    }

    @Test
    @DisplayName("download(key): 바이트 배열 반환")
    void downloadOk() throws IOException {
        S3Client s3 = mock(S3Client.class);
        S3Util util = createS3UtilWithMock(s3, "test-bucket");

        byte[] bytes = "hello".getBytes();
        ResponseBytes<GetObjectResponse> resp =
                ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), bytes);

        // Consumer<GetObjectRequest.Builder>를 받는 오버로드를 any()로 처리
        // when(s3.getObjectAsBytes(any())).thenReturn(resp);
        when(s3.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(resp);
        
        byte[] result = util.download("demo/hello.txt");
        assertArrayEquals(bytes, result);
    }

    @Test
    @DisplayName("download(key): S3 예외 → IOException 변환")
    void downloadThrowsIoOnS3Exception() {
        // given
        S3Client s3 = mock(S3Client.class);
        S3Util util = createS3UtilWithMock(s3, "test-bucket");

        // getObjectAsBytes 오버로드 모호성 해결: Consumer<GetObjectRequest.Builder> 매처 사용
        when(s3.getObjectAsBytes(ArgumentMatchers.<Consumer<GetObjectRequest.Builder>>any()))
            .thenThrow(
                S3Exception.builder()
                    .statusCode(500)
                    .message("boom")
                    .build()
            );

        // when & then
        assertThrows(IOException.class, () -> util.download("demo/missing.txt"));
    }
    
    @Test
    @DisplayName("delete(key): 삭제 요청 수행")
    void deleteOk() {
        S3Client s3 = mock(S3Client.class);
        S3Util util = createS3UtilWithMock(s3, "test-bucket");

        util.delete("demo/x.txt");
        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3).deleteObject(captor.capture());
        assertEquals("test-bucket", captor.getValue().bucket());
        assertEquals("demo/x.txt", captor.getValue().key());
    }
}
