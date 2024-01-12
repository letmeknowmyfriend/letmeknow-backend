package com.letmeknow.auth.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.letmeknow.auth.service.AuthService;
import com.letmeknow.auth.service.JwtService;
import com.letmeknow.dto.Response;
import com.letmeknow.message.messages.Messages;
import com.letmeknow.service.DeviceTokenService;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static com.letmeknow.auth.messages.JwtMessages.REFRESH_TOKEN;
import static com.letmeknow.auth.messages.MemberMessages.SIGN_IN;
import static com.letmeknow.auth.messages.MemberMessages.SIGN_OUT;
import static com.letmeknow.auth.service.JwtService.BEARER;
import static com.letmeknow.auth.service.JwtService.REFRESH_TOKEN_HEADER;
import static com.letmeknow.enumstorage.response.Status.SUCCESS;
import static com.letmeknow.message.messages.Messages.NOT_FOUND;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {
    private final AuthService authService;
    private final ObjectMapper objectMapper;

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

            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.setStatus(HttpStatus.SC_OK);
            response.getWriter().write(objectMapper.writeValueAsString(Response.builder()
                .status(SUCCESS.getStatus())
                .message(SIGN_OUT.getMessage() + Messages.SUCCESS.getMessage())
                .build()));
        }
        // 들어오는 값이 이상할 때
        catch (IllegalArgumentException e) {
            // 400 Bad Request
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
        } catch (JsonProcessingException e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
