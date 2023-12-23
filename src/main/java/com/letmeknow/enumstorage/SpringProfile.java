package com.letmeknow.enumstorage;

public enum SpringProfile {
    LOCAL("local"),
    DEV("dev"),
    PROD("prod");

    private final String profile;

    SpringProfile(String profile) {
        this.profile = profile;
    }

    public String getProfile() {
        return profile;
    }
}
