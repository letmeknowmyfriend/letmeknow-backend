package com.letmeknow.auth.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.letmeknow.auth.entity.DeviceToken;
import com.letmeknow.auth.entity.RefreshToken;
import com.letmeknow.auth.repository.devicetoken.DeviceTokenRepository;
import com.letmeknow.auth.repository.refreshtoken.RefreshTokenRepository;
import com.letmeknow.entity.member.Member;
import com.letmeknow.exception.auth.jwt.NoSuchDeviceTokenException;
import com.letmeknow.exception.auth.jwt.NoSuchRefreshTokenInDBException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.exception.subscription.SubscriptionException;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.service.DeviceTokenService;
import com.letmeknow.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Optional;

import static com.letmeknow.auth.messages.MemberMessages.MEMBER;
import static com.letmeknow.message.messages.Messages.NOT_EXISTS;
import static com.letmeknow.message.messages.Messages.SUCH;

@Service
// Transactional 붙이지 마
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final DeviceTokenService deviceTokenService;
    private final SubscriptionService subscriptionService;

    private final RefreshTokenRepository refreshTokenRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public String[] whenMemberSignIn_IssueJwts_StoreDeviceToken_SubscribeToAllTopics(String email, String deviceToken) throws NoSuchMemberException, SubscriptionException {
        // email로 member를 찾는다.
        Member member = memberRepository.findNotDeletedByEmailWithRefreshTokenAndSubscriptionAndDeviceToken(email)
            .orElseThrow(() -> new NoSuchMemberException(new StringBuffer().append(SUCH.getMessage()).append(MEMBER.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        // 회원의 DeviceToken가 DB에 저장되어 있는지 확인하고, 없으면 저장한다.
        DeviceToken deviceTokenEntity = deviceTokenService.storeDeviceToken(deviceToken, member);

        String[] issuedJwts = jwtService.issueJwts(email, member, deviceTokenEntity);

        subscriptionService.subscribeToAllTopics(deviceToken, member);

        return issuedJwts;
    }

    // Transactional 필요 없음
    public String[] reissueJwts(HttpServletRequest request) throws NoSuchMemberException, IllegalArgumentException, NoSuchDeviceTokenException, NoSuchRefreshTokenInDBException, JWTVerificationException {
        // Header에서 deviceToken 추출
        String deviceToken = deviceTokenService.extractDeviceTokenFromHeader(request);

        // ToDo: 테스트 할 때만 끔 // 추출한 deviceToken이 유효한지 확인한다.
//        deviceTokenService.validateAndExtractDeviceToken(deviceToken);

        // Header에서 refreshToken 추출
        String refreshToken = jwtService.extractRefreshToken(request);
        String emailFromRefreshToken = jwtService.validateAndExtractEmailFromRefreshToken(refreshToken);

        // token들을 재발급한다.
        return jwtService.reissueJwts(emailFromRefreshToken, refreshToken, deviceToken);
    }

    @Transactional
    public void signOut(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException {
        // Header에서 Refresh Token 추출
        String refreshToken = jwtService.extractRefreshToken(request);

        // Header에서 Device Token 추출
        String deviceToken = deviceTokenService.extractDeviceTokenFromHeader(request);

        // Refresh Token을 찾는다.
        refreshTokenRepository.findByRefreshToken(refreshToken)
            .ifPresentOrElse(
                refreshTokenEntity -> {
                    // Refresh Token이 DB에 있으면,
                    // Refresh Token Id로 회원과 Device Token, Subscription을 찾는다.
                    long refreshTokenEntityId = refreshTokenEntity.getId();
                    memberRepository.findNotDeletedByRefreshTokenIdWithSubscriptionAndDeviceToken(refreshTokenEntityId)
                        .ifPresent(member -> {
                            // 구독 해제 처리
                            subscriptionService.unsubscribeFromAllTopics(deviceToken, member);
                        });
                },
                () -> {
                    // Refresh Token이 DB에 없으면,
                    deviceTokenRepository.findByDeviceToken(deviceToken)
                        .ifPresent(deviceTokenEntity -> {
                            // Device Token Id으로 회원과 Subscription을 찾는다.
                            memberRepository.findNotDeletedByDeviceTokenIdWithSubscription(deviceTokenEntity.getId())
                                .ifPresent(member -> {
                                    // 구독 해제 처리
                                    subscriptionService.unsubscribeFromAllTopics(deviceToken, member);
                                });
                        });
                });

        // Device Token 삭제
        deviceTokenRepository.deleteByDeviceToken(deviceToken);

        // ToDo: null 해도 되는지 테스트
        // Header에서 Access Token 삭제
        response.setHeader(JwtService.ACCESS_TOKEN_HEADER, null);
        // ToDo: null 해도 되는지 테스트
        // Header에서 Refresh Token 삭제
        response.setHeader(JwtService.REFRESH_TOKEN_HEADER, null);
    }
}
