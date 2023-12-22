package com.letmeknow.message.messages;

import lombok.Getter;

@Getter
public enum Messages {
    EXISTS("존재 "),
    NOT_EXISTS("존재하지 않음 "),
    NOT_FOUND("찾을 수 없음 "),
    SUCCESS("성공 "),
    FAIL("실패 "),
    CHARACTER("문자 "),
    DUPLICATED("중복된 "),
    EMPTY("비어있음 "),
    INVALID("유효하지 않음 "),
    INCORRECT("잘못된 "),
    EXCEEDED("초과 "),
    NOT_EQUAL("일치하지 않음 "),
    TOO_LONG("너무 긺 "),
    REDIRECT_URL("Redirect URL "),
    OR("또는 "),
    ALREADY("이미 "),
    DID_NOT("하지 않음 "),
    SUCH("해당하는 "),
    WITH("가지고 있는 "),
    FORMAT("형식 "),
    REPEATED("반복된 "),
    CONTAINED("포함된 "),
    IN("안에 "),
    VALIDATION("검증 "),
    REQUEST_VALIDATION("요청 검증 "),
    ATTEMPT("시도 "),
    REQUEST("요청 "),
    REQUEST_HEADER("Request Header "),
    HEADER("header "),
    TIME("시간 "),
    PASSED("지남 "),
    CHANGE("변경 "),
    ;

    private final String message;

    Messages(String message) {
        this.message = message;
    }
}
