package com.letmeknow.auth.repository.refreshtoken;

import com.letmeknow.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>, RefreshTokenRepositoryQueryDsl {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    Optional<RefreshToken> findByRefreshTokenWithMember(String refreshToken);
    Optional<RefreshToken> findByMemberId(long memberId);
    void deleteByRefreshToken(String refreshToken);
}
