package com.letmeknow.message;

import lombok.Getter;

@Getter
public enum MemberMessage {
    SIGN_UP_SUCCESS("회원가입에 성공하였습니다."),
    CHANGE_PASSWORD_SUCCESS("비밀번호 변경에 성공하였습니다."),;

    private final String message;

    MemberMessage(String message) {
        this.message = message;
    }
}
