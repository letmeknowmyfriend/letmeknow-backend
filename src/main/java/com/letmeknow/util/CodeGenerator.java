package com.letmeknow.util;

import org.springframework.stereotype.Component;

@Component
public class CodeGenerator {
    private static final StringBuilder sb = new StringBuilder();
    private static final String[] CODES = {
            "0123456789",
            "abcdefghijklmnopqrstuvwxyz",
    };

    public String generateCode(int length) {
        for (int i = 0; i < length; ++i) {
            int randomIndex = (int) (Math.random() * CODES.length);
            String randomCode = CODES[randomIndex];
            int randomCodeIndex = (int) (Math.random() * randomCode.length());
            sb.append(randomCode.charAt(randomCodeIndex));
        }

        String result = sb.toString();

        sb.setLength(0);

        return result;
    }
}
