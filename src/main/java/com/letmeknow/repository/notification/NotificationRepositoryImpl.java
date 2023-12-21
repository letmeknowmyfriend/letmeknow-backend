package com.letmeknow.repository.notification;

import com.letmeknow.entity.notification.Notification;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;
import java.util.List;

import static com.letmeknow.entity.notification.QNotification.notification;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public List<Notification> findByNoOffsetWithArticle(Long lastId, Long pageSize, Long memberId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return queryFactory
            .selectFrom(notification)
            .where(
                ltId(lastId),
                notification.memberId.eq(memberId)
            )
            .leftJoin(notification.board).fetchJoin()
            .leftJoin(notification.article).fetchJoin()
            .orderBy(notification.id.desc())
            .limit(getPageSize(pageSize))
            .fetch();
    }

    @Override
    public void readNotification(Long notificationId, Long memberId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        queryFactory
            .update(notification)
            .set(notification.isRead, true)
            .where(
                notification.id.eq(notificationId)
                    .and(notification.member.id.eq(memberId))
            )
            .execute();
    }

    @Override
    public void deleteNotification(Long notificationId, Long memberId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        queryFactory
            .delete(notification)
            .where(
                notification.id.eq(notificationId)
                .and(notification.member.id.eq(memberId))
            )
            .execute();
    }

    private BooleanExpression ltId(Long lastId) {
        return lastId == null ? null : notification.id.lt(lastId);
    }

    private long getPageSize(Long pageSize) {
        return pageSize == null ? 10 : pageSize;
    }
}
