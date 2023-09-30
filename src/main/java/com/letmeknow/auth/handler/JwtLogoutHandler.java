package com.letmeknow.auth.handler;

import com.letmeknow.exception.member.NoSuchMemberException;
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
        try {
            // Header에서 accessToken, refreshToken 추출
            String accessToken = jwtService.extractAccessToken(request);
            String refreshToken = jwtService.extractRefreshToken(request);

            AtomicBoolean subscriptionJob = new AtomicBoolean(false);
            if (!accessToken.isBlank()) {
                jwtService.validateAndExtractEmailFromToken(accessToken)
                    .ifPresent(email -> {
                        String deviceToken = request.getParameter("DeviceToken");

                        if (deviceToken != null) {
                            // 회원의 기기 토큰을 찾고, FCM 구독을 삭제한다.
                            try {
                                notificationService.whenMemberLogOut_DeleteDeviceToken_UnsubscribeFCM(email, deviceToken);
                            } catch (NoSuchMemberException e) {
                                throw new RuntimeException(e);
                            }
                            subscriptionJob.set(true);
                        }
                    });
            }

            if (!refreshToken.isBlank()) {
                jwtService.deleteByRefreshToken(response, refreshToken);

                // 앞에서 accessToken이 없어 구독 해제를 하지 못했을 경우, 다시 구독 해제를 시도한다.
                if (!subscriptionJob.get()) {
                    jwtService.validateAndExtractEmailFromToken(refreshToken)
                        .ifPresent(email -> {
                            String deviceToken = request.getParameter("DeviceToken");

                            if (deviceToken != null) {
                                // 회원의 기기 토큰을 찾고, FCM 구독을 삭제한다.
                                try {
                                    notificationService.whenMemberLogOut_DeleteDeviceToken_UnsubscribeFCM(email, deviceToken);
                                } catch (NoSuchMemberException e) {
                                    throw new RuntimeException(e);
                                }
                                subscriptionJob.set(true);
                            }
                        });
                }
            }

//        // 클라이언트의 모든 토큰을 삭제한다.
//        jwtService.deleteAllTokensFromClient(response);
        }
        catch (RuntimeException e) {
            // 회원을 찾을 수 없는 경우, 로그인 페이지로 이동
        }
        finally {
            try {
                response.sendRedirect("/auth/login");
            } catch (IOException ioException) {
                throw new RuntimeException(ioException);
            }
        }
    }
}
