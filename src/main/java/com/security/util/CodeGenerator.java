package com.security.util;

import org.springframework.stereotype.Component;

@Component
public class CodeGenerator {
    private static final String[] CODES = {
            "0123456789",
            "abcdefghijklmnopqrstuvwxyz",
    };

    public static String generateCode(int length) {
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; ++i) {
            int randomIndex = (int) (Math.random() * CODES.length);
            String randomCode = CODES[randomIndex];
            int randomCodeIndex = (int) (Math.random() * randomCode.length());
            code.append(randomCode.charAt(randomCodeIndex));
        }
        return code.toString();
    }
}
