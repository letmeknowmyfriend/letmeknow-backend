package com.letmeknow.repository.jwt;

import com.letmeknow.domain.auth.Jwt;

import java.util.Optional;

public interface JwtRepositoryQueryDsl {
    Optional<Jwt> findByRefreshTokenWithMember(String refreshToken);
}
