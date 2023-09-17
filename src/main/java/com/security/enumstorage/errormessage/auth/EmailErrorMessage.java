package com.security.enumstorage.errormessage.auth;

import lombok.Getter;

@Getter
public enum EmailErrorMessage {
    NOT_VALID_EMAIL("유효하지 않은 이메일입니다."),
    EMAIL_IS_EMPTY("이메일을 입력해주세요."),
    DUPLICATE_EMAIL("이미 존재하는 이메일입니다.");

    private final String message;

    EmailErrorMessage(String message) {
        this.message = message;
    }
}
