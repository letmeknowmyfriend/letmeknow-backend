package com.letmeknow.enumstorage.message;

import lombok.Getter;

@Getter
public enum Message {
    PRICE_MUST_BE_GREATER_THAN_ZERO("가격은 0보다 커야합니다.");

    private final String message;

    Message(String message) {
        this.message = message;
    }
}
