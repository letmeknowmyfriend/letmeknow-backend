package com.security.repository.member.temporarymember;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import com.security.domain.member.TemporaryMember;

import javax.persistence.EntityManager;

import java.util.Optional;

import static com.security.domain.member.QTemporaryMember.temporaryMember;

@RequiredArgsConstructor
public class TemporaryMemberRepositoryImpl implements TemporaryMemberRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public Optional<Long> findIdByEmail(String email) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                .select(temporaryMember.id)
                .from(temporaryMember)
                .where(temporaryMember.email.eq(email))
                .fetchOne());
    }

    @Override
    public Optional<TemporaryMember> findByEmail(String email) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                .selectFrom(temporaryMember)
                .where(temporaryMember.email.eq(email))
                .fetchOne());
    }

    @Override
    public Optional<String> findVerificationCodeByEmail(String email) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                .select(temporaryMember.verificationCode)
                .from(temporaryMember)
                .where(temporaryMember.email.eq(email))
                .fetchOne());
    }

    @Override
    public Optional<TemporaryMember> findByVerificationCode(String verificationCode) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                .selectFrom(temporaryMember)
                .where(temporaryMember.verificationCode.eq(verificationCode))
                .fetchOne());
    }
}
