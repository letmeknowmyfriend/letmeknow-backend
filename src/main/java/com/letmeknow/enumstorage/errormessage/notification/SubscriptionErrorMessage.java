package com.letmeknow.enumstorage.errormessage.notification;

import lombok.Getter;

@Getter
public enum SubscriptionErrorMessage {
    ALREADY_SUBSCRIBED("이미 구독한 게시판입니다."),
    NO_SUCH_SUBSCRIPTION("구독하지 않은 게시판입니다.");

    private final String message;

    SubscriptionErrorMessage(String message) {
        this.message = message;
    }
}
