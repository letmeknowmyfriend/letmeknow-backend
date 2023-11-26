package com.letmeknow.auth.messages;

import lombok.Getter;

@Getter
public enum JwtMessages {
    JWT("JWT "),
    ACCESS_TOKEN("access token "),
    REFRESH_TOKEN("refresh token "),
    REISSUE("재발급 ");

    private final String message;

    private JwtMessages(String message) {
        this.message = message;
    }
}
