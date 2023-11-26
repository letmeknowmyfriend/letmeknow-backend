package com.letmeknow.enumstorage.errormessage.notification;

import lombok.Getter;

@Getter
public enum SubscriptionErrorMessage {
    NO_SUCH_SUBSCRIPTION("구독하지 않은 게시판입니다."),
    NO_BOARD_ID("게시판 ID가 없습니다.");

    private final String message;

    SubscriptionErrorMessage(String message) {
        this.message = message;
    }
}
