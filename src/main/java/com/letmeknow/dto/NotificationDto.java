package com.letmeknow.dto;

import lombok.Builder;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@NoArgsConstructor(access = PROTECTED, force = true)
public class NotificationDto {
    private final Long id;
    private final Long memberId;
    private final Long articleId;
    private final Boolean isRead;

    @Builder
    protected NotificationDto(Long id, Long memberId, Long articleId, Boolean isRead) {
        this.id = id;
        this.memberId = memberId;
        this.articleId = articleId;
        this.isRead = isRead;
    }
}
