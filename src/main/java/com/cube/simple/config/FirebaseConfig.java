package com.cube.simple.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FirebaseConfig {

    // application.properties에서 키 파일 경로를 주입받습니다.
    @Value("${firebase.key-path}")
    private String firebaseKeyPath;

    /**
     * FirebaseApp 빈을 초기화하고 제공합니다.
     * 애플리케이션 시작 시 한 번만 초기화되도록 보장합니다.
     * @return 초기화된 FirebaseApp 인스턴스
     * @throws IOException 인증 파일을 읽을 수 없는 경우 발생
     */
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        log.debug("FirebaseApp 초기화를 시작합니다...");

        // 이미 앱이 초기화되었는지 확인하여 오류를 방지합니다.
        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
        if (firebaseApps != null && !firebaseApps.isEmpty()) {
            for (FirebaseApp app : firebaseApps) {
                if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
                    log.debug("이미 초기화된 FirebaseApp이 존재하여 기존 인스턴스를 반환합니다.");
                    return app;
                }
            }
        }

        // 클래스패스에서 인증 정보를 로드합니다. (JAR/WAR 패키징에 유리)
        ClassPathResource resource = new ClassPathResource(firebaseKeyPath);
        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("FirebaseApp 초기화에 성공했습니다.");
            return app;
        } catch (IOException e) {
            log.error("FirebaseApp 초기화 실패: {}", e.getMessage());
            throw new IOException("Firebase 인증 파일을 읽을 수 없습니다.", e);
        }
    }

    /**
     * 초기화된 FirebaseApp에 의존하는 FirebaseMessaging 빈을 제공합니다.
     * 이 빈은 FirebaseUtil과 같은 다른 서비스에 주입됩니다.
     * @param firebaseApp 초기화된 FirebaseApp 빈
     * @return 기본 앱의 FirebaseMessaging 인스턴스
     */
    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}

