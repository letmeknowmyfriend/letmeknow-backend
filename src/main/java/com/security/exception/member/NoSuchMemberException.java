package com.security.exception.member;

public class NoSuchMemberException extends RuntimeException {
    private String email;

    public NoSuchMemberException(String message) {
        super(message);
    }

    public NoSuchMemberException(String message, String email) {
        super(message);
        this.email = email;
    }
}
