package com.letmeknow.repository.jwt;

import com.letmeknow.domain.auth.RefreshToken;

import java.util.Optional;

public interface JwtRepositoryQueryDsl {
    Optional<RefreshToken> findByRefreshTokenWithMember(String refreshToken);
    Optional<RefreshToken> findByRefreshTokenWithDeviceToken(String refreshToken);
}
