package com.chatwithme.backend.device.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterDeviceRequest(
        @NotBlank String deviceId,
        @NotBlank String name,
        @NotBlank String platform
) {
}
