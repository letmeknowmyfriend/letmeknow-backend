package com.letmeknow.config.auth.oauth;

import com.letmeknow.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import com.letmeknow.service.auth.jwt.JwtService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {
    private final JwtService jwtService;
    private final NotificationService notificationService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        String accessToken = "";
        String refreshToken = "";

        Cookie[] requestCookies = request.getCookies();
        if (requestCookies != null) {
            for (Cookie cookie : requestCookies) {
                if (cookie.getName().equals("accessToken")) {
                    accessToken = cookie.getValue();
                }
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        AtomicBoolean subscriptionJob = new AtomicBoolean(false);
        if (!accessToken.isBlank()) {
            jwtService.extractEmailFromToken(accessToken)
                    .ifPresent(email -> {
                        String deviceToken = request.getParameter("deviceToken");

                        if (deviceToken != null) {
                            // 회원의 기기 토큰을 찾고, FCM 구독을 삭제한다.
                            notificationService.whenMemberLogOut_DeleteDeviceToken_UnsubscribeFCM(email, deviceToken);
                            subscriptionJob.set(true);
                        }
                    });
        }

        if (!refreshToken.isBlank()) {
            jwtService.deleteByRefreshToken(response, refreshToken);

            // 앞에서 accessToken이 없어 구독 해제를 하지 못했을 경우, 다시 구독 해제를 시도한다.
            if (!subscriptionJob.get()) {
                jwtService.extractEmailFromToken(refreshToken)
                        .ifPresent(email -> {
                            String deviceToken = request.getParameter("deviceToken");

                            if (deviceToken != null) {
                                // 회원의 기기 토큰을 찾고, FCM 구독을 삭제한다.
                                notificationService.whenMemberLogOut_DeleteDeviceToken_UnsubscribeFCM(email, deviceToken);
                                subscriptionJob.set(true);
                            }
                        });
            }
        }

        // 클라이언트의 모든 토큰을 삭제한다.
        jwtService.deleteAllTokensFromClient(response);

        try {
            response.sendRedirect("/auth/login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
