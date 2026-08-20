package com.chatwithme.backend.device;

import com.chatwithme.backend.device.dto.DeviceResponse;
import com.chatwithme.backend.device.dto.HeartbeatRequest;
import com.chatwithme.backend.device.dto.RegisterDeviceRequest;
import com.chatwithme.backend.device.dto.RegisterDeviceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final long onlineThresholdSeconds;

    public DeviceService(
            DeviceRepository deviceRepository,
            @Value("${app.device.online-threshold-seconds:45}") long onlineThresholdSeconds
    ) {
        this.deviceRepository = deviceRepository;
        this.onlineThresholdSeconds = onlineThresholdSeconds;
    }

    public RegisterDeviceResponse registerDevice(RegisterDeviceRequest request) {
        Instant now = Instant.now();
        Device device = deviceRepository.findById(request.deviceId())
                .orElseGet(() -> new Device(request.deviceId(), request.name(), request.platform(), request.fcmToken(), now));

        device.setName(request.name());
        device.setPlatform(request.platform());
        device.setFcmToken(request.fcmToken());
        device.setLastSeenAt(now);
        deviceRepository.save(device);

        return new RegisterDeviceResponse(device.getDeviceId(), now);
    }

    public void heartbeat(String deviceId, HeartbeatRequest request) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not registered: " + deviceId));

        if (request != null && request.fcmToken() != null && !request.fcmToken().isBlank()) {
            device.setFcmToken(request.fcmToken());
        }
        device.setLastSeenAt(Instant.now());
        deviceRepository.save(device);
    }

    public List<DeviceResponse> listDevices() {
        Instant now = Instant.now();
        return deviceRepository.findAll().stream()
                .map(device -> toResponse(device, now))
                .toList();
    }

    private DeviceResponse toResponse(Device device, Instant now) {
        boolean online = Duration.between(device.getLastSeenAt(), now).getSeconds() < onlineThresholdSeconds;
        return new DeviceResponse(
                device.getDeviceId(),
                device.getName(),
                device.getPlatform(),
                device.getLastSeenAt(),
                online
        );
    }
}
