package com.letmeknow.repository.jwt;

import com.letmeknow.domain.auth.RefreshToken;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;
import java.util.Optional;

import static com.letmeknow.domain.auth.QRefreshToken.refreshToken1;
import static com.letmeknow.domain.member.QMember.member;
import static com.letmeknow.domain.notification.QDeviceToken.deviceToken1;

@RequiredArgsConstructor
public class JwtRepositoryImpl implements JwtRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public Optional<RefreshToken> findByRefreshTokenWithMember(String refreshToken) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
            .selectFrom(refreshToken1)
            .join(refreshToken1.member, member).fetchJoin()
            .where(refreshToken1.refreshToken.eq(refreshToken))
            .fetchOne());
    }

    @Override
    public Optional<RefreshToken> findByRefreshTokenWithDeviceToken(String refreshToken) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
            .selectFrom(refreshToken1)
            .where(refreshToken1.refreshToken.eq(refreshToken))
            .leftJoin(refreshToken1.deviceToken, deviceToken1).fetchJoin()
            .fetchOne());
    }
}
