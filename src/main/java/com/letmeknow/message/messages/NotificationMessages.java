package com.letmeknow.message.messages;

import lombok.Getter;

@Getter
public enum NotificationMessages {
    FCM("FCM "),
    DEVICE_TOKEN("Device Token ");

    private final String message;

    private NotificationMessages(String message) {
        this.message = message;
    }
}
