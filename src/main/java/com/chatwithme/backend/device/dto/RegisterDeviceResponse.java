package com.chatwithme.backend.device.dto;

import java.time.Instant;

public record RegisterDeviceResponse(
        String deviceId,
        Instant registeredAt
) {
}
