package com.letmeknow.repository.notification;

public interface SubscriptionRepositoryQueryDsl {
    void deleteByMemberIdAndBoardId(Long memberId, Long boardId);
}
