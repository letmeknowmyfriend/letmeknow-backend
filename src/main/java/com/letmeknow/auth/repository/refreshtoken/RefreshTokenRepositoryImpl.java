package com.letmeknow.auth.repository.refreshtoken;

import com.letmeknow.auth.entity.RefreshToken;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;
import java.util.Optional;

import static com.letmeknow.auth.entity.QDeviceToken.deviceToken1;
import static com.letmeknow.auth.entity.QRefreshToken.refreshToken1;
import static com.letmeknow.entity.member.QMember.member;

@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepositoryQueryDsl {
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
}
