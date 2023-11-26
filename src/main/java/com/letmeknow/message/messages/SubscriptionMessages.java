package com.letmeknow.message.messages;

import lombok.Getter;

@Getter
public enum SubscriptionMessages {
    SUBSCRIPTION("구독 "),
    UNSUBSCRIPTION("구독 취소 "),
    SUBSCRIBED("구독한 "),
    BOARD_ID("게시판 ID ");

    private final String message;

    private SubscriptionMessages(String message) {
        this.message = message;
    }
}
