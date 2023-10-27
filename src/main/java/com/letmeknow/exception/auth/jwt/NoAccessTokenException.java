package com.letmeknow.exception.auth.jwt;

public class NoAccessTokenException extends Exception {
    public NoAccessTokenException(String message) {
        super(message);
    }
}
