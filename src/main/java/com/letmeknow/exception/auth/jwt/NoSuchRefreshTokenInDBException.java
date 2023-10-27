package com.letmeknow.exception.auth.jwt;

public class NoSuchRefreshTokenInDBException extends Exception {
    private final String message;

    public NoSuchRefreshTokenInDBException(String message) {
        this.message = message;
    }
}
