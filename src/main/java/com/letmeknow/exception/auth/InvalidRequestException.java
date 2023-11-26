package com.letmeknow.exception.auth;

import org.springframework.security.core.AuthenticationException;

public class InvalidRequestException extends AuthenticationException {
    public InvalidRequestException(String msg) {
        super(msg);
    }
}
