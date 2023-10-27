package com.letmeknow.enumstorage.errormessage.member.temporarymember;

import lombok.Getter;

@Getter
public enum NameErrorMessage {
    NAME_IS_TOO_SHORT("이름은 2글자 이상이어야 합니다."),
    NAME_IS_TOO_LONG("이름은 10글자 이하여야 합니다."),
    NAME_IS_NOT_ALLOWED("이름에 허용되지 않는 문자가 포함되어 있습니다.");

    private final String message;

    NameErrorMessage(String message) {
        this.message = message;
    }
}
