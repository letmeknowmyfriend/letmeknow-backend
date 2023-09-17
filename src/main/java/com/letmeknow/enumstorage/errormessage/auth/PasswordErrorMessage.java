package com.letmeknow.enumstorage.errormessage.auth;

import lombok.Getter;

@Getter
public enum PasswordErrorMessage {
    PASSWORD_DOES_NOT_MATCH("비밀번호가 일치하지 않습니다."),
    PASSWORD_IS_EMPTY("비밀번호를 입력해주세요."),
    PASSWORD_AGAIN_IS_NOT_EQUAL("비밀번호 재입력이 일치하지 않습니다."),
    PASSWORD_IS_NOT_VALID("비밀번호가 유효하지 않습니다."),
    PASSWORD_IS_BLANK("비밀번호를 입력해주세요."),
    PASSWORD_FORMAT_IS_NOT_VALID("비밀번호는 대문자, 소문자, 특수문자, 숫자를 포함하여 8자 이상, 30자 이하여야 합니다."),
    PASSWORD_CONTAINS_REPEATED_CHARACTER("비밀번호에 같은 문자를 4번 이상 사용할 수 없습니다."),
    PASSWORD_CONTAINS_ID("비밀번호에 ID를 포함할 수 없습니다."),
    PASSWORD_ONLY_CONTAINS_CERTAIN_SPECIAL_CHARACTER("비밀번호에는 !?@#$%^&*( )/+\\-=~,.를 포함해야 합니다.");

    private final String message;

    PasswordErrorMessage(String message) {
        this.message = message;
    }
}
