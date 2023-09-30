package com.letmeknow.auth.handler;

import com.letmeknow.enumstorage.errormessage.notification.DeviceTokenErrorMessage;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.exception.notification.DeviceTokenException;
import com.letmeknow.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.letmeknow.auth.PrincipalUserDetails;
import com.letmeknow.enumstorage.errormessage.auth.EmailErrorMessage;
import com.letmeknow.exception.auth.EmailException;
import com.letmeknow.service.auth.jwt.JwtService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2LogInSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final NotificationService notificationService;

    /**
     * 로그인 성공 시, JwtEntity를 생성하고 AccessToken과 RefreshToken을 Cookie에 담아 보낸다.
     * @param request the request which caused the successful authentication
     * @param response the response
     * @param authentication the <tt>Authentication</tt> object which was created during
     * the authentication process.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            PrincipalUserDetails principalUserDetails = (PrincipalUserDetails) authentication.getPrincipal();

            String email = Optional.ofNullable(principalUserDetails.getUsername())
                .orElseThrow(() -> new EmailException(EmailErrorMessage.EMAIL_IS_EMPTY.getMessage()));

            //token들을 발급한다.
            jwtService.issueTokensAndSetTokensOnHeader(email, response);

            String deviceToken = Optional.ofNullable(request.getParameter("DeviceToken"))
                    .orElseThrow(() -> new DeviceTokenException(DeviceTokenErrorMessage.DEVICE_TOKEN_IS_EMPTY.getMessage()));

            // 회원의 기기 토큰을 찾고, FCM 구독을 추가한다.
            if (deviceToken != null) {
                notificationService.whenMemberLogIn_AddDeviceToken_AddFCMSubscription(email, deviceToken);
            }
        } catch (NoSuchMemberException | DeviceTokenException e) {
            // 회원을 찾을 수 없거나, DeviceToken이 없으면, 로그인 페이지로 이동

            response.sendRedirect("/auth/login");
        }
    }
}
