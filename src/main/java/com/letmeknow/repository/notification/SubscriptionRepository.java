package com.letmeknow.repository.notification;

import com.letmeknow.entity.notification.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, SubscriptionRepositoryQueryDsl {
    Optional<Subscription> findOneByMemberIdAndBoardId(long memberId, long boardId);
    List<Subscription> findByBoardId(long boardId);
    void deleteByBoardId(String boardId);
}
