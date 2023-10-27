package com.letmeknow.enumstorage.errormessage;

import lombok.Getter;

@Getter
public enum RequestArgumentErrorMessage {
    CONTENT_TYPE_IS_NOT_JSON("Content-Type이 application/json이 아닙니다.");

    private final String message;

    RequestArgumentErrorMessage(String message) {
        this.message = message;
    }
}
