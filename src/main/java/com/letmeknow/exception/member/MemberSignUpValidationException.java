package com.letmeknow.exception.member;

import lombok.Getter;

@Getter
public class MemberSignUpValidationException extends Exception {
    private final String reason;
    private final String message;

    public MemberSignUpValidationException(String reason, String message) {
        super();
        this.reason = reason;
        this.message = message;
    }
}
