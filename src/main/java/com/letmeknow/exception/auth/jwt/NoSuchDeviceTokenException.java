package com.letmeknow.exception.auth.jwt;

public class NoSuchDeviceTokenException extends Exception {
    public NoSuchDeviceTokenException(String message) {
        super(message);
    }
}
