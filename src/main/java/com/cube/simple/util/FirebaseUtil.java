package com.cube.simple.util;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cube.simple.enums.FirebaseCode;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseUtil {

    private final FirebaseMessaging firebaseMessaging;

    // --- Single Device Send Methods ---

    public FirebaseCode send(String token, String title, String body) {
        return send(token, title, body, null, null);
    }

    public FirebaseCode send(String token, String title, String body, String image) {
        return send(token, title, body, image, null);
    }

    /**
     * Sends a push notification to a single device.
     *
     * @param token The device registration token.
     * @param title The title of the notification.
     * @param body  The body of the notification.
     * @param image An optional URL to an image.
     * @param link  An optional data payload link.
     * @return FirebaseStatus indicating the result.
     */
    public FirebaseCode send(String token, String title, String body, String image, String link) {
        Message.Builder messageBuilder = Message.builder()
                .setToken(token)
                .setNotification(buildNotification(title, body, image))
                .setAndroidConfig(getAndroidConfig());

        if (link != null && !link.isBlank()) {
            messageBuilder.putData("link", link);
        }

        try {
            String messageId = firebaseMessaging.send(messageBuilder.build());
            log.info("Successfully sent message to token {}: {}", token, messageId);
            return FirebaseCode.SUCCESS;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send message to token {}: {}", token, e.getMessage());
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                log.warn("Device token {} is no longer registered.", token);
                // Consider adding logic here to remove the token from your database
            }
            return FirebaseCode.FAILURE;
        }
    }

    // --- Multiple Device (Multicast) Send Methods ---

    public FirebaseCode send(List<String> tokens, String title, String body) {
        return send(tokens, title, body, null, null);
    }

    public FirebaseCode send(List<String> tokens, String title, String body, String image) {
        return send(tokens, title, body, image, null);
    }

    /**
     * Sends the same push notification to multiple devices.
     *
     * @param tokens A list of device registration tokens.
     * @param title  The title of the notification.
     * @param body   The body of the notification.
     * @param image  An optional URL to an image.
     * @param link   An optional data payload link.
     * @return FirebaseStatus indicating the result (SUCCESS, PARTIAL, or FAILURE).
     */
    public FirebaseCode send(List<String> tokens, String title, String body, String image, String link) {
        if (tokens == null || tokens.isEmpty()) {
            log.warn("Token list is empty or null. No messages to send.");
            return FirebaseCode.FAILURE;
        }

        MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(buildNotification(title, body, image))
                .setAndroidConfig(getAndroidConfig());

        if (Objects.nonNull(link) && !link.isBlank()) {
            messageBuilder.putData("link", link);
        }

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(messageBuilder.build());
            log.info("Multicast message sent. Success: {}, Failure: {}",
                    response.getSuccessCount(), response.getFailureCount());

            if (response.getFailureCount() > 0) {
                handleFailedMulticast(tokens, response);
            }

            if (response.getFailureCount() == 0) {
                return FirebaseCode.SUCCESS;
            } else if (response.getSuccessCount() > 0) {
                return FirebaseCode.PARTIAL;
            } else {
                return FirebaseCode.FAILURE;
            }
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send multicast message: {}", e.getMessage(), e);
            return FirebaseCode.FAILURE;
        }
    }

    // --- Helper Methods ---

    private Notification buildNotification(String title, String body, String image) {
        Notification.Builder builder = Notification.builder()
                .setTitle(title)
                .setBody(body);
        if (Objects.nonNull(image) && !image.isBlank()) {
        	builder.setImage(image);
        }
        return builder.build();
    }

    private AndroidConfig getAndroidConfig() {
        return AndroidConfig.builder()
                .setNotification(AndroidNotification.builder()
                        .setIcon("default")
                        .setSound("default")
                        .build())
                .build();
    }

    private void handleFailedMulticast(List<String> tokens, BatchResponse response) {
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            if (!responses.get(i).isSuccessful()) {
                // It's safer to match by index as the order is preserved.
                String token = tokens.get(i);
                FirebaseMessagingException e = responses.get(i).getException();
                log.warn("Message to token {} failed: {}", token, e.getMessage());
            }
        }
    }
}

