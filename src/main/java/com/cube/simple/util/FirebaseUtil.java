package com.cube.simple.util;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FirebaseUtil {

    private final FirebaseMessaging messaging;

    public FirebaseUtil(FirebaseMessaging messaging) {
        this.messaging = messaging;
    }

    public String send(String token, String title, String body) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setNotification(AndroidNotification.builder()
                                .setIcon("default").setSound("default").build())
                        .build())
                .setToken(token)
                .build();

        return messaging.send(message); // ✅ DEFAULT not found 에러 사라짐
    }
}
