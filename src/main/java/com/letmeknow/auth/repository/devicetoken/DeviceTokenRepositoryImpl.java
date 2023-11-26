package com.letmeknow.auth.repository.devicetoken;

import com.letmeknow.auth.entity.DeviceToken;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;
import java.util.Optional;

import static com.letmeknow.auth.entity.QDeviceToken.deviceToken1;
import static com.letmeknow.auth.entity.QRefreshToken.refreshToken1;

@RequiredArgsConstructor
public class DeviceTokenRepositoryImpl implements DeviceTokenRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public Optional<DeviceToken> findByDeviceTokenWithJWTs(String deviceToken) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
            queryFactory
                .selectFrom(deviceToken1)
                .where(deviceToken1.deviceToken.eq(deviceToken))
                .leftJoin(deviceToken1.refreshToken, refreshToken1).fetchJoin()
            .fetchOne()
        );
    }

    @Override
    public void deleteByRefreshTokenId(long refreshTokenId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        queryFactory
            .delete(deviceToken1)
            .where(deviceToken1.refreshToken.id.eq(refreshTokenId))
            .execute();
    }
}
