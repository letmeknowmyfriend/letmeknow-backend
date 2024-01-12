package com.letmeknow.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letmeknow.auth.service.AuthService;
import com.letmeknow.dto.Response;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.exception.subscription.SubscriptionException;
import com.letmeknow.message.messages.Messages;
import com.letmeknow.service.DeviceTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.letmeknow.auth.messages.MemberMessages.SIGN_IN;
import static com.letmeknow.auth.service.JwtService.*;
import static com.letmeknow.enumstorage.response.Status.SUCCESS;
import static com.letmeknow.service.DeviceTokenService.DEVICE_TOKEN;
import static javax.servlet.http.HttpServletResponse.SC_GONE;

@Component
@RequiredArgsConstructor
public class MemberLogInSuccessHandler implements AuthenticationSuccessHandler {
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    /**
     * 로그인 성공 시, JwtEntity를 생성하고 AccessToken과 RefreshToken을 Header에 담아 보낸다.
     * @param request the request which caused the successful authentication
     * @param response the response
     * @param authentication the <tt>Authentication</tt> object which was created during
     * the authentication process.
     * @throws IOException
     * @throws ServletException
     */
    // Transactional 붙이지 말 것
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        try {
            String email = authentication.getName();
            String deviceToken = (String) request.getAttribute(DEVICE_TOKEN);

            // Transaction 있음
            String[] accessTokenAndRefreshToken = authService.whenMemberSignIn_IssueJwts_StoreDeviceToken_SubscribeToAllTopics(email, deviceToken);

            // access token, refresh token을 헤더에 실어서 보낸다.
            response.setCharacterEncoding("UTF-8");
            response.setHeader(ACCESS_TOKEN_HEADER, BEARER + accessTokenAndRefreshToken[0]);
            response.setHeader(REFRESH_TOKEN_HEADER, BEARER + accessTokenAndRefreshToken[1]);
            response.getWriter().write(objectMapper.writeValueAsString(Response.builder()
                .status(SUCCESS.getStatus())
                .message(SIGN_IN.getMessage() + Messages.SUCCESS.getMessage())
                .build()));
        }
        catch (NoSuchMemberException | SubscriptionException e) {
            // 회원을 찾을 수 없거나, 구독에 오류가 발생하면, 로그인 페이지로 이동
            response.setStatus(SC_GONE);
            response.setHeader("Location", "/auth/login");
        }
    }
}
