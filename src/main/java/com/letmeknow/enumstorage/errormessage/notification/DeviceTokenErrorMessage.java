package com.letmeknow.enumstorage.errormessage.notification;

public enum DeviceTokenErrorMessage {
    DEVICE_TOKEN_IS_EMPTY("DeviceToken이 비어있습니다."),
    NO_SUCH_DEVICE_TOKEN("해당하는 기기 토큰이 없습니다.")
    ;

    private final String message;

    DeviceTokenErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
