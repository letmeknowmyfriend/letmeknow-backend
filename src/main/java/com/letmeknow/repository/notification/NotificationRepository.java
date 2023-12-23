package com.letmeknow.repository.notification;

import com.letmeknow.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryQueryDsl {
    List<Notification> findByNoOffsetWithArticle(Long lastId, Long pageSize, Long memberId);
    List<Notification> findByNoOffsetWithArticleAndKeyword(String keyword, Long lastId, Long pageSize, Long memberId);
    void readNotification(Long notificationId, Long memberId);
    void deleteNotification(Long notificationId, Long memberId);
}
