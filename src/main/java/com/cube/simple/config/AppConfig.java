package com.cube.simple.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cube.simple.util.AESUtil;
import com.cube.simple.util.FirebaseUtil;

import jakarta.annotation.PostConstruct;

@Component
public class AppConfig {

    @Value("${aes.key}")
    private String aesKey;

    @Value("${firebase.server.key}")
    private static String firebaseServerKey;

    @PostConstruct
    public void init() {
        AESUtil.setBase64Key(aesKey);
        FirebaseUtil.setServerKey(firebaseServerKey);
        
    }
}