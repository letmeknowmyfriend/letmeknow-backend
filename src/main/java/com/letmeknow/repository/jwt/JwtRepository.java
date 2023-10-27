package com.letmeknow.repository.jwt;

import com.letmeknow.domain.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JwtRepository extends JpaRepository<RefreshToken, Long>, JwtRepositoryQueryDsl {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    Optional<RefreshToken> findByRefreshTokenWithMember(String refreshToken);
    Optional<RefreshToken> findByRefreshTokenWithDeviceToken(String refreshToken);
    Optional<RefreshToken> findByMemberId(Long memberId);
    void deleteByRefreshToken(String refreshToken);
}
