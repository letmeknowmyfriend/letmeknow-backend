package com.letmeknow.auth.handler;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.letmeknow.dto.jwt.JwtFindDtoWithDeviceToken;
import com.letmeknow.enumstorage.errormessage.notification.DeviceTokenErrorMessage;
import com.letmeknow.exception.auth.jwt.NoAccessTokenException;
import com.letmeknow.exception.auth.jwt.NoRefreshTokenException;
import com.letmeknow.exception.auth.jwt.NoSuchDeviceTokenException;
import com.letmeknow.exception.auth.jwt.NoSuchRefreshTokenInDBException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.exception.notification.NotificationException;
import com.letmeknow.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import com.letmeknow.service.auth.jwt.JwtService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {
    private final JwtService jwtService;
    private final NotificationService notificationService;

    /**
     * 로그아웃 할 때는 accessToken과 refreshToken을 모두 보내야 함
     * @param request the HTTP request
     * @param response the HTTP response
     * @param authentication the current principal details
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            String deviceToken = Optional.ofNullable(request.getParameter("DeviceToken"))
                .orElseThrow(() -> new NotificationException(DeviceTokenErrorMessage.DEVICE_TOKEN_IS_EMPTY.getMessage()));

            // deviceToken으로 연관된 JWT를 전부 삭제한다.
            jwtService.deleteAllTokensWithDeviceToken(deviceToken);

            // Header에서 accessToken 추출
            String accessToken = jwtService.extractAccessToken(request);

            String email = jwtService.validateAndExtractEmailFromToken(accessToken);

            // 회원의 기기 토큰을 찾고, FCM 구독을 삭제한다.
            notificationService.whenMemberLogOut_UnsubscribeFCM_RemoveDeviceToken(email, deviceToken);
        }
        // AccessToken이 없는 경우 | DeviceToken이 없는 경우 | accessToken이 유효하지 않은 경우
        catch (NoAccessTokenException | NotificationException | JWTVerificationException e) {
            try {
                // refreshToken을 찾아 해결
                String refreshToken = jwtService.extractRefreshToken(request);

                String email = jwtService.validateAndExtractEmailFromToken(refreshToken);

                JwtFindDtoWithDeviceToken jwtFindDtoWithDeviceTokenByRefreshToken = jwtService.findJwtFindDtoWithDeviceTokenByRefreshToken(refreshToken);

                // 회원의 기기 토큰을 찾고, FCM 구독을 삭제한다.
                notificationService.whenMemberLogOut_UnsubscribeFCM_RemoveDeviceToken(email, jwtFindDtoWithDeviceTokenByRefreshToken.getDeviceTokenDto().getDeviceToken());
            }
            catch (JWTVerificationException ex) {
                // refreshToken이 유효하지 않은 경우, 로그인 페이지로 이동
            }
            catch (NoRefreshTokenException ex) {
                // refreshToken도 없는 경우, 로그인 페이지로 이동
            }
            catch (NoSuchRefreshTokenInDBException ex) {
                // refreshToken이 DB에 없는 경우, 로그인 페이지로 이동
            }
            catch (NoSuchMemberException ex) {
                // 회원을 찾을 수 없는 경우, 로그인 페이지로 이동
            }
            catch (NoSuchDeviceTokenException ex) {
                // DeviceToken이 DB와 매칭되지 않는 경우, 로그인 페이지로 이동
            }
        }
        // DeviceToken이 DB와 매칭되지 않는 경우
        catch (NoSuchDeviceTokenException e) {
            // DeviceToken이 DB와 매칭되지 않는 경우, 로그인 페이지로 이동
        }
        catch (NoSuchMemberException e) {
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
