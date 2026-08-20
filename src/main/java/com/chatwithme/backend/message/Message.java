package com.chatwithme.backend.message;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String senderName;

    @Column(nullable = false, length = 4000)
    private String text;

    @Column(nullable = false)
    private Instant createdAt;

    protected Message() {
        // JPA
    }

    public Message(String deviceId, String senderName, String text, Instant createdAt) {
        this.deviceId = deviceId;
        this.senderName = senderName;
        this.text = text;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getText() {
        return text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
