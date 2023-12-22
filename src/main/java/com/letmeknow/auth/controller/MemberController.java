package com.letmeknow.auth.controller;

import com.letmeknow.enumstorage.EmailEnum;
import com.letmeknow.exception.member.MemberSignUpValidationException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.exception.member.VerificationInvalidException;
import com.letmeknow.exception.member.temporarymember.NoSuchTemporaryMemberException;
import com.letmeknow.form.auth.MemberChangePasswordForm;
import com.letmeknow.message.cause.MemberCause;
import com.letmeknow.service.member.MemberService;
import com.letmeknow.service.member.TemporaryMemberService;
import com.letmeknow.util.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import static com.letmeknow.auth.messages.MemberMessages.PASSWORD;
import static com.letmeknow.message.messages.Messages.*;

@Controller
@RequestMapping("/auth/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final TemporaryMemberService temporaryMemberService;

    @GetMapping("/verification-email")
    public String verifyEmail(@RequestParam String verificationCode, Model model) {
        try {
            temporaryMemberService.verifyEmailAndTurnIntoMember(verificationCode);
            model.addAttribute("message", EmailEnum.VERIFICATION_EMAIL_SUCCESS.getMessage());

            return "message/message";

        } catch (NoSuchTemporaryMemberException e) {
            model.addAttribute("message", e.getMessage());

            return "message/message";
        }
    }

    // TODO: 비밀번호 변경 기능 구현
    @GetMapping("/change-password/{passwordVerificationCode}")
    public String changePasswordForm(@PathVariable String passwordVerificationCode, Model model) {
        //verificationCode로 인증
        try {
            memberService.verifyPasswordVerificationCode(passwordVerificationCode);

            // 인증 성공시
            model.addAttribute("memberChangePasswordForm", new MemberChangePasswordForm())
                .addAttribute("passwordVerificationCode", passwordVerificationCode);

            return "auth/member/changePasswordForm";
        }
        // 해당 회원이 없거나, verificationCode가 유효하지 않으면
        catch (NoSuchMemberException | VerificationInvalidException e) {
            model.addAttribute("message", e.getMessage());

            return "message/message";
        }
    }

    @PostMapping("/change-password/{passwordVerificationCode}")
    public String changePassword(@PathVariable String passwordVerificationCode, MemberChangePasswordForm memberChangePasswordForm, Model model, BindingResult result) {
        try {
            // 비밀번호 변경, 상태 변경, verificationCode로 인증, verificationCode 삭제
            memberService.changePassword(passwordVerificationCode, memberChangePasswordForm.getNewPassword(), memberChangePasswordForm.getNewPasswordAgain());
        }
        // verificationCode가 유효하지 않거나, verificationCodeExpiration이 만료되었으면
        catch (NoSuchMemberException | VerificationInvalidException e) {
            model.addAttribute("message", e.getMessage());
            return "message/message";
        }
        // 비밀번호가 유효하지 않으면
        catch (MemberSignUpValidationException e) {
            if (e.getReason().equals(MemberCause.PASSWORD)) {
                result.addError(new FieldError("memberChangePasswordForm", "newPassword", e.getMessage()));
                result.addError(new FieldError("memberChangePasswordForm", "newPasswordAgain", e.getMessage()));

                model.addAttribute("passwordVerificationCode", passwordVerificationCode);
                model.addAttribute("memberChangePasswordForm", memberChangePasswordForm);

                // 잘못된 값이 들어오면, 다시 로그인 폼으로 돌아간다.
                return "auth/member/changePasswordForm";
            }
        }

        // 모든 작업 성공 시, 완료 메시지 출력
        model.addAttribute("message", new StringBuffer().append(PASSWORD.getMessage()).append(CHANGE.getMessage()).append(SUCCESS.getMessage()).toString());
        return "message/message";
    }
}
