package com.chatwithme.backend.device;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @Column(nullable = false, updatable = false)
    private String deviceId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String platform;

    @Column(length = 1024)
    private String fcmToken;

    @Column(nullable = false)
    private Instant lastSeenAt;

    protected Device() {
        // JPA
    }

    public Device(String deviceId, String name, String platform, String fcmToken, Instant lastSeenAt) {
        this.deviceId = deviceId;
        this.name = name;
        this.platform = platform;
        this.fcmToken = fcmToken;
        this.lastSeenAt = lastSeenAt;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
