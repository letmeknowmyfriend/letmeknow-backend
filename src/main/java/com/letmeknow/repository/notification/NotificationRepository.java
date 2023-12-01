package com.letmeknow.repository.notification;

import com.letmeknow.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findWithNoOffset(Long lastId, Long pageSize, Long memberId);
    void readNotification(Long notificationId, Long memberId);
    void deleteNotification(Long notificationId, Long memberId);
}
