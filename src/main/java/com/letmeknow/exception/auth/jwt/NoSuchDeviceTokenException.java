package com.letmeknow.exception.auth.jwt;

public class NoSuchDeviceTokenException extends RuntimeException {
    public NoSuchDeviceTokenException(String message) {
        super(message);
    }
}
