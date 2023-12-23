package com.letmeknow.auth.filter.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.letmeknow.auth.handler.MemberLogInFailureHandler;
import com.letmeknow.auth.handler.MemberLogInSuccessHandler;
import com.letmeknow.dto.auth.SignInAPIRequest;
import com.letmeknow.exception.auth.InvalidRequestException;
import com.letmeknow.service.DeviceTokenService;
import org.apache.http.entity.ContentType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.letmeknow.auth.messages.MemberMessages.EMAIL;
import static com.letmeknow.auth.messages.MemberMessages.PASSWORD;
import static com.letmeknow.message.messages.Messages.*;
import static com.letmeknow.message.messages.NotificationMessages.DEVICE_TOKEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class MemberAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final ObjectMapper objectMapper;
    private final MemberLogInSuccessHandler memberLogInSuccessHandler;
    private final MemberLogInFailureHandler memberLogInFailureHandler;

    public MemberAuthenticationFilter(ObjectMapper objectMapper, AuthenticationManager authenticationManager, MemberLogInSuccessHandler memberLogInSuccessHandler, MemberLogInFailureHandler memberLogInFailureHandler) {
        super("/api/auth/signin/v1", authenticationManager);

        this.objectMapper = objectMapper;
        this.memberLogInSuccessHandler = memberLogInSuccessHandler;
        this.memberLogInFailureHandler = memberLogInFailureHandler;

        setAuthenticationManager(authenticationManager);
        setAuthenticationSuccessHandler(memberLogInSuccessHandler);
        setAuthenticationFailureHandler(memberLogInFailureHandler);
    }

    //로그인 요청이 들어오면 로그인 시도하는 메소드
    //1. username, password 받아서
    //2. 정상인지 로그인 시도 -> AuthenticationManager로 로그인 시도
    //3. PrincipalDetailsService 호출 -> loadUserByUsername() 함수 실행
    //4. PrincipalDetails를 세션에 담고 (권한 관리를 위해서)
    //5. JWT를 만들어서 응답해주면 됨
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        // request의 method가 POST가 아니거나, Content-Type이 application/json이 아니면 예외 발생
        if (request.getContentType() == null || !request.getMethod().equals("POST") || !request.getContentType().contains(APPLICATION_JSON_VALUE)) {
            throw new InvalidRequestException(new StringBuffer().append(REQUEST.getMessage()).append(INVALID.getMessage()).toString());
        }

        // DeviceToken이 없으면 예외 발생
        String deviceToken = Optional.ofNullable(request.getHeader(DeviceTokenService.DEVICE_TOKEN))
            .orElseThrow(() -> new InvalidRequestException(new StringBuffer().append(DEVICE_TOKEN.getMessage()).append(NOT_FOUND.getMessage()).toString()));

        // ToDo: 테스트 할 때만 끔
        // DeviceToken 유효성 검사
        try {
            FirebaseMessaging.getInstance().send(Message.builder()
                        .setToken(deviceToken)
                .build());
        } catch (FirebaseMessagingException e) {
            throw new InvalidRequestException(new StringBuffer().append(DEVICE_TOKEN.getMessage()).append(INVALID.getMessage()).toString());
        }

        // DeviceToken을 request의 attribute에 담는다.
        request.setAttribute("DeviceToken", deviceToken);

        // request의 body에서 email과 password를 받아서 SignInAPIRequest 객체로 만든다.
        SignInAPIRequest signInAPIRequest = objectMapper.readValue(StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8), SignInAPIRequest.class);

        // email이나 password가 없으면 예외 발생
        String email = Optional.ofNullable(signInAPIRequest.getEmail())
            .orElseThrow(() -> new InvalidRequestException(new StringBuffer().append(EMAIL.getMessage()).append(NOT_FOUND.getMessage()).toString()));
        String password = Optional.ofNullable(signInAPIRequest.getPassword())
                .orElseThrow(() -> new InvalidRequestException(new StringBuffer().append(PASSWORD.getMessage()).append(NOT_FOUND.getMessage()).toString()));

        // trim()으로 공백 제거
        email = email.trim();
        password = password.trim();

        // 로그인 실패 시 활용 용도
        request.setAttribute("email", email);

        // authentication 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);

        return super.getAuthenticationManager().authenticate(authentication);
        // MemberAuthenticationProvider의 authenticate() 함수가 실행됨
    }
}
