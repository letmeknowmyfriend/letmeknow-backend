package com.letmeknow.exception.auth.jwt;

public class NoRefreshTokenException extends Exception {
    public NoRefreshTokenException(String message) {
        super(message);
    }
}
