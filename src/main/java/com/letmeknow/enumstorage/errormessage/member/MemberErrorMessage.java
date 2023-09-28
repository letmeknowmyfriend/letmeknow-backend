package com.letmeknow.enumstorage.errormessage.member;

import lombok.Getter;

@Getter
public enum MemberErrorMessage {

    NO_SUCH_MEMBER("해당하는 회원이 없습니다."),
    NO_SUCH_MEMBER_WITH_THAT_EMAIL("해당하는 이메일을 가진 회원이 없습니다."),
    NO_SUCH_MEMBER_WITH_THAT_PASSWORD_VERIFICATION_CODE("해당하는 비밀번호 인증 코드를 가진 회원이 없습니다."),
    PLEASE_RESET_PASSWORD("비밀번호를 재설정해주세요."),
    DUPLICATED_EMAIL("이미 존재하는 이메일입니다.");

    private final String message;

    MemberErrorMessage(String message) {
        this.message = message;
    }
}
