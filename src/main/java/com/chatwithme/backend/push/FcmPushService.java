package com.chatwithme.backend.push;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
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
import java.util.List;
import java.util.Map;

/**
 * Wraps the Firebase Admin SDK. Initialization is optional: if neither
 * FIREBASE_SERVICE_ACCOUNT_JSON nor FIREBASE_SERVICE_ACCOUNT_PATH resolve to
 * usable credentials, pushes are silently skipped so the rest of the API
 * keeps working in local/dev use.
 */
@Service
public class FcmPushService {

    private static final Logger log = LoggerFactory.getLogger(FcmPushService.class);
    private static final int MAX_TOKENS_PER_MULTICAST = 500;

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

    public void broadcastMessage(Long messageId, String deviceId, String senderName, String text, Instant createdAt, List<String> targetTokens) {
        if (!enabled || targetTokens.isEmpty()) {
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("type", "chat_message");
        data.put("id", String.valueOf(messageId));
        data.put("deviceId", deviceId);
        data.put("senderName", senderName);
        data.put("text", text);
        data.put("createdAt", createdAt.toString());

        // FCM multicast caps out at 500 tokens per call; chunk defensively.
        for (int start = 0; start < targetTokens.size(); start += MAX_TOKENS_PER_MULTICAST) {
            List<String> chunk = targetTokens.subList(start, Math.min(start + MAX_TOKENS_PER_MULTICAST, targetTokens.size()));
            sendChunk(data, chunk);
        }
    }

    private void sendChunk(Map<String, String> data, List<String> tokens) {
        MulticastMessage multicastMessage = MulticastMessage.builder()
                .putAllData(data)
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessage);
            if (response.getFailureCount() > 0) {
                log.warn("{} of {} FCM pushes failed", response.getFailureCount(), tokens.size());
            }
        } catch (FirebaseMessagingException e) {
            log.error("FCM broadcast failed: {}", e.getMessage());
        }
    }
}
