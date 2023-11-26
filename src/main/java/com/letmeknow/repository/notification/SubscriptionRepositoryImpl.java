package com.letmeknow.repository.notification;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;
import java.util.Optional;

import static com.letmeknow.entity.notification.QSubscription.subscription;

@RequiredArgsConstructor
public class SubscriptionRepositoryImpl implements SubscriptionRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public void deleteByMemberIdAndBoardId(Long memberId, Long boardId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        queryFactory
            .delete(subscription)
            .where(subscription.member.id.eq(memberId).and(subscription.board.id.eq(boardId)))
            .execute();
    }
}
