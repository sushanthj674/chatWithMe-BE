package com.chatwithme.backend.device;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, String> {
}
