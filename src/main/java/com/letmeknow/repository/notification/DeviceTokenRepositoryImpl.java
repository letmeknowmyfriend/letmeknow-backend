package com.letmeknow.repository.notification;

import com.letmeknow.domain.notification.DeviceToken;
import com.letmeknow.enumstorage.status.MemberStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;
import java.util.Optional;

import static com.letmeknow.domain.auth.QRefreshToken.refreshToken1;
import static com.letmeknow.domain.notification.QDeviceToken.deviceToken1;

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
                .leftJoin(deviceToken1.refreshTokens, refreshToken1).fetchJoin()
            .fetchOne()
        );
    }
}
