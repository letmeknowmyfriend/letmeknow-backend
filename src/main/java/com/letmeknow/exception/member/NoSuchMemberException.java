package com.letmeknow.exception.member;

public class NoSuchMemberException extends Exception {
    private String email;

    public NoSuchMemberException(String message) {
        super(message);
    }

    public NoSuchMemberException(String message, String email) {
        super(message);
        this.email = email;
    }
}
