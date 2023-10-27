package com.letmeknow.message;

import lombok.Getter;

@Getter
public enum PasswordMessage {
    NOT_VALID_PASSWORD_VERIFICATION_CODE("비밀번호 인증 코드가 일치하지 않습니다.");

    private final String message;

    PasswordMessage(String message) {
        this.message = message;
    }
}
