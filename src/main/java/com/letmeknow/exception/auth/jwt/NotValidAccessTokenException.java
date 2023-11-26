package com.letmeknow.exception.auth.jwt;

public class NotValidAccessTokenException extends NotValidTokenException {
    public NotValidAccessTokenException(String message) {
        super(message);
    }
}
