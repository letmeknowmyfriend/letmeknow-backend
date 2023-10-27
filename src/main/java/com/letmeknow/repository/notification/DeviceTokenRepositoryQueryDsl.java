package com.letmeknow.repository.notification;

import com.letmeknow.domain.notification.DeviceToken;

import java.util.Optional;

public interface DeviceTokenRepositoryQueryDsl {
    Optional<DeviceToken> findByDeviceTokenWithJWTs(String deviceToken);

}
