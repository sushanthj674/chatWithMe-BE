package com.chatwithme.backend.device;

import com.chatwithme.backend.device.dto.DeviceResponse;
import com.chatwithme.backend.device.dto.HeartbeatRequest;
import com.chatwithme.backend.device.dto.RegisterDeviceRequest;
import com.chatwithme.backend.device.dto.RegisterDeviceResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/register")
    public RegisterDeviceResponse register(@Valid @RequestBody RegisterDeviceRequest request) {
        return deviceService.registerDevice(request);
    }

    @PostMapping("/{deviceId}/heartbeat")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void heartbeat(@PathVariable String deviceId, @RequestBody(required = false) HeartbeatRequest request) {
        deviceService.heartbeat(deviceId, request);
    }

    @GetMapping
    public List<DeviceResponse> list() {
        return deviceService.listDevices();
    }
}
