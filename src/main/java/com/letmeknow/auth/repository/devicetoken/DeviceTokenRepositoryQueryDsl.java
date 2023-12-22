package com.letmeknow.auth.repository.devicetoken;

import com.letmeknow.auth.entity.DeviceToken;

import java.util.Optional;

public interface DeviceTokenRepositoryQueryDsl {
    Optional<DeviceToken> findByDeviceTokenWithJWTs(String deviceToken);
}
