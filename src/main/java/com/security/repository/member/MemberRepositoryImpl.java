package com.security.repository.member;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import com.security.domain.member.Member;
import com.security.enumstorage.status.MemberStatus;

import javax.persistence.EntityManager;
import java.util.Optional;

import static com.security.domain.member.QMember.member;

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
}
