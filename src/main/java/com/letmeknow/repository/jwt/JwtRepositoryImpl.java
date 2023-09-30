package com.letmeknow.repository.jwt;

import com.letmeknow.domain.auth.Jwt;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;
import java.util.Optional;

import static com.letmeknow.domain.auth.QJwt.jwt;
import static com.letmeknow.domain.member.QMember.member;

@RequiredArgsConstructor
public class JwtRepositoryImpl implements JwtRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public Optional<Jwt> findByRefreshTokenWithMember(String refreshToken) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
            .selectFrom(jwt)
            .join(jwt.member, member).fetchJoin()
            .where(jwt.refreshToken.eq(refreshToken))
            .fetchOne());
    }

}
