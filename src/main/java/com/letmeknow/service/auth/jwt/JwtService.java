package com.letmeknow.service.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import com.letmeknow.domain.member.Member;
import com.letmeknow.domain.auth.Jwt;
import com.letmeknow.dto.jwt.JwtFindDto;
import com.letmeknow.enumstorage.errormessage.member.MemberErrorMessage;
import com.letmeknow.enumstorage.errormessage.auth.jwt.JwtErrorMessage;
import com.letmeknow.exception.auth.jwt.NoSuchJwtException;
import com.letmeknow.exception.auth.jwt.NotValidJwtException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.repository.jwt.JwtRepository;
import com.letmeknow.repository.member.MemberRepository;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Getter
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JwtService {
    private final JWT jwt;
    private final MemberRepository memberRepository;
    private final JwtRepository jwtRepository;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.token.access.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.token.refresh.expiration}")
    private Long refreshTokenExpiration;

    @Value("${jwt.token.access.header}")
    private String accessTokenHeader;

    @Value("${jwt.token.refresh.header}")
    private String refreshTokenHeader;

    private static final String BEARER = "Bearer ";

    public JwtFindDto findJwtFindDtoByRefreshToken(String refreshToken) {
        return jwtRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new NoSuchJwtException(JwtErrorMessage.NO_SUCH_REFRESH_TOKEN.getMessage()))
                .toJwtFindDto();
    }

    @Transactional
    public void deleteByRefreshToken(HttpServletResponse response, String refreshToken) {
        jwtRepository.deleteByRefreshToken(refreshToken);
    }

    /**
     * access token, refresh token을 발급, DB에 저장한다.
     * @param email
     * @param response
     */
    @Transactional
    public void issueTokens(String email, HttpServletResponse response) {
        //email로 member를 찾는다.
        Member member = memberRepository.findNotDeletedByEmail(email)
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()));

        //refresh token을 발급한다.
        String refreshToken = jwt.create()
                .withIssuer("myLittleStore")
                .withSubject("refreshToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .withClaim("email", member.getEmail())
                .sign(Algorithm.HMAC512(secret));

        jwtRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                        //이미 있으면, update
                        jwtEntity -> jwtEntity.updateRefreshToken(refreshToken),
                        //없으면, 새로 만든다.
                        () -> {
                            Jwt jwtEntity = Jwt.builder()
                                    .member(member)
                                    .refreshToken(refreshToken)
                                    .build();
                            jwtRepository.save(jwtEntity);
                        });

        //access token을 발급한다.
        String accessToken = jwt.create()
                .withIssuer("myLittleStore")
                .withSubject("accessToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .withClaim("email", email)
                .sign(Algorithm.HMAC512(secret));

        //access token, refresh token을 헤더에 실어서 보낸다.
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenOnCookie(response, accessToken);
        setRefreshTokenOnCookie(response, refreshToken);
    }

    @Transactional
    public Member reissueTokens(String refreshToken, HttpServletResponse response) {
        //refreshToken으로 member를 찾는다.
        Jwt jwt = jwtRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new NoSuchJwtException(JwtErrorMessage.NO_SUCH_REFRESH_TOKEN.getMessage()));

        Member member = jwt.getMember();

        //refresh token을 발급한다.
        String newRefreshToken = this.jwt.create()
                .withIssuer("myLittleStore")
                .withSubject("refreshToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .withClaim("email", member.getEmail())
                .sign(Algorithm.HMAC512(secret));

        jwtRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                        //이미 있으면, update
                        jwtEntity -> jwtEntity.updateRefreshToken(newRefreshToken),
                        //없으면, 새로 만든다.
                        () -> {
                            Jwt jwtEntity = Jwt.builder()
                                    .member(member)
                                    .refreshToken(newRefreshToken)
                                    .build();
                            jwtRepository.save(jwtEntity);
                        });

        //access token을 발급한다.
        String accessToken = this.jwt.create()
                .withIssuer("myLittleStore")
                .withSubject("accessToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .withClaim("email", member.getEmail())
                .sign(Algorithm.HMAC512(secret));

        //access token, refresh token을 헤더에 실어서 보낸다.
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenOnCookie(response, accessToken);
        setRefreshTokenOnCookie(response, refreshToken);

        return member;
    }

    public DecodedJWT decodeJwt(HttpServletResponse response, String token) {
        if (isTokenValid(token)) {
            return jwt.decodeJwt(token);
        } else {
            throw new NotValidJwtException(JwtErrorMessage.NOT_VALID_JWT.getMessage());
        }
    }

    /**
     * 헤더에서 AccessToken 추출
     * 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서
     * 헤더를 가져온 후 "Bearer"를 삭제(""로 replace)
     */
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessTokenHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    /**
     * 헤더에서 RefreshToken 추출
     * 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서
     * 헤더를 가져온 후 "Bearer"를 삭제(""로 replace)
     */
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshTokenHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    public Optional<String> extractEmailFromToken(String accessToken) {
        try {
            // 토큰 유효성 검사하는 데에 사용할 알고리즘이 있는 JWT verifier builder 반환
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secret))
                    .build() // 반환된 빌더로 JWT verifier 생성
                    .verify(accessToken) // accessToken을 검증하고 유효하지 않다면 예외 발생
                    .getClaim("email") // claim(Email) 가져오기
                    .asString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secret))
                    .build()
                    .verify(token);

            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Transactional
    public boolean isRefreshTokenExists(HttpServletResponse response, String refreshToken) {
        Jwt jwt = jwtRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new NoSuchJwtException(JwtErrorMessage.NO_SUCH_REFRESH_TOKEN.getMessage()));

        //DB의 refresh token과 일치하는지, 만료기간이 지났는지 확인
        if (!jwt.getRefreshToken().equals(refreshToken) || jwt.getExpiredAt().isBefore(LocalDateTime.now())) {
            //DB에서 지우고
            jwtRepository.delete(jwt);

            //cookie에서 지우고
            deleteToken("accessToken", response);
            deleteToken("refreshToken", response);

            return false;
        }

        return true;
    }

    public void deleteAllTokensFromClient(HttpServletResponse response) {
        deleteToken("accessToken", response);
        deleteToken("refreshToken", response);
    }

    private static void deleteToken(String token, HttpServletResponse response) {
        Cookie tokenCookie = new Cookie(token, "");
        tokenCookie.setMaxAge(0);
        tokenCookie.setHttpOnly(true);
//        tokenCookie.setSecure(true);
        tokenCookie.setPath("/");
        response.addCookie(tokenCookie);
    }

    /**
     * AccessToken 헤더 설정
     */
    private void setAccessTokenOnCookie(HttpServletResponse response, String accessToken) {
//        response.addHeader("Access-Control-Expose-Headers", accessTokenHeader);
//        response.addHeader(accessTokenHeader, BEARER + accessToken);

        Cookie cookie = new Cookie("accessToken", accessToken);
        cookie.setMaxAge(Math.toIntExact(accessTokenExpiration / 1000));
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * RefreshToken 헤더 설정
     */
    private void setRefreshTokenOnCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setMaxAge(Math.toIntExact(refreshTokenExpiration / 1000));
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
