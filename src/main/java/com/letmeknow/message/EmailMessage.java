package com.letmeknow.message;

import lombok.Getter;

@Getter
public enum EmailMessage {
    VERIFICATION_EMAIL_SUBJECT("LetMeKnow 회원가입 인증 메일입니다."),
    VERIFICATION_EMAIL_MESSAGE("LetMeKnow 회원가입 인증 메일입니다.<br>" + "이메일을 인증하려면 아래 링크를 눌러주세요.<br><br>"),
    VERIFICATION_EMAIL_LINK1("<a href=https://localhost:8443/auth/member/verification-email?verificationCode="),
    VERIFICATION_EMAIL_LINK2(">이메일 인증하기</a>"),
    VERIFICATION_EMAIL_SENT("회원가입 인증 이메일이 성공적으로 발송되었습니다."),
    CHECK_VERIFICATION_EMAIL("이메일 인증을 완료해주세요."),
    VERIFICATION_EMAIL_RESENT("회원가입 인증 이메일이 성공적으로 재발송되었습니다."),
    VERIFICATION_EMAIL_SUCCESS("이메일 인증에 성공하였습니다."),
    VERIFICATION_EMAIL_SEND_FAIL("회원가입 인증 이메일 발송에 실패하였습니다. 다시 시도해주세요."),
    CHANGE_PASSWORD_EMAIL_SUBJECT("LetMeKnow 비밀번호 변경 메일입니다."),
    CHANGE_PASSWORD_EMAIL_MESSAGE("LetMeKnow 비밀번호 변경 메일입니다.<br>" + "비밀번호를 변경하려면 아래 링크를 눌러주세요.<br><br>"),
    CHANGE_PASSWORD_EMAIL_LINK1("<a href=https://localhost:8443/auth/member/change-password/"),
    CHANGE_PASSWORD_EMAIL_LINK2(">비밀번호 변경하기</a>"),
    CHANGE_PASSWORD_EMAIL_SENT("비밀번호 변경 이메일이 성공적으로 발송되었습니다."),
    CHANGE_PASSWORD_EMAIL_SUCCESS("비밀번호 변경에 성공하였습니다."),
    CHANGE_PASSWORD_EMAIL_SEND_FAIL("비밀번호 변경 이메일 발송에 실패하였습니다. 다시 시도해주세요.");

    private final String message;

    EmailMessage(String message) {
        this.message = message;
    }
}
