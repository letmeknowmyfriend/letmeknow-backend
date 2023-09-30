package com.letmeknow.exception.auth.jwt;

public class NotValidTokenException extends Exception {
    public NotValidTokenException(String message) {
        super(message);
    }
}
