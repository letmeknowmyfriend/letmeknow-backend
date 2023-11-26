package com.letmeknow.auth.service;

import com.letmeknow.auth.entity.RefreshToken;
import com.letmeknow.auth.repository.refreshtoken.RefreshTokenRepository;
import com.letmeknow.exception.auth.jwt.NoSuchRefreshTokenInDBException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.letmeknow.auth.messages.JwtMessages.REFRESH_TOKEN;
import static com.letmeknow.message.messages.Messages.NOT_EXISTS;
import static com.letmeknow.message.messages.Messages.SUCH;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken findByRefreshToken(String refreshToken) throws NoSuchRefreshTokenInDBException {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
            .orElseThrow(() -> new NoSuchRefreshTokenInDBException(new StringBuffer().append(SUCH.getMessage()).append(REFRESH_TOKEN.getMessage()).append(NOT_EXISTS.getMessage()).toString()));
    }
}
