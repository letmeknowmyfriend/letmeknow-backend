package com.letmeknow.auth.repository.devicetoken;

import com.letmeknow.auth.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long>, DeviceTokenRepositoryQueryDsl {
    Optional<DeviceToken> findByDeviceToken(String deviceToken);
    Optional<DeviceToken> findByDeviceTokenWithJWTs(String deviceToken);
    void deleteByDeviceToken(String deviceToken);
}
