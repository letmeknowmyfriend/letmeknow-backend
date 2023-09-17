package com.security.exception.auth.jwt;

public class NotValidJwtException extends RuntimeException {
    public NotValidJwtException(String message) {
        super(message);
    }
}
