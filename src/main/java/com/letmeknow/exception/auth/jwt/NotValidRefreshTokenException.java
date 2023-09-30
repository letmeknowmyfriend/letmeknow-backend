package com.letmeknow.exception.auth.jwt;

public class NotValidRefreshTokenException extends NotValidTokenException {
    public NotValidRefreshTokenException(String message) {
        super(message);
    }
}
