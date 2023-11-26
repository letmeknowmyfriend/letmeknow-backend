package com.letmeknow.exception.auth.jwt;

public class NoSuchRefreshTokenInDBException extends RuntimeException {
    private final String message;

    public NoSuchRefreshTokenInDBException(String message) {
        this.message = message;
    }
}
