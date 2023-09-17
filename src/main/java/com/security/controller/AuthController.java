package com.security.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.security.dto.member.MemberCreationDto;
import com.security.enumstorage.errormessage.auth.EmailErrorMessage;
import com.security.enumstorage.errormessage.auth.PasswordErrorMessage;
import com.security.enumstorage.message.EmailMessage;
import com.security.enumstorage.message.MemberMessage;
import com.security.enumstorage.message.PasswordMessage;
import com.security.exception.auth.PasswordException;
import com.security.exception.member.NoSuchMemberException;
import com.security.exception.member.temporarymember.NoSuchTemporaryMemberException;
import com.security.form.auth.MemberChangePasswordForm;
import com.security.form.auth.MemberLogInError;
import com.security.form.auth.MemberLogInForm;
import com.security.form.auth.MemberSignUpForm;
import com.security.service.member.MemberService;
import com.security.service.member.TemporaryMemberService;
import com.security.util.Validator;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;
    private final TemporaryMemberService temporaryMemberService;

    @GetMapping("/auth/login")
    public String logInForm(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "exception", required = false) String exception, @RequestParam(value = "email", required = false) String email, Model model) {
        MemberLogInForm memberLogInForm = new MemberLogInForm();
        MemberLogInError memberLogInError = new MemberLogInError();

        if (error != null) {
            if (email != null) {
                memberLogInForm.setEmail(email);
            }

            if (error.equals("email")) {
                memberLogInError.setEmail(exception);
            } else if (error.equals("password")) {
                memberLogInError.setPassword(exception);
            }
        }

        model.addAttribute("memberLogInForm", memberLogInForm);
        model.addAttribute("memberLogInError", memberLogInError);

        return "auth/logInForm";
    }

    @GetMapping("auth/signup")
    public String signUpForm(Model model) {
        model.addAttribute("memberSignUpForm", new MemberSignUpForm());

        return "auth/signUpForm";
    }

    @PostMapping("auth/signup")
    public String temporarySignUp(@Valid MemberSignUpForm memberSignUpForm, Model model, BindingResult result) {
        //이메일 규칙 검사
        if (!Validator.isValidEmail(memberSignUpForm.getEmail())) {
            result.addError(new FieldError("memberSignUpForm", "email", EmailErrorMessage.NOT_VALID_EMAIL.getMessage()));
        }

        //비밀번호 일치 검사
        if (!memberSignUpForm.getPassword().equals(memberSignUpForm.getPasswordAgain())) {
            result.addError(new FieldError("memberSignUpForm", "password", PasswordErrorMessage.PASSWORD_AGAIN_IS_NOT_EQUAL.getMessage()));
            result.addError(new FieldError("memberSignUpForm", "passwordAgain", PasswordErrorMessage.PASSWORD_AGAIN_IS_NOT_EQUAL.getMessage()));
        }

        //잘못된 값이 들어오면, 다시 회원가입 폼으로 돌아간다.
        if (result.hasErrors()) {
            model.addAttribute("memberSignUpForm", memberSignUpForm);

            return "auth/signUpForm";
        }

        //비밀번호 규칙 검사
        try
        {
            Validator.isValidPassword(memberSignUpForm.getEmail(), memberSignUpForm.getPassword());
        }
        catch (PasswordException e)
        {
            result.addError(new FieldError("memberSignUpForm", "password", e.getMessage()));
        }

        //잘못된 값이 들어오면, 다시 로그인 폼으로 돌아간다.
        if (result.hasErrors()) {
            model.addAttribute("memberSignUpForm", memberSignUpForm);
            return "auth/signUpForm";
        }

        //중복된 이메일이 존재하는지 확인한다.
        if (memberService.isEmailValid(memberSignUpForm.getEmail()))
        //중복된 이메일이 존재하지 않으면,
        {
            try {
                //임시 회원가입을 시도한다.
                temporaryMemberService.joinTemporaryMember(MemberCreationDto.builder()
                        .name(memberSignUpForm.getName())
                        .email(memberSignUpForm.getEmail())
                        .password(memberSignUpForm.getPassword())
                        .city(memberSignUpForm.getCity())
                        .street(memberSignUpForm.getStreet())
                        .zipcode(memberSignUpForm.getZipcode())
                        .build());

                //임시 회원가입이 완료되면, 인증 메일 메시지를 띄우고, 로그인 페이지로 이동한다.
                model.addAttribute("message", EmailMessage.VERIFICATION_EMAIL_SENT.getMessage())
                        .addAttribute("href", "/auth/login");

                return "message/message";

            } catch (DataIntegrityViolationException e) {
                //중복된 이메일은 존재 하지 않지만, 임시 회원가입 기록이 존재하는 경우
                model.addAttribute("message", EmailMessage.CHECK_VERIFICATION_EMAIL.getMessage())
                        .addAttribute("href", "/auth/member/notice/verification-email");

                return "message/message";
            } catch (UnsupportedEncodingException | MessagingException e) {
                //메일 전송에 실패한 경우
                model.addAttribute("message", EmailMessage.VERIFICATION_EMAIL_SEND_FAIL.getMessage())
                        .addAttribute("href", "/auth/member/notice/verification-email");

                return "message/message";
            }
        }

        //중복된 이메일이 존재하면
        //이메일 중복 에러를 추가한다.
        result.addError(new FieldError("memberSignUpForm", "email", EmailErrorMessage.DUPLICATE_EMAIL.getMessage()));
        model.addAttribute("memberSignUpForm", memberSignUpForm);

        return "auth/signUpForm";
    }

    @GetMapping("/auth/member/verification-email/{verificationCode}")
    public String verifyEmail(@PathVariable String verificationCode, Model model) {
        try {
            temporaryMemberService.verifyEmail(verificationCode);
            model.addAttribute("message", EmailMessage.VERIFICATION_EMAIL_SUCCESS.getMessage())
                    .addAttribute("href", "/auth/login");

            return "message/message";

        } catch (NoSuchTemporaryMemberException e) {
            model.addAttribute("message", e.getMessage())
                    .addAttribute("href", "/auth/login");

            return "message/message";
        }
    }

    @GetMapping("/auth/member/notice/verification-email")
    public String verificationEmailNotice(@PathVariable Long temporaryMemberId, Model model) {
        model.addAttribute("temporaryMemberId", temporaryMemberId);
        return "/auth/notice/verificationEmailNotice";
    }

    @GetMapping("/auth/member/change-password/{passwordVerificationCode}")
    public String changePasswordForm(@PathVariable String passwordVerificationCode, Model model) {
        //verificationCode로 인증
        try
        {
            memberService.verifyPasswordVerificationCode(passwordVerificationCode);
        }
        //verificationCode가 유효하지 않으면
        catch (NoSuchMemberException e)
        {
            model.addAttribute("message", PasswordMessage.NOT_VALID_PASSWORD_VERIFICATION_CODE.getMessage())
                    .addAttribute("href", "/auth/login");

            return "message/message";
        }

        //인증 성공시
        model.addAttribute("memberChangePasswordForm", new MemberChangePasswordForm())
                .addAttribute("passwordVerificationCode", passwordVerificationCode);

        return "auth/member/changePasswordForm";
    }

    @PostMapping("/auth/member/change-password/{passwordVerificationCode}")
    public String changePassword(@PathVariable String passwordVerificationCode, MemberChangePasswordForm memberChangePasswordForm, Model model, BindingResult result) {
        //새 비밀번호 일치 검사
        if (!memberChangePasswordForm.getNewPassword().equals(memberChangePasswordForm.getNewPasswordAgain())) {
            result.addError(new FieldError("memberChangePasswordForm", "newPassword", PasswordErrorMessage.PASSWORD_AGAIN_IS_NOT_EQUAL.getMessage()));
            result.addError(new FieldError("memberChangePasswordForm", "newPasswordAgain", PasswordErrorMessage.PASSWORD_AGAIN_IS_NOT_EQUAL.getMessage()));
        }

        //잘못된 값이 들어오면, 다시 로그인 폼으로 돌아간다.
        if (result.hasErrors()) {
            model.addAttribute("passwordVerificationCode", passwordVerificationCode);
            model.addAttribute("memberChangePasswordForm", memberChangePasswordForm);

            return "auth/member/changePasswordForm";
        }

        //새 비밀번호 규칙 검사
        try
        {
            Validator.isValidPassword(memberChangePasswordForm.getNewPassword());
        }
        catch (PasswordException e)
        {
            result.addError(new FieldError("memberChangePasswordForm", "newPassword", e.getMessage()));
            result.addError(new FieldError("memberChangePasswordForm", "newPasswordAgain", e.getMessage()));
        }

        //잘못된 값이 들어오면, 다시 로그인 폼으로 돌아간다.
        if (result.hasErrors()) {
            model.addAttribute("passwordVerificationCode", passwordVerificationCode);
            model.addAttribute("memberChangePasswordForm", memberChangePasswordForm);

            return "auth/member/changePasswordForm";
        }

        try
        {
            //비밀번호 변경, 상태 변경, verificationCode로 인증, verificationCode 삭제
            memberService.changePassword(passwordVerificationCode, memberChangePasswordForm.getNewPassword());
        }
        //verificationCode가 유효하지 않으면
        catch (NoSuchMemberException e)
        {
            model.addAttribute("message", PasswordMessage.NOT_VALID_PASSWORD_VERIFICATION_CODE.getMessage())
                    .addAttribute("href", "/auth/login");

            return "message/message";
        }

        //모든 작업 성공시, 로그인 페이지로 redirect
        model.addAttribute("message", MemberMessage.CHANGE_PASSWORD_SUCCESS.getMessage())
                .addAttribute("href", "/auth/login");

        return "message/message";
    }
    
    @GetMapping("/auth/member/notice/change-password")
    public String changePasswordNotice() {
        return "/auth/notice/changePasswordNotice";
    }

    //== Test ==//
    @GetMapping("/auth/oauth2/member/{memberId}/switch-role")
    public String switchRole(@PathVariable Long memberId) {
        memberService.switchRole(memberId);

        return "redirect:/";
    }
}