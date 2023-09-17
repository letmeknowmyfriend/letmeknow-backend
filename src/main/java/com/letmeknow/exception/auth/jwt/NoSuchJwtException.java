package com.letmeknow.exception.auth.jwt;

public class NoSuchJwtException extends RuntimeException {
    private final String message;

    public NoSuchJwtException(String message) {
        this.message = message;
    }
}
