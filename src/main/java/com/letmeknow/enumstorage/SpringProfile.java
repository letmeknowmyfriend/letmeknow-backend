package com.letmeknow.enumstorage;

import lombok.Getter;

@Getter
public enum SpringProfile {
    LOCAL("local"),
    DEV("dev"),
    PROD("prod");

    private final String profile;

    SpringProfile(String profile) {
        this.profile = profile;
    }
}
