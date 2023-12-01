package com.letmeknow.enumstorage.response;

import lombok.Getter;

@Getter
public enum Status {
    SUCCESS("success"),
    FAIL("fail");

    private final String status;

    private Status(String status) {
        this.status = status;
    }
}
