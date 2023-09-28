package com.letmeknow.repository.notification;

import com.letmeknow.domain.notification.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, SubscriptionRepositoryQueryDsl {
    Optional<Subscription> findOneByMemberIdAndBoardId(Long memberId, Long boardId);
}
