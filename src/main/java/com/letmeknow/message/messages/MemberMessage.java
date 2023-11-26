package com.letmeknow.message.messages;

import lombok.Getter;

@Getter
public enum MemberMessage {
    VERIFICATION_CODE("인증 코드 "),
    SIGN_UP_SUCCESS("회원가입에 성공하였습니다."),
    CHANGE_PASSWORD_SUCCESS("비밀번호 변경에 성공하였습니다."),
    CONSENT_TO_PUSH_NOTIFICATION("푸시 알림 수신에 동의 "),;

    private final String message;

    MemberMessage(String message) {
        this.message = message;
    }
}
