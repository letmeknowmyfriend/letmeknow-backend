package com.letmeknow.auth.handler;

import com.letmeknow.dto.temporarymember.TemporaryMemberDto;
import com.letmeknow.exception.auth.InvalidRequestException;
import com.letmeknow.exception.member.MemberStateException;
import com.letmeknow.exception.member.NoSuchMemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import com.letmeknow.enumstorage.errormessage.auth.EmailErrorMessage;
import com.letmeknow.enumstorage.errormessage.auth.LogInErrorMessage;
import com.letmeknow.enumstorage.errormessage.auth.PasswordErrorMessage;
import com.letmeknow.message.messages.EmailMessage;
import com.letmeknow.exception.member.temporarymember.NoSuchTemporaryMemberException;
import com.letmeknow.service.member.MemberService;
import com.letmeknow.service.member.TemporaryMemberService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberLogInFailureHandler implements AuthenticationFailureHandler {
    private final TemporaryMemberService temporaryMemberService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        String email = (String) request.getAttribute("email");

        // 해당 회원이 없는 경우
        if (exception instanceof UsernameNotFoundException) {
            // 임시 회원을 찾아본다.
            try
            {
                // 임시 회원이 있다면,
                temporaryMemberService.findTemporaryMemberByEmail(email);

                // 이메일 재전송 요청 페이지로 redirect
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader("Location", "/api/auth/member/notice/verification-email/v1?email=" + email);
                return;
            }
            // 임시회원도 없으면
            catch (NoSuchTemporaryMemberException e)
            {
                // 회원가입 페이지로 redirect
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader("Location", "/api/auth/signup/v1");
                return;
            }
        }
        // 계정이 잠겨있는 경우
        else if (exception instanceof MemberStateException) {
            // 잠긴 계정 알림 페이지로 redirect
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("Location", "/api/auth/member/notice/locked/v1?email=" + email);
            return;
        }
        // 비밀번호가 틀린 경우
        else if (exception instanceof BadCredentialsException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // 요청이 유효하지 않은 경우
        else if (exception instanceof InvalidRequestException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            // UTF-8로 인코딩, body에 메시지 담아서 보내기
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(exception.getMessage());
            return;
        }
    }
}
