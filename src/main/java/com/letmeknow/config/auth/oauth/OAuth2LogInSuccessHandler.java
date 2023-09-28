package com.letmeknow.config.auth.oauth;

import com.letmeknow.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.letmeknow.config.auth.PrincipalUserDetails;
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

        PrincipalUserDetails principalUserDetails = (PrincipalUserDetails) authentication.getPrincipal();

        String email = Optional.ofNullable(principalUserDetails.getUsername())
                .orElseThrow(() -> new EmailException(EmailErrorMessage.EMAIL_IS_EMPTY.getMessage()));

        //token들을 발급한다.
        jwtService.issueTokens(email, response);

        String deviceToken = request.getParameter("deviceToken");

        // 회원의 기기 토큰을 찾고, FCM 구독을 추가한다.
        notificationService.whenMemberLogIn_AddDeviceToken_AddFCMSubscription(email, deviceToken);

        //OAuth2 로그인 성공 시, 메인 페이지로 이동
        response.setStatus(HttpServletResponse.SC_OK);
        response.sendRedirect("/");
    }
}
