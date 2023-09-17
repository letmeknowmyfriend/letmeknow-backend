package com.letmeknow.util.email;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Email {
    private final String subject;
    private final String receiver;
    private final String message;

    @Builder
    protected Email(String subject, String receiver, String message) {
        this.subject = subject;
        this.receiver = receiver;
        this.message = message;
    }
}
