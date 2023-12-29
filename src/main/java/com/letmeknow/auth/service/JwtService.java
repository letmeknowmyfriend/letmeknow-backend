package com.letmeknow.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.letmeknow.auth.entity.DeviceToken;
import com.letmeknow.auth.entity.RefreshToken;
import com.letmeknow.auth.repository.devicetoken.DeviceTokenRepository;
import com.letmeknow.auth.repository.refreshtoken.RefreshTokenRepository;
import com.letmeknow.entity.member.Member;
import com.letmeknow.exception.auth.jwt.NoAccessTokenException;
import com.letmeknow.exception.auth.jwt.NoRefreshTokenException;
import com.letmeknow.exception.auth.jwt.NoSuchDeviceTokenException;
import com.letmeknow.exception.auth.jwt.NoSuchRefreshTokenInDBException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.service.SubscriptionService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.letmeknow.auth.messages.JwtMessages.ACCESS_TOKEN;
import static com.letmeknow.auth.messages.JwtMessages.REFRESH_TOKEN;
import static com.letmeknow.auth.messages.MemberMessages.MEMBER;
import static com.letmeknow.message.messages.Messages.*;
import static com.letmeknow.message.messages.NotificationMessages.DEVICE_TOKEN;

@Getter
@Service
// Transactional 붙이지 마
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final SubscriptionService subscriptionService;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.token.access.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.token.refresh.expiration}")
    private long refreshTokenExpiration;

    public static final String ACCESS_TOKEN_HEADER = "Authorization";

    public static final String REFRESH_TOKEN_HEADER = "AuthorizationRefresh";

    public static final String BEARER = "Bearer ";

    // access token, refresh token을 발급, DB에 저장한다.
    @Transactional
    public String[] issueJwts(String email, Member member, DeviceToken deviceToken) {
        // refresh token을 발급한다.
        String newRefreshToken = JWT.create()
            .withIssuer("LetMeKnow")
            .withSubject("refreshToken")
            .withIssuedAt(new Date(System.currentTimeMillis()))
            .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpiration))
            .withClaim("email", member.getEmail())
            .withClaim("issuedTime", System.currentTimeMillis())
            .sign(Algorithm.HMAC512(secret));

        // DeviceToken과 연관된 refreshToken이 DB에 있으면,
        // refreshToken을 업데이트한다.
        if (deviceToken.getRefreshToken() != null) {
            deviceToken.getRefreshToken().updateRefreshToken(newRefreshToken);
        }
        // DeviceToken과 연관된 refreshToken이 DB에 없으면,
        else {
            // 새로 발급한 refreshToken을 DB에 저장
            RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .member(member)
                .refreshToken(newRefreshToken)
                .deviceToken(deviceToken)
                .build();

            refreshTokenRepository.save(newRefreshTokenEntity);
        }

        memberRepository.save(member);

        // access token을 발급한다.
        String newAccessToken = JWT.create()
            .withIssuer("LetMeKnow")
            .withSubject("accessToken")
            .withIssuedAt(new Date(System.currentTimeMillis()))
            .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
            .withClaim("email", email)
            .withClaim("issuedTime", System.currentTimeMillis())
            .sign(Algorithm.HMAC512(secret));

        return new String[] {newAccessToken, newRefreshToken};
    }

    @Transactional(noRollbackFor = {JWTVerificationException.class, NoSuchDeviceTokenException.class, NoSuchMemberException.class}) // JWTVerificationException 발생해도 롤백 X
    public String[] reissueJwts(String email, String refreshToken, String deviceToken) throws NoSuchMemberException, IllegalArgumentException, NoSuchDeviceTokenException, NoSuchRefreshTokenInDBException, JWTVerificationException {
        // RefreshToken과 함께 DeviceTokenEntity를 찾아
        DeviceToken deviceTokenEntity = deviceTokenRepository.findByDeviceTokenWithRefreshToken(deviceToken)
            // Device Token이 DB에 없으면, Device Token이 바뀐 상황
            .orElseThrow(() -> {
                // RefreshToken을 DB에서 삭제
                refreshTokenRepository.deleteByRefreshToken(refreshToken);

                Member member = memberRepository.findNotDeletedByEmailWithRefreshTokenAndSubscriptionAndDeviceToken(email)
                    .orElseThrow(() -> new NoSuchMemberException(new StringBuffer().append(SUCH.getMessage()).append(MEMBER.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

                // 만료된 Device Token으로 구독한 모든 Topic을 구독 해제한다.
                subscriptionService.unsubscribeFromAllTopics(deviceToken, member);

                throw new NoSuchDeviceTokenException(new StringBuffer().append(SUCH.getMessage()).append(DEVICE_TOKEN.getMessage()).append(NOT_EXISTS.getMessage()).toString());
            });

        // 일치하는 Device Token이 있으면
        // 그 RefreshToken이 들어온 RefreshToken과 일치하는지 확인
        // 유저가 보낸 Refresh Token와 다르면, 유효하지 않은 Refresh Token이므로
        RefreshToken refreshTokenEntity = Optional.ofNullable(deviceTokenEntity.getRefreshToken())
            // RefreshToken이 DB에 없으면
            .orElseThrow(() -> {
                throw new JWTVerificationException(new StringBuffer().append(REFRESH_TOKEN.getMessage()).append(INVALID.getMessage()).toString());
            });

        // RefreshToken이 있지만, 유저가 보낸 Refresh Token와 다르면, 이미 사용된 Refresh Token이므로
        if (!refreshTokenEntity.getRefreshToken().equals(refreshToken)) {
            // 해당 DeviceToken을 구독 해제
            subscriptionService.unsubscribeFromAllTopics(deviceTokenEntity.getDeviceToken(), deviceTokenEntity.getMember());

            // RefreshToken을 DB에서 삭제
            // DeviceToken도 삭제
            deviceTokenRepository.delete(deviceTokenEntity);

            // 예외 발생
            throw new JWTVerificationException(new StringBuffer().append(REFRESH_TOKEN.getMessage()).append(INVALID.getMessage()).toString());
        }

        // refreshToken이 유효하면
        // access token을 발급한다.
        String newAccessToken = JWT.create()
            .withIssuer("LetMeKnow")
            .withSubject("accessToken")
            .withIssuedAt(new Date(System.currentTimeMillis()))
            .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
            .withClaim("email", email)
            .withClaim("issuedTime", System.currentTimeMillis())
            .sign(Algorithm.HMAC512(secret));

        // refresh token을 발급한다.
        String newRefreshToken = JWT.create()
            .withIssuer("LetMeKnow")
            .withSubject("refreshToken")
            .withIssuedAt(new Date(System.currentTimeMillis()))
            .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpiration))
            .withClaim("email", email)
            .withClaim("issuedTime", System.currentTimeMillis())
            .sign(Algorithm.HMAC512(secret));

        // 해당 refreshToken을 업데이트한다.
        refreshTokenEntity.updateRefreshToken(newRefreshToken);
        refreshTokenRepository.save(refreshTokenEntity);

        return new String[]{newAccessToken, newRefreshToken};
    }

    // refreshToken을 삭제한다.
    // 검증 안하고 그냥 삭제해도 될 듯? - 해시된 비번들만 들어있으니까
    @Transactional
    public void deleteRefreshToken(String refreshToken) throws JWTVerificationException {
        // refreshToken을 삭제한다.
        refreshTokenRepository.deleteByRefreshToken(refreshToken);
    }

    public String extractAccessToken(HttpServletRequest request) throws IllegalArgumentException {
        String accessToken = Optional.ofNullable(request.getHeader(ACCESS_TOKEN_HEADER))
            .orElseThrow(() -> new IllegalArgumentException(new StringBuffer().append(ACCESS_TOKEN.getMessage()).append(NOT_FOUND.getMessage()).toString()))
            .replace(BEARER, "");

        if (accessToken.isBlank()) {
            throw new IllegalArgumentException(new StringBuffer().append(ACCESS_TOKEN.getMessage()).append(NOT_FOUND.getMessage()).toString());
        }

        return accessToken;
    }

    /**
     * 헤더에서 AccessToken 추출
     * 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서
     * 헤더를 가져온 후 "Bearer"를 삭제(""로 replace)
     */
    public String validateAndExtractEmailFromAccessToken(String accessToken) throws NoAccessTokenException {
        try {
            // accessToken 값 검증
            String email = JWT.require(Algorithm.HMAC512(secret))
                .withIssuer("LetMeKnow")
                .withSubject("accessToken")
                .build() // 반환된 빌더로 JWT verifier 생성
                .verify(accessToken) // accessToken을 검증하고 유효하지 않다면 예외 발생
                .getClaim("email") // claim(Email) 가져오기
                .asString();

            return email;
        }
        catch (JWTVerificationException e) {
            throw new NoAccessTokenException(new StringBuffer().append(ACCESS_TOKEN.getMessage()).append(INVALID.getMessage()).toString());
        }
    }

    public String extractRefreshToken(HttpServletRequest request) throws IllegalArgumentException {
        String refreshToken = Optional.ofNullable(request.getHeader(REFRESH_TOKEN_HEADER))
            .orElseThrow(() -> new IllegalArgumentException(new StringBuffer().append(REFRESH_TOKEN.getMessage()).append(NOT_FOUND.getMessage()).toString()))
            .replace(BEARER, "");

        // refreshToken 값 검증
        if (refreshToken.isBlank()) {
            throw new IllegalArgumentException(new StringBuffer().append(REFRESH_TOKEN.getMessage()).append(NOT_FOUND.getMessage()).toString());
        }

        return refreshToken;
    }

    /**
     * 헤더에서 RefreshToken 추출
     * 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서
     * 헤더를 가져온 후 "Bearer"를 삭제(""로 replace)
     */
    public String validateAndExtractEmailFromRefreshToken(String refreshToken) throws NoRefreshTokenException {
        try {
            // refreshToken을 검증한다.
            String emailInRefreshToken = JWT.require(Algorithm.HMAC512(secret))
                .withIssuer("LetMeKnow")
                .withSubject("refreshToken")
                .build()
                .verify(refreshToken)
                .getClaim("email")
                .asString();

            return emailInRefreshToken;
        }
        catch (JWTVerificationException e) {
            throw new NoRefreshTokenException(new StringBuffer().append(REFRESH_TOKEN.getMessage()).append(INVALID.getMessage()).toString());
        }
    }

    /**
     * accessToken을 Header에 작성한다.
     */
    public void setAccessTokenOnHeader(HttpServletResponse response, String token) {
        response.setHeader(ACCESS_TOKEN_HEADER, BEARER + token);
    }

    /**
     * refreshToken을 Header에 작성한다.
     */
    public void setRefreshTokenOnHeader(HttpServletResponse response, String token) {
        response.setHeader(REFRESH_TOKEN_HEADER, BEARER + token);
    }
}
