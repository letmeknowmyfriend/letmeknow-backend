package com.letmeknow.auth.repository.refreshtoken;

import com.letmeknow.auth.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepositoryQueryDsl {
    Optional<RefreshToken> findByRefreshTokenWithMember(String refreshToken);
}
