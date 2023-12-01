package com.letmeknow.repository.notification;

import com.letmeknow.entity.notification.Notification;

import java.util.List;

public interface NotificationRepositoryQueryDsl {
    List<Notification> findWithNoOffset(Long lastId, Long pageSize, Long memberId);
    void readNotification(Long notificationId, Long memberId);
    void deleteNotification(Long notificationId, Long memberId);
}
