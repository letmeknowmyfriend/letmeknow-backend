package com.letmeknow.util;

import com.letmeknow.dto.member.MemberCreationDto;
import com.letmeknow.enumstorage.errormessage.auth.PasswordErrorMessage;
import com.letmeknow.exception.member.MemberSignUpValidationException;
import com.letmeknow.form.auth.MemberSignUpForm;
import com.letmeknow.message.cause.MemberCause;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.letmeknow.auth.messages.MemberMessages.*;
import static com.letmeknow.message.messages.Messages.*;

@Component
@RequiredArgsConstructor
public class Validator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))"
    );

    // 비밀번호에 반복된 문자가 3개 이상 포함되어 있으면 안된다.
    private static final Pattern passwordRepeatPattern = Pattern.compile("(\\w)\\1{2,}");

    // 영문, 숫자, 특수문자 포함 8자 이상, 30자 이하
    private static final Pattern passwordFormatPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,30}$");


    public void validateNewMember(MemberSignUpForm memberSignUpForm) throws MemberSignUpValidationException {
        // 도시 검사
        isValidVarchar255(memberSignUpForm.getCity());

        // 거리 검사
        isValidVarchar255(memberSignUpForm.getStreet());

        // 우편번호 검사
        isValidVarchar255(memberSignUpForm.getZipcode());

        // 이름 검사
        isValidName(memberSignUpForm.getName());

        // 이메일 검사
        isValidEmail(memberSignUpForm.getEmail());

        // 비밀번호 검사
        isValidPassword(memberSignUpForm.getEmail(), memberSignUpForm.getPassword(), memberSignUpForm.getPasswordAgain());
    }

    private void isValidVarchar255(String varchar255) throws MemberSignUpValidationException {
        if (varchar255 == null) {
            throw new MemberSignUpValidationException(MemberCause.FORM, new StringBuffer().append(ADDRESS.getMessage()).append(NOT_FOUND.getMessage()).toString());
        }

        if (varchar255.isBlank()) {
            throw new MemberSignUpValidationException(MemberCause.FORM, new StringBuffer().append(ADDRESS.getMessage()).append(NOT_FOUND.getMessage()).toString());
        }

        if (varchar255.isBlank()) {
            throw new MemberSignUpValidationException(MemberCause.FORM, new StringBuffer().append(ADDRESS.getMessage()).append(NOT_FOUND.getMessage()).toString());
        }

        if (varchar255.length() > 255) {
            throw new MemberSignUpValidationException(MemberCause.FORM, new StringBuffer().append(ADDRESS.getMessage()).append(TOO_LONG.getMessage()).toString());
        }
    }

    private void isValidName(String input) throws MemberSignUpValidationException {
        // 이름 검사
        String name = Optional.ofNullable(input).orElseThrow(
            () -> new MemberSignUpValidationException(MemberCause.NAME, new StringBuffer().append(NAME.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        if (name.isBlank()) {
            throw new MemberSignUpValidationException(MemberCause.NAME, new StringBuffer().append(NAME.getMessage()).append(NOT_EXISTS.getMessage()).toString());
        }

        if (name.length() > 10) {
            throw new MemberSignUpValidationException(MemberCause.NAME, new StringBuffer().append(NAME.getMessage()).append(TOO_LONG).toString());
        }
    }

    private void isValidEmail(String input) throws MemberSignUpValidationException {
        // 이메일 규칙 검사
        String email = Optional.ofNullable(input).orElseThrow(
            () -> new MemberSignUpValidationException(MemberCause.EMAIL, new StringBuffer().append(EMAIL.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        if (email.isBlank()) {
            throw new MemberSignUpValidationException(MemberCause.EMAIL, new StringBuffer().append(EMAIL.getMessage()).append(NOT_EXISTS.getMessage()).toString());
        }

        Matcher matcher = EMAIL_PATTERN.matcher(email);

        if (!matcher.matches()) {
            throw new MemberSignUpValidationException(MemberCause.EMAIL, new StringBuffer().append(EMAIL.getMessage()).append(INVALID.getMessage()).toString());
        }
    }

    public boolean isValidPassword(String email, String password, String passwordAgain) throws MemberSignUpValidationException {
        password = Optional.ofNullable(password).orElseThrow(
            () -> new MemberSignUpValidationException(MemberCause.PASSWORD, new StringBuffer().append(PASSWORD.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        passwordAgain = Optional.ofNullable(passwordAgain).orElseThrow(
            () -> new MemberSignUpValidationException(MemberCause.PASSWORD, new StringBuffer().append(PASSWORD.getMessage()).append(AGAIN).append(NOT_EXISTS.getMessage()).toString()));

        if (password.isBlank()) {
            throw new MemberSignUpValidationException(MemberCause.PASSWORD, new StringBuffer().append(PASSWORD.getMessage()).append(NOT_EXISTS.getMessage()).toString());
        }

        // 비밀번호 일치 검사
        if (!password.equals(passwordAgain)) {
            throw new MemberSignUpValidationException(MemberCause.PASSWORD, new StringBuffer().append(PASSWORD.getMessage()).append(AGAIN.getMessage()).append(NOT_EQUAL.getMessage()).toString());
        }

        // 비밀번호 포맷 확인(영문, 특수문자, 숫자 포함 8자 이상, 30자 이하)
        Matcher passwordFormatMatcher = passwordFormatPattern.matcher(password);

        if (!passwordFormatMatcher.find()) {
            throw new MemberSignUpValidationException(MemberCause.PASSWORD, PasswordErrorMessage.PASSWORD_FORMAT_IS_NOT_VALID.getMessage());
        }

        // 반복된 문자 확인
        Matcher passwordRepeatMatcher = passwordRepeatPattern.matcher(password);

        if (passwordRepeatMatcher.find()) {
            throw new MemberSignUpValidationException(MemberCause.PASSWORD, PasswordErrorMessage.PASSWORD_CONTAINS_REPEATED_CHARACTER.getMessage());
        }

        //아이디 포함 확인
        String id = email.split("@")[0];
        if (password.contains(id)) {
            throw new MemberSignUpValidationException(MemberCause.PASSWORD, PasswordErrorMessage.PASSWORD_CONTAINS_ID.getMessage());
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
