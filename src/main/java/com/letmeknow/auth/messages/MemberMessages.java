package com.letmeknow.auth.messages;

import lombok.Getter;

@Getter
public enum MemberMessages {
    MEMBER("회원 "),
    SIGN_UP("가입 "),
    SIGN_IN("로그인 "),
    TEMPORARY_MEMBER("임시 회원 "),
    NAME("이름 "),
    EMAIL("이메일 "),
    ID("아이디 "),
    PASSWORD("비밀번호 "),
    NEW_PASSWORD("새 비밀번호 "),
    ADDRESS("주소 "),
    AGAIN("재입력 "),
    SEND(" 전송 "),
    VERIFICATION("인증 ");

    private final String message;

    private MemberMessages(String message) {
        this.message = message;
    }
}
