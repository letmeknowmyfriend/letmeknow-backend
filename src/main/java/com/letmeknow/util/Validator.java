package com.letmeknow.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import com.letmeknow.enumstorage.errormessage.auth.PasswordErrorMessage;
import com.letmeknow.exception.auth.PasswordException;

@Component
public class Validator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))"
    );

    public static boolean isValidEmail(String email) {
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidPassword(String password) throws PasswordException {
        if (password.isBlank()) {
            throw new PasswordException(PasswordErrorMessage.PASSWORD_IS_BLANK.getMessage());
        }

        //비밀번호 포맷 확인(영문, 특수문자, 숫자 포함 8자 이상, 30자 이하)
        Pattern passwordFormatPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,30}$");
        Matcher passwordFormatMatcher = passwordFormatPattern.matcher(password);

        if (!passwordFormatMatcher.find()) {
            throw new PasswordException(PasswordErrorMessage.PASSWORD_FORMAT_IS_NOT_VALID.getMessage());
        }

        //반복된 문자 확인
        Pattern passwordRepeatPattern = Pattern.compile("(\\w)\\1{2,}");
        Matcher passwordRepeatMatcher = passwordRepeatPattern.matcher(password);

        if (passwordRepeatMatcher.find()) {
            throw new PasswordException(PasswordErrorMessage.PASSWORD_CONTAINS_REPEATED_CHARACTER.getMessage());
        }

        return true;
    }

    public static boolean isValidPassword(String email, String password) throws PasswordException {
        if (password.isBlank()) {
            throw new PasswordException(PasswordErrorMessage.PASSWORD_IS_BLANK.getMessage());
        }

        //비밀번호 포맷 확인(영문, 특수문자, 숫자 포함 8자 이상, 30자 이하)
        Pattern passwordFormatPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,30}$");
        Matcher passwordFormatMatcher = passwordFormatPattern.matcher(password);

        if (!passwordFormatMatcher.find()) {
            throw new PasswordException(PasswordErrorMessage.PASSWORD_FORMAT_IS_NOT_VALID.getMessage());
        }

        //반복된 문자 확인
        Pattern passwordRepeatPattern = Pattern.compile("(\\w)\\1{2,}");
        Matcher passwordRepeatMatcher = passwordRepeatPattern.matcher(password);

        if (passwordRepeatMatcher.find()) {
            throw new PasswordException(PasswordErrorMessage.PASSWORD_CONTAINS_REPEATED_CHARACTER.getMessage());
        }

        //아이디 포함 확인
        String id = email.split("@")[0];
        if (password.contains(id)) {
            throw new PasswordException(PasswordErrorMessage.PASSWORD_CONTAINS_ID.getMessage());
        }

//        //특정 특수문자 포함 확인
//        Pattern passwordSpecialPattern = Pattern.compile("[!?@#$%^&*( )/+\\-=~,.]");
//        Matcher passwordSpecialMatcher = passwordSpecialPattern.matcher(password);
//        if (!passwordSpecialMatcher.find()) {
//            throw new PasswordException(PasswordErrorMessage.PASSWORD_ONLY_CONTAINS_CERTAIN_SPECIAL_CHARACTER.getMessage());
//        }

        return true;
    }
}
