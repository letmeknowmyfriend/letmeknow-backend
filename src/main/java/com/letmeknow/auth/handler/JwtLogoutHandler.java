package com.letmeknow.auth.handler;

import com.letmeknow.auth.service.AuthService;
import com.letmeknow.auth.service.JwtService;
import com.letmeknow.service.DeviceTokenService;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static com.letmeknow.auth.messages.JwtMessages.REFRESH_TOKEN;
import static com.letmeknow.auth.service.JwtService.BEARER;
import static com.letmeknow.auth.service.JwtService.REFRESH_TOKEN_HEADER;
import static com.letmeknow.message.messages.Messages.NOT_FOUND;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {
    private final AuthService authService;
    private final JwtService jwtService;
    private final DeviceTokenService deviceTokenService;

    /**
     * 로그아웃 할 때는 accessToken과 refreshToken을 모두 보내야 함
     * @param request the HTTP request
     * @param response the HTTP response
     * @param authentication the current principal details
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            authService.signOut(request, response);
        }
        // 들어오는 값이 이상할 때
        catch (IllegalArgumentException e) {
            // 400 Bad Request
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
        }
    }
}
