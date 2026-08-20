package com.chatwithme.backend.device.dto;

import java.time.Instant;

public record DeviceResponse(
        String deviceId,
        String name,
        String platform,
        Instant lastSeenAt,
        boolean online
) {
}
