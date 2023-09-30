package com.letmeknow.service.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
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
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.repository.jwt.JwtRepository;
import com.letmeknow.repository.member.MemberRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

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
    private String refreshTokenHeader = "Authorization-refresh";

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
    public void issueTokensAndSetTokensOnHeader(String email, HttpServletResponse response) throws NoSuchMemberException {
        //email로 member를 찾는다.
        Member member = memberRepository.findNotDeletedByEmailWithJwt(email)
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()));

        //refresh token을 발급한다.
        String newRefreshToken = jwt.create()
                .withIssuer("myLittleStore")
                .withSubject("refreshToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .withClaim("email", member.getEmail())
                .sign(Algorithm.HMAC512(secret));

        Set<Jwt> memberJwts = member.getJwts();

        // 만료된 것 삭제
        removeExpired(memberJwts);

        // 새로 발급한 refreshToken을 DB에 저장
        Jwt newJwt = Jwt.builder()
            .member(member)
            .refreshToken(newRefreshToken)
            .build();

        jwtRepository.save(newJwt);

        //access token을 발급한다.
        String newAccessToken = jwt.create()
                .withIssuer("myLittleStore")
                .withSubject("accessToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .withClaim("email", email)
                .sign(Algorithm.HMAC512(secret));

        //access token, refresh token을 헤더에 실어서 보낸다.
        setAccessTokenOnHeader(response, newAccessToken);
        setRefreshTokenOnHeader(response, newRefreshToken);
    }


    @Transactional
    public void reissueTokensAndSetTokensOnHeader(String email, String refreshToken, HttpServletResponse response) throws NoSuchMemberException {
        //email로 member를 찾는다.
        Member member = memberRepository.findNotDeletedByEmailWithJwt(email)
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()));

        Set<Jwt> memberJwts = member.getJwts();

        // 만료된 것 삭제
        removeExpired(memberJwts);

        // 원래 있던 refreshToken을 찾아 삭제
        memberJwts.stream()
            .filter(jwt -> jwt.getRefreshToken().equals(refreshToken))
            .findFirst()
            .ifPresent(jwt -> {
                jwt.removeRefreshToken();
                jwtRepository.delete(jwt);
            });

        //refresh token을 발급한다.
        String newRefreshToken = jwt.create()
                .withIssuer("myLittleStore")
                .withSubject("refreshToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .withClaim("email", member.getEmail())
                .sign(Algorithm.HMAC512(secret));

        // 새로 만든 refreshToken을 DB에 저장
        Jwt newJwt = Jwt.builder()
                .member(member)
                .refreshToken(newRefreshToken)
                .build();

        jwtRepository.save(newJwt);

        //access token을 발급한다.
        String newAccessToken = jwt.create()
                .withIssuer("myLittleStore")
                .withSubject("accessToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .withClaim("email", email)
                .sign(Algorithm.HMAC512(secret));

        //access token, refresh token을 헤더에 실어서 보낸다.
        setAccessTokenOnHeader(response, newAccessToken);
        setRefreshTokenOnHeader(response, newRefreshToken);
    }

    /**
     * 헤더에서 AccessToken 추출
     * 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서
     * 헤더를 가져온 후 "Bearer"를 삭제(""로 replace)
     */
    public String extractAccessToken(HttpServletRequest request) {
        return request.getHeader(accessTokenHeader).replace(BEARER, "");
    }

    /**
     * 헤더에서 RefreshToken 추출
     * 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서
     * 헤더를 가져온 후 "Bearer"를 삭제(""로 replace)
     */
    public String extractRefreshToken(HttpServletRequest request) {
        return request.getHeader(refreshTokenHeader).replace(BEARER, "");
    }

    public Optional<String> validateAndExtractEmailFromToken(String token) {
        try {
            // 토큰 유효성 검사하는 데에 사용할 알고리즘이 있는 JWT verifier builder 반환
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secret))
                    .build() // 반환된 빌더로 JWT verifier 생성
                    .verify(token) // accessToken을 검증하고 유효하지 않다면 예외 발생
                    .getClaim("email") // claim(Email) 가져오기
                    .asString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Transactional
    public boolean isRefreshTokenExists(HttpServletResponse response, String refreshToken) {
        Jwt jwt = jwtRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new NoSuchJwtException(JwtErrorMessage.NO_SUCH_REFRESH_TOKEN.getMessage()));

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

    private void removeExpired(Set<Jwt> memberJwts) {
        // 만료된거 삭제하는 로직?
        memberJwts.forEach(jwt -> {
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
        response.setHeader(accessTokenHeader, BEARER + token);
    }

    /**
     * refreshToken을 Header에 작성한다.
     */
    public void setRefreshTokenOnHeader(HttpServletResponse response, String token) {
        response.setHeader(refreshTokenHeader, BEARER + token);
    }
}
