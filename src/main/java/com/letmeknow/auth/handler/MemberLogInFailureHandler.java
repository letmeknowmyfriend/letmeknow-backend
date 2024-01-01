package com.letmeknow.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letmeknow.dto.Response;
import com.letmeknow.exception.auth.InvalidRequestException;
import com.letmeknow.exception.member.MemberStateException;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import com.letmeknow.exception.member.temporarymember.NoSuchTemporaryMemberException;
import com.letmeknow.service.member.TemporaryMemberService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.letmeknow.auth.messages.MemberMessages.EMAIL;
import static com.letmeknow.auth.messages.MemberMessages.VERIFICATION;
import static com.letmeknow.enumstorage.response.Status.FAIL;
import static com.letmeknow.message.messages.Messages.PLEASE;
import static com.letmeknow.message.messages.Messages.REQUIRED;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

@Component
@RequiredArgsConstructor
public class MemberLogInFailureHandler implements AuthenticationFailureHandler {
    private final TemporaryMemberService temporaryMemberService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        String email = (String) request.getAttribute("email");

        response.setCharacterEncoding("UTF-8");

        // 해당 회원이 없는 경우
        if (exception instanceof UsernameNotFoundException) {
            response.setStatus(SC_UNAUTHORIZED);

            // 임시 회원을 찾아본다.
            try
            {
                // 임시 회원이 있다면,
                temporaryMemberService.findTemporaryMemberByEmail(email);

                response.getWriter().write(objectMapper.writeValueAsString(Response.builder()
                    .status(FAIL.getStatus())
                    .message(EMAIL.getMessage() + VERIFICATION.getMessage() + REQUIRED.getMessage() + PLEASE.getMessage())
                    .build()
                ));

                return;
            }
            // 임시회원도 없으면
            catch (NoSuchTemporaryMemberException e)
            {
                response.getWriter().write(objectMapper.writeValueAsString(Response.builder()
                    .status(FAIL.getStatus())
                    .message(exception.getMessage())
                    .build()
                ));

                return;
            }
        }
        // 계정이 잠겨있는 경우
        else if (exception instanceof MemberStateException) {
            response.setStatus(SC_UNAUTHORIZED);

            response.getWriter().write(objectMapper.writeValueAsString(Response.builder()
                .status(FAIL.getStatus())
                .message(exception.getMessage())
                .build()
            ));

            return;
        }
        // 비밀번호가 틀린 경우
        else if (exception instanceof BadCredentialsException) {
            response.setStatus(SC_UNAUTHORIZED);

            response.getWriter().write(objectMapper.writeValueAsString(Response.builder()
                .status(FAIL.getStatus())
                .message(exception.getMessage())
                .build()
            ));

            return;
        }
        // 요청이 유효하지 않은 경우
        else if (exception instanceof InvalidRequestException) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);

            response.getWriter().write(objectMapper.writeValueAsString(
                Response.builder()
                .status(FAIL.getStatus())
                .message(exception.getMessage())
                .build()
            ));

            return;
        }
    }
}
