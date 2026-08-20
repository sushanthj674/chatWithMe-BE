package com.chatwithme.backend.message.dto;

import java.time.Instant;

public record MessageResponse(
        Long id,
        String deviceId,
        String senderName,
        String text,
        Instant createdAt
) {
}
