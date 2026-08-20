package com.chatwithme.backend.message.dto;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
        @NotBlank String deviceId,
        @NotBlank String senderName,
        @NotBlank String text
) {
}
