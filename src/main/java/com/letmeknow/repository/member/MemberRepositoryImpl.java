package com.letmeknow.repository.member;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import com.letmeknow.domain.member.Member;
import com.letmeknow.enumstorage.status.MemberStatus;

import javax.persistence.EntityManager;
import java.util.Optional;

import static com.letmeknow.domain.auth.QRefreshToken.refreshToken1;
import static com.letmeknow.domain.member.QMember.member;
import static com.letmeknow.domain.notification.QSubscription.subscription;
import static com.letmeknow.domain.notification.QDeviceToken.deviceToken1;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public Optional<Member> findNotDeletedById(Long id) {
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
    public Optional<Member> findNotDeletedByEmailWithJwtAndDeviceToken(String email, String deviceToken) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
            queryFactory
                        .selectDistinct(member)
                        .from(member)
                        .where(member.email.eq(email)
                                .and(member.status.ne(MemberStatus.DELETED)))
                        .leftJoin(member.refreshTokens, refreshToken1).fetchJoin()
                        .leftJoin(member.deviceTokens, deviceToken1).fetchJoin()
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
