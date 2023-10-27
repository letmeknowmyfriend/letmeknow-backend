package com.letmeknow.repository.notification;

import com.letmeknow.domain.notification.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long>, DeviceTokenRepositoryQueryDsl {
    Optional<DeviceToken> findByDeviceTokenWithJWTs(String deviceToken);
}
