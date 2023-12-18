package com.letmeknow.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED, force = true)
public class BoardDtoWithSubscription {
    private final Long id;
    private final String boardName;
    private final String boardUrl;
    private final Boolean isSubscribed;

    @Builder
    protected BoardDtoWithSubscription(Long id, String boardName, String boardUrl, Boolean isSubscribed) {
        this.id = id;
        this.boardName = boardName;
        this.boardUrl = boardUrl;
        this.isSubscribed = isSubscribed;
    }
}
