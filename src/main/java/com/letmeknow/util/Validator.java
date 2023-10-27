package com.letmeknow.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.letmeknow.exception.auth.EmailException;
import com.letmeknow.exception.auth.NameException;
import com.letmeknow.message.MessageMaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.letmeknow.enumstorage.errormessage.auth.PasswordErrorMessage;
import com.letmeknow.exception.member.InvalidPasswordException;

import static com.letmeknow.message.reason.MemberReason.*;
import static com.letmeknow.message.Message.*;

@Component
@RequiredArgsConstructor
public class Validator {
    private final MessageMaker messageMaker;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))"
    );

    public void isValidName(String name) throws NameException {
        if (name.isBlank()) {
            throw new NameException(messageMaker.add(NAME).add(NOT_EXISTS).toString());
        }

        if (name.length() > 10) {
            throw new NameException(messageMaker.add(NAME).add(TOO_LONG).toString());
        }
    }

    public void isValidEmail(String email) throws EmailException {
        if (email.isBlank()) {
            throw new EmailException(messageMaker.add(EMAIL).add(NOT_EXISTS).toString());
        }

        Matcher matcher = EMAIL_PATTERN.matcher(email);

        if (!matcher.matches()) {
            throw new EmailException(messageMaker.add(EMAIL).add(INVALID).toString());
        }
    }

    /**
     * 비밀번호 유효성 검사
     * NotValidPasswordException에 해당하는 에러 메시지를 담아 예외를 던진다.
     * @param email
     * @param password
     * @throws InvalidPasswordException
     */
    public boolean isValidPassword(String email, String password) throws InvalidPasswordException {
        if (password.isBlank()) {
            throw new InvalidPasswordException(messageMaker.add(PASSWORD).add(NOT_EXISTS).toString());
        }

        // 비밀번호 포맷 확인(영문, 특수문자, 숫자 포함 8자 이상, 30자 이하)
        Pattern passwordFormatPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,30}$");
        Matcher passwordFormatMatcher = passwordFormatPattern.matcher(password);

        if (!passwordFormatMatcher.find()) {
            throw new InvalidPasswordException(PasswordErrorMessage.PASSWORD_FORMAT_IS_NOT_VALID.getMessage());
        }

        //반복된 문자 확인
        Pattern passwordRepeatPattern = Pattern.compile("(\\w)\\1{2,}");
        Matcher passwordRepeatMatcher = passwordRepeatPattern.matcher(password);

        if (passwordRepeatMatcher.find()) {
            throw new InvalidPasswordException(PasswordErrorMessage.PASSWORD_CONTAINS_REPEATED_CHARACTER.getMessage());
        }

        //아이디 포함 확인
        String id = email.split("@")[0];
        if (password.contains(id)) {
            throw new InvalidPasswordException(PasswordErrorMessage.PASSWORD_CONTAINS_ID.getMessage());
        }

        return true;

//        //특정 특수문자 포함 확인
//        Pattern passwordSpecialPattern = Pattern.compile("[!?@#$%^&*( )/+\\-=~,.]");
//        Matcher passwordSpecialMatcher = passwordSpecialPattern.matcher(password);
//        if (!passwordSpecialMatcher.find()) {
//            throw new NotValidPasswordException(PasswordErrorMessage.PASSWORD_ONLY_CONTAINS_CERTAIN_SPECIAL_CHARACTER.getMessage());
//        }
    }
}
