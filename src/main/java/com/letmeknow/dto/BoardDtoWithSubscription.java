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
    private final Boolean isThereSubscription;

    @Builder
    protected BoardDtoWithSubscription(Long id, String boardName, Boolean isThereSubscription) {
        this.id = id;
        this.boardName = boardName;
        this.isThereSubscription = isThereSubscription;
    }
}
