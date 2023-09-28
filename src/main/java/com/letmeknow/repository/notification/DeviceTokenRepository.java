package com.letmeknow.repository.notification;

import com.letmeknow.domain.notification.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long>, DeviceTokenRepositoryQueryDsl {
}
