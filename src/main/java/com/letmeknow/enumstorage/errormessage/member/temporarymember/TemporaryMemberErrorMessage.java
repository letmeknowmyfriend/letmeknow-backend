package com.letmeknow.enumstorage.errormessage.member.temporarymember;

import lombok.Getter;

@Getter
public enum TemporaryMemberErrorMessage {
    NO_SUCH_TEMPORARY_MEMBER_WITH_THAT_VERIFICATION_CODE("해당하는 인증 코드를 가진 임시 회원이 없습니다."),
    NO_SUCH_TEMPORARY_MEMBER_WITH_THAT_EMAIL("해당하는 이메일을 가진 임시 회원이 없습니다."),
    NO_SUCH_TEMPORARY_MEMBER_WITH_THAT_ID("해당하는 임시 회원이 없습니다.");

    private final String message;

    TemporaryMemberErrorMessage(String message) {
        this.message = message;
    }
}
