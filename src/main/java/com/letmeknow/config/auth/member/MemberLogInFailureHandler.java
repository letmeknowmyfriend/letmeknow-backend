package com.letmeknow.config.auth.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import com.letmeknow.enumstorage.errormessage.auth.EmailErrorMessage;
import com.letmeknow.enumstorage.errormessage.auth.LogInErrorMessage;
import com.letmeknow.enumstorage.errormessage.auth.PasswordErrorMessage;
import com.letmeknow.enumstorage.message.EmailMessage;
import com.letmeknow.exception.member.temporarymember.NoSuchTemporaryMemberException;
import com.letmeknow.service.member.MemberService;
import com.letmeknow.service.member.TemporaryMemberService;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

@Component
@RequiredArgsConstructor
public class MemberLogInFailureHandler implements AuthenticationFailureHandler {
    private final TemporaryMemberService temporaryMemberService;
    private final MemberService memberService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        //해당 회원이 없는 경우
        if (exception instanceof UsernameNotFoundException) {
            String email = request.getParameter("email");

            //임시 회원도 찾아본다.
            try
            {
                //임시 회원이 있다면, 이메일 인증 재전송
                temporaryMemberService.resendVerificationEmailByEmail(email);

                //이메일 인증 알림 페이지로 redirect
                response.sendRedirect("/auth/member/notice/verification-email");
                return;
            }
            //임시회원도 없으면
            catch (NoSuchTemporaryMemberException e)
            {
                //로그인 페이지로 redirect
                response.sendRedirect("/auth/login?error=email&exception=" + URLEncoder.encode(exception.getMessage(), "UTF-8"));
                return;
            }
            //메일 전송 실패하면
            catch (MessagingException e) {
                response.sendRedirect("/auth/login?error=email&exception=" + URLEncoder.encode(EmailMessage.VERIFICATION_EMAIL_SEND_FAIL.getMessage(), "UTF-8") + "&email=" + email);
                return;
            }
        }

        //유효한 이메일이 아닌 경우
        if (exception.getMessage().equals(EmailErrorMessage.NOT_VALID_EMAIL.getMessage())) {
            response.sendRedirect("/auth/login?error=email&exception=" + URLEncoder.encode(exception.getMessage(), "UTF-8"));
            return;
        }

        String email = request.getParameter("email");

        //비밀번호가 비어있는 경우
        if (exception.getMessage().equals(PasswordErrorMessage.PASSWORD_IS_EMPTY.getMessage())) {
            response.sendRedirect("/auth/login?error=password&exception=" + URLEncoder.encode(exception.getMessage(), "UTF-8") + "&email=" + email);
            return;
        }

        //비밀번호가 틀린 경우
        if (exception.getMessage().equals(PasswordErrorMessage.PASSWORD_DOES_NOT_MATCH.getMessage())) {
            response.sendRedirect("/auth/login?error=password&exception=" + URLEncoder.encode(exception.getMessage(), "UTF-8") + "&email=" + email);
            return;
        }

        //로그인 시도 횟수 초과
        if (exception.getMessage().equals(LogInErrorMessage.LOG_IN_ATTEMPT_EXCEEDED.getMessage())) {
            //비밀번호 변경 이메일 전송
            try {
                memberService.sendChangePasswordEmail(email);

                //비밀번호 변경 알림 페이지로 redirect
                response.sendRedirect("/auth/member/notice/change-password");
                return;

            //메일 전송 실패하면
            } catch (MessagingException e) {
                response.sendRedirect("/auth/login?error=email&exception=" + URLEncoder.encode(EmailMessage.VERIFICATION_EMAIL_SEND_FAIL.getMessage(), "UTF-8") + "&email=" + email);
                return;
            }
        }
    }
}
