package com.letmeknow.exception.auth.jwt;

public class NoAccessTokenException extends RuntimeException {
    public NoAccessTokenException(String message) {
        super(message);
    }
}
