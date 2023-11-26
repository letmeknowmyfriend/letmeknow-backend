package com.letmeknow.repository.member;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import com.letmeknow.entity.member.Member;
import com.letmeknow.enumstorage.status.MemberStatus;

import javax.persistence.EntityManager;
import java.util.Optional;

import static com.letmeknow.auth.entity.QDeviceToken.deviceToken1;
import static com.letmeknow.auth.entity.QRefreshToken.refreshToken1;
import static com.letmeknow.entity.member.QMember.member;
import static com.letmeknow.entity.notification.QSubscription.subscription;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public Optional<Member> findNotDeletedById(long id) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(member)
                        .where(member.id.eq(id)
                                .and(member.status.ne(MemberStatus.DELETED)))
                        .fetchOne()
        );
    }

    @Override
    public Optional<Member> findNotDeletedByEmail(String email) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(member)
                        .where(member.email.eq(email)
                                .and(member.status.ne(MemberStatus.DELETED)))
                        .fetchOne()
        );
    }

    @Override
    public Optional<Member> findNotDeletedByEmailWithSubscription(String email) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
            queryFactory
                .selectFrom(member)
                .where(member.email.eq(email))
                .leftJoin(member.subscriptions, subscription).fetchJoin()
                .fetchOne());
    }

    @Override
    public Optional<Member> findNotDeletedByEmailWithDeviceToken(String email) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
            queryFactory
                .selectFrom(member)
                .where(member.email.eq(email))
                .leftJoin(member.deviceTokens, deviceToken1).fetchJoin()
                .fetchOne());
    }

    @Override
    public Optional<Member> findNotDeletedByEmailWithSubscriptionAndDeviceToken(String email) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
            queryFactory
                .selectFrom(member)
                .where(member.email.eq(email))
                .leftJoin(member.subscriptions, subscription).fetchJoin()
                .leftJoin(member.deviceTokens, deviceToken1).fetchJoin()
                .fetchOne());
    }


    @Override
    public Optional<Member> findNotDeletedByEmailWithRefreshTokenAndSubscriptionAndDeviceToken(String email) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
            queryFactory
                        .selectDistinct(member)
                        .from(member)
                        .where(member.status.ne(MemberStatus.DELETED)
                            .and(member.email.eq(email)))
                        .leftJoin(member.refreshTokens, refreshToken1).fetchJoin()
                        .leftJoin(member.subscriptions, subscription).fetchJoin()
                        .leftJoin(member.deviceTokens, deviceToken1).fetchJoin()
                        .fetchOne()
        );
    }

    @Override
    public Optional<Member> findNotDeletedByRefreshTokenIdWithSubscriptionAndDeviceToken(long refreshTokenId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
            queryFactory
                .selectDistinct(member)
                .from(member)
                .where(member.status.ne(MemberStatus.DELETED)
                    .and(member.refreshTokens.any().id.eq(refreshTokenId)))
                .leftJoin(member.refreshTokens, refreshToken1).fetchJoin()
                .leftJoin(member.subscriptions, subscription).fetchJoin()
                .leftJoin(member.deviceTokens, deviceToken1).fetchJoin()
                .fetchOne()
        );
    }

    @Override
    public Optional<Member> findNotDeletedByDeviceTokenIdWithSubscription(long deviceTokenId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
            queryFactory
                .selectDistinct(member)
                .from(member)
                .where(member.status.ne(MemberStatus.DELETED)
                    .and(member.deviceTokens.any().id.eq(deviceTokenId)))
                .leftJoin(member.subscriptions, subscription).fetchJoin()
                .fetchOne()
        );
    }

    @Override
    public Optional<Member> findNotDeletedByPasswordVerificationCode(String passwordVerificationCode) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(member)
                        .where(member.passwordVerificationCode.eq(passwordVerificationCode)
                                .and(member.status.ne(MemberStatus.DELETED)))
                        .fetchOne()
        );
    }

    @Override
    public Optional<Long> findIdByEmail(String email) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
                queryFactory
                        .select(member.id)
                        .from(member)
                        .where(member.email.eq(email))
                        .fetchOne()
        );
    }

    @Override
    public Optional<Long> findNotDeletedIdByEmail(String email) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
                queryFactory
                        .select(member.id)
                        .from(member)
                        .where(member.email.eq(email)
                                .and(member.status.ne(MemberStatus.DELETED)))
                        .fetchOne()
        );
    }

    @Override
    public Optional<Member> findNotDeletedByEmailAndDeviceTokenAndSubscription(String email) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
            queryFactory
                .selectDistinct(member)
                .from(member)
                .where(member.email.eq(email)
                    .and(member.status.ne(MemberStatus.DELETED)))
                .leftJoin(member.deviceTokens, deviceToken1).fetchJoin()
                .leftJoin(member.subscriptions, subscription).fetchJoin()
            .fetchOne());
    }
}
