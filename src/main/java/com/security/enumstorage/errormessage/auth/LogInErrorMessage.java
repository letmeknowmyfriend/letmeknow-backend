package com.security.enumstorage.errormessage.auth;

import lombok.Getter;

@Getter
public enum LogInErrorMessage {
    LOG_IN_ATTEMPT_EXCEEDED("로그인 시도 횟수를 초과하였습니다.");

    private final String message;

    LogInErrorMessage(String message) {
        this.message = message;
    }
}
