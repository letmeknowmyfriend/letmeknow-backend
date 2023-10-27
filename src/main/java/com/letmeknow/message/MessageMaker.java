package com.letmeknow.message;

import org.springframework.stereotype.Component;

@Component
public class MessageMaker {
    private final static StringBuilder sb = new StringBuilder();

    @Override
    public String toString() {
        String result = sb.toString();

        // StringBuilder 초기화
        sb.setLength(0);

        return result;
    }

    public MessageMaker add(String string) {
        this.sb.append(string);
        return this;
    }
}
