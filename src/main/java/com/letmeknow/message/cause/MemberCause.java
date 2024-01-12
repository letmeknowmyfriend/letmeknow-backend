package com.letmeknow.message.cause;

import lombok.Getter;

@Getter
public enum MemberCause {
    MEMBER("member"),
    NAME("name"),
    EMAIL("email"),
    PASSWORD("password"),
    VERIFICATION("verification"),
    FORM("form")
    ;

    private final String cause;

    private MemberCause(String cause) {
        this.cause = cause;
    }
}
