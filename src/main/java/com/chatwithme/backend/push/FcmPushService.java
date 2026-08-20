package com.chatwithme.backend.push;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps the Firebase Admin SDK. Initialization is optional: if neither
 * FIREBASE_SERVICE_ACCOUNT_JSON nor FIREBASE_SERVICE_ACCOUNT_PATH resolve to
 * usable credentials, pushes are silently skipped so the rest of the API
 * keeps working in local/dev use.
 *
 * Broadcasts go to a topic rather than per-device tokens: every device
 * subscribes to CHAT_TOPIC on startup, and the FCM SDK re-associates that
 * subscription with a fresh token automatically on rotation, so there's
 * nothing here to keep in sync.
 */
@Service
public class FcmPushService {

    private static final Logger log = LoggerFactory.getLogger(FcmPushService.class);
    public static final String CHAT_TOPIC = "chat-messages";

    private final String serviceAccountJson;
    private final String serviceAccountPath;
    private boolean enabled = false;

    public FcmPushService(@Value("${firebase.service-account-json:}") String serviceAccountJson,
                           @Value("${firebase.service-account-path:}") String serviceAccountPath) {
        this.serviceAccountJson = serviceAccountJson;
        this.serviceAccountPath = serviceAccountPath;
    }

    @PostConstruct
    void init() {
        try (InputStream serviceAccount = openCredentialsStream()) {
            if (serviceAccount == null) {
                log.warn("Neither FIREBASE_SERVICE_ACCOUNT_JSON nor FIREBASE_SERVICE_ACCOUNT_PATH is set; FCM pushes are disabled (messages will still be stored).");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            enabled = true;
            log.info("Firebase Admin SDK initialized");
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
        }
    }

    private InputStream openCredentialsStream() throws IOException {
        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            return new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
        }
        if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
            return new FileInputStream(serviceAccountPath);
        }
        return null;
    }

    public void broadcastMessage(Long messageId, String deviceId, String senderName, String text, Instant createdAt) {
        if (!enabled) {
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("type", "chat_message");
        data.put("id", String.valueOf(messageId));
        data.put("deviceId", deviceId);
        data.put("senderName", senderName);
        data.put("text", text);
        data.put("createdAt", createdAt.toString());

        Message message = Message.builder()
                .putAllData(data)
                .setTopic(CHAT_TOPIC)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.error("FCM broadcast failed: {}", e.getMessage());
        }
    }
}
