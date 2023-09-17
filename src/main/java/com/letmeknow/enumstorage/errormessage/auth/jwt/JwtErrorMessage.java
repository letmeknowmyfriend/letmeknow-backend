package com.letmeknow.enumstorage.errormessage.auth.jwt;

import lombok.Getter;

@Getter
public enum JwtErrorMessage {
    NO_SUCH_REFRESH_TOKEN("해당 refresh token이 존재하지 않습니다."),
    NOT_EXPIRED_YET("JWT가 아직 만료되지 않았습니다."),
    NOT_VALID_JWT("유효하지 않은 JWT입니다."),
    ACCESS_TOKEN_NOT_FOUND("Access Token이 존재하지 않습니다."),;

    private final String message;

    JwtErrorMessage(String message) {
        this.message = message;
    }
}
