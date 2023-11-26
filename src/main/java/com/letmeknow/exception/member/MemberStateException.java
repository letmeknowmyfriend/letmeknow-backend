package com.letmeknow.exception.member;

import org.springframework.security.core.AuthenticationException;

public class MemberStateException extends AuthenticationException {
    public MemberStateException(String msg) {
        super(msg);
    }
}
