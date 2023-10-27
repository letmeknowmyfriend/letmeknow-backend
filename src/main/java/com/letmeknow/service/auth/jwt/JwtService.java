package com.letmeknow.service.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.letmeknow.domain.auth.RefreshToken;
import com.letmeknow.domain.notification.DeviceToken;
import com.letmeknow.dto.jwt.JwtFindDtoWithDeviceToken;
import com.letmeknow.exception.auth.jwt.*;
import com.letmeknow.repository.notification.DeviceTokenRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import com.letmeknow.domain.member.Member;
import com.letmeknow.dto.jwt.JwtFindDto;
import com.letmeknow.enumstorage.errormessage.member.MemberErrorMessage;
import com.letmeknow.enumstorage.errormessage.auth.jwt.JwtErrorMessage;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.repository.jwt.JwtRepository;
import com.letmeknow.repository.member.MemberRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JwtService {
    private final JWT jwt;
    private final MemberRepository memberRepository;
    private final JwtRepository jwtRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.token.access.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.token.refresh.expiration}")
    private Long refreshTokenExpiration;

    public static final String ACCESS_TOKEN_HEADER = "Authorization";

    public static final String REFRESH_TOKEN_HEADER = "Authorization-refresh";

    public static final String BEARER = "Bearer ";

    public JwtFindDto findJwtFindDtoByRefreshToken(String refreshToken) throws NoSuchRefreshTokenInDBException {
        return jwtRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new NoSuchRefreshTokenInDBException(JwtErrorMessage.NO_SUCH_REFRESH_TOKEN.getMessage()))
                .toJwtFindDto();
    }

    public JwtFindDtoWithDeviceToken findJwtFindDtoWithDeviceTokenByRefreshToken(String refreshToken) throws NoSuchRefreshTokenInDBException {
        return jwtRepository.findByRefreshTokenWithDeviceToken(refreshToken)
                .orElseThrow(() -> new NoSuchRefreshTokenInDBException(JwtErrorMessage.NO_SUCH_REFRESH_TOKEN.getMessage()))
                .toJwtFindDtoWithDeviceToken();
    }

    /**
     * access token, refresh token을 발급, DB에 저장한다.
     * @param email
     * @param response
     */
    @Transactional
    public String[] issueTokens(String email, String deviceToken) throws NoSuchMemberException {
        //email로 member를 찾는다.
        Member member = memberRepository.findNotDeletedByEmailWithJwtAndDeviceToken(email, deviceToken)
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()));

        Set<DeviceToken> deviceTokens = member.getDeviceTokens().stream()
            .filter(deviceToken1 -> deviceToken1.getDeviceToken().equals(deviceToken))
            .collect(Collectors.toSet());

        // deviceToken이 없으면 deviceToken을 만든다.
        if (deviceTokens.isEmpty()) {
            DeviceToken newDeviceToken = DeviceToken.builder()
                .member(member)
                .deviceToken(deviceToken)
                .build();

            deviceTokens.add(newDeviceToken);

            deviceTokenRepository.save(newDeviceToken);
        }
        // deviceToken이 이미 있으면 관련된 JWT를 전부 삭제한다.
        else {
            deviceTokens.forEach(deviceToken1 -> {
                deviceToken1.getRefreshTokens().forEach((jwt1) -> {
                    jwt1.deleteRefreshToken();

                    jwtRepository.delete(jwt1);
                });
            });
        }

        //refresh token을 발급한다.
        String newRefreshToken = jwt.create()
                .withIssuer("LetMeKnow")
                .withSubject("refreshToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .withClaim("email", member.getEmail())
                .sign(Algorithm.HMAC512(secret));

        // 만료된 것 삭제
        Set<RefreshToken> memberRefreshTokens = member.getRefreshTokens();
        removeExpired(memberRefreshTokens);

        // 새로 발급한 refreshToken을 DB에 저장
        RefreshToken newJwt = RefreshToken.builder()
            .member(member)
            .deviceToken(deviceTokens.stream().findFirst().get())
            .refreshToken(newRefreshToken)
            .build();

        jwtRepository.save(newJwt);

        //access token을 발급한다.
        String newAccessToken = jwt.create()
                .withIssuer("LetMeKnow")
                .withSubject("accessToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .withClaim("email", email)
                .sign(Algorithm.HMAC512(secret));

        return new String[] {newAccessToken, newRefreshToken};
    }

    @Transactional
    public void deleteAllTokensWithDeviceToken(String deviceToken) {
        // deviceToken으로 연관된 JWT를 전부 삭제한다.
        Optional<DeviceToken> deviceTokenWithJWTs = deviceTokenRepository.findByDeviceTokenWithJWTs(deviceToken);
        deviceTokenWithJWTs.ifPresent(deviceToken1 -> deviceToken1.getRefreshTokens().forEach(jwt -> jwtRepository.delete(jwt)));
    }

//    @Transactional
//    public void reissueTokensAndSetTokensOnHeader(String email, String deviceToken, String refreshToken, HttpServletResponse response) throws NoSuchMemberException {
//        //email로 member를 찾는다.
//        Member member = memberRepository.findNotDeletedByEmailWithJwtAndDeviceToken(email, deviceToken)
//                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()));
//
//        Set<Jwt> memberJwts = member.getJwts();
//
//        // 만료된 것 삭제
//        removeExpired(memberJwts);
//
//        // 원래 있던 refreshToken을 찾아 삭제
//        memberJwts.stream()
//            .filter(jwt -> jwt.getRefreshToken().equals(refreshToken))
//            .findFirst()
//            .ifPresentOrElse(jwtEntity -> {
//                // DB에서 해당 refreshToken이 있을 때만
//                jwtEntity.removeRefreshToken();
//                jwtRepository.delete(jwtEntity);
//
//                //refresh token을 발급한다.
//                String newRefreshToken = jwt.create()
//                    .withIssuer("LetMeKnow")
//                    .withSubject("refreshToken")
//                    .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpiration))
//                    .withClaim("email", member.getEmail())
//                    .sign(Algorithm.HMAC512(secret));
//
//                // 새로 만든 refreshToken을 DB에 저장
//                Jwt newJwt = Jwt.builder()
//                    .member(member)
//                    .refreshToken(newRefreshToken)
//                    .build();
//
//                jwtRepository.save(newJwt);
//
//                //access token을 발급한다.
//                String newAccessToken = jwt.create()
//                    .withIssuer("LetMeKnow")
//                    .withSubject("accessToken")
//                    .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
//                    .withClaim("email", email)
//                    .sign(Algorithm.HMAC512(secret));
//
//                //access token, refresh token을 헤더에 실어서 보낸다.
//                setAccessTokenOnHeader(response, newAccessToken);
//                setRefreshTokenOnHeader(response, newRefreshToken);
//            },
//                // DB에 해당 refreshToken이 없으면, 예외 발생
//                () -> {
//                throw new NotValidRefreshTokenException(JwtErrorMessage.NOT_VALID_JWT.getMessage())
//            });
//    }

    /**
     * 헤더에서 AccessToken 추출
     * 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서
     * 헤더를 가져온 후 "Bearer"를 삭제(""로 replace)
     */
    public String extractAccessToken(HttpServletRequest request) throws NoAccessTokenException {
        return Optional.ofNullable(request.getHeader(ACCESS_TOKEN_HEADER))
            .orElseThrow(() -> new NoAccessTokenException(JwtErrorMessage.NO_ACCESS_TOKEN_IN_REQUEST_HEADER.getMessage()))
            .replace(BEARER, "");
    }

    /**
     * 헤더에서 RefreshToken 추출
     * 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서
     * 헤더를 가져온 후 "Bearer"를 삭제(""로 replace)
     */
    public String extractRefreshToken(HttpServletRequest request) throws NoRefreshTokenException {
        return Optional.ofNullable(request.getHeader(REFRESH_TOKEN_HEADER))
            .orElseThrow(() -> new NoRefreshTokenException(JwtErrorMessage.NO_REFRESH_TOKEN_IN_REQUEST_HEADER.getMessage()))
            .replace(BEARER, "");
    }

    public String validateAndExtractEmailFromToken(String token) throws JWTVerificationException {
        // 토큰 유효성 검사하는 데에 사용할 알고리즘이 있는 JWT verifier builder 반환
        return JWT.require(Algorithm.HMAC512(secret))
                .build() // 반환된 빌더로 JWT verifier 생성
                .verify(token) // accessToken을 검증하고 유효하지 않다면 예외 발생
                .getClaim("email") // claim(Email) 가져오기
                .asString();
    }

    @Transactional
    public boolean isRefreshTokenExists(HttpServletResponse response, String refreshToken) throws NoSuchRefreshTokenInDBException {
        RefreshToken jwt = jwtRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new NoSuchRefreshTokenInDBException(JwtErrorMessage.NO_SUCH_REFRESH_TOKEN.getMessage()));

        //DB의 refresh token과 일치하는지, 만료기간이 지났는지 확인
        if (!jwt.getRefreshToken().equals(refreshToken)) {
            //DB에서 지우고
            jwtRepository.delete(jwt);

//            //cookie에서 지우고
//            deleteToken("accessToken", response);
//            deleteToken("refreshToken", response);

            return false;
        }

        return true;
    }

    private void removeExpired(Set<RefreshToken> memberRefreshTokens) {
        // 만료된거 삭제하는 로직?
        memberRefreshTokens.forEach(jwt -> {
            if (jwt.isExpired()) {
                jwtRepository.delete(jwt);
            }
        });
    }

//    public void deleteAllTokensFromClient(HttpServletResponse response) {
//        deleteToken("accessToken", response);
//        deleteToken("refreshToken", response);
//    }

//    private static void deleteToken(String token, HttpServletResponse response) {
//        Cookie tokenCookie = new Cookie(token, "");
//        tokenCookie.setMaxAge(0);
//        tokenCookie.setHttpOnly(true);
////        tokenCookie.setSecure(true);
//        tokenCookie.setPath("/");
//        response.addCookie(tokenCookie);
//    }

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
