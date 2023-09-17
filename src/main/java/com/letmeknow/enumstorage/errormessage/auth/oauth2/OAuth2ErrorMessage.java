package com.letmeknow.enumstorage.errormessage.auth.oauth2;

import lombok.Getter;

@Getter
public enum OAuth2ErrorMessage  {
    NO_SUCH_PROVIDER("해당 provider가 존재하지 않습니다."),
    NO_PROVIDER_ID("providerId가 존재하지 않습니다."),
    NO_ATTRIBUTE("attribute가 존재하지 않습니다."),
    NO_SUCH_OAUTH_2("해당 OAuth2가 존재하지 않습니다."),
    NO_EMAIL("email이 존재하지 않습니다."),
    NO_NAME("name이 존재하지 않습니다.");

    private final String message;

    OAuth2ErrorMessage(String message) {
        this.message = message;
    }
}
