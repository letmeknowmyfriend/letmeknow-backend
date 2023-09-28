package com.letmeknow.filter.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.letmeknow.config.auth.PrincipalUserDetails;
import com.letmeknow.domain.member.Member;
import com.letmeknow.enumstorage.errormessage.member.MemberErrorMessage;
import com.letmeknow.enumstorage.errormessage.auth.jwt.JwtErrorMessage;
import com.letmeknow.exception.auth.jwt.NoSuchJwtException;
import com.letmeknow.exception.auth.jwt.NotValidJwtException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.service.auth.jwt.JwtService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * /auth/login 이외의 요청이 들어올 때, access token이 유효한지 검증하고 인증 처리/인증 실패/토큰 재발급 등을 수행
 */
@Component
@RequiredArgsConstructor
public class AuthenticationProcessFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    private static final String NO_CHECK_URL = "/auth"; // "/auth/login"으로 들어오는 요청은 Filter 작동 X

    /**
     * "/auth/login"으로 시작하는 URL 요청은 logIn 검증 및 authenticate X
     * 그 외의 URL 요청은 access token 검증 및 authenticate 수행
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith(NO_CHECK_URL) || request.getRequestURI().startsWith("/error") || request.getRequestURI().startsWith("/css") || request.getRequestURI().startsWith("/js") || request.getRequestURI().startsWith("/img") || request.getRequestURI().startsWith("/favicon.ico")) {
            filterChain.doFilter(request, response); // "/auth/login"으로 시작하는 URL 요청이 들어오면, 다음 필터 호출
            return; // return으로 이후 현재 필터 진행 막기 (안해주면 아래로 내려가서 계속 필터 진행시킴)
        }

        String accessToken = "";
        String refreshToken = "";
        Cookie[] requestCookies = request.getCookies();
        if (requestCookies != null) {
            for (Cookie cookie : requestCookies) {
                if (cookie.getName().equals("accessToken")) {
                    accessToken = cookie.getValue();
                } else if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        try {
            //access token이 있을 때
            if (!accessToken.isBlank()) {
                //access token이 유효하면
                try
                {
                    //access token에서 email 추출
                    String email = jwtService.extractEmailFromToken(accessToken)
                            .orElseThrow(() -> new NotValidJwtException(JwtErrorMessage.NOT_VALID_JWT.getMessage()));

                    //해당 email을 사용하는 유저 객체 반환
                    Member member = memberRepository.findNotDeletedByEmail(email)
                            .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()));

                    //access token으로 인증 처리
                    saveAuthentication(member);

                    filterChain.doFilter(request, response); //다음 필터 호출
                    return; //return으로 이후 현재 필터 진행 막기
                }

                //access token이 유효하지 않으면
                catch (JWTVerificationException e)
                {
                    //access token이 유효하지 않으면, 인증 실패
                    //모든 토큰 삭제
                    jwtService.deleteAllTokensFromClient(response);

                    //로그인 페이지로 redirect
                    response.sendRedirect("/auth/login");
                    return;
                }
            }

            //access token이 없을 때
            else if (accessToken.isBlank()) {
                //refresh token이 있으면
                if (!refreshToken.isBlank()) {
                    //refresh token을 검증
                    if (jwtService.isTokenValid(refreshToken) && jwtService.isRefreshTokenExists(response, refreshToken)) {
                        //refresh token이 유효하면

                        //토큰들 재발급
                        Member member = jwtService.reissueTokens(refreshToken, response);

                        //access token을 재발급 받았으므로, 인증 성공
                        saveAuthentication(member);

                        filterChain.doFilter(request, response); //다음 필터 호출

                        return; //RefreshToken이 유효한 경우에는 AccessToken을 재발급 하고 인증 처리는 하지 않도록 바로 return으로 필터 진행 막기
                    }

                    //refresh token인 유효하지 않거나 DB에 없거나 기한이 지났으면, 인증 실패
                    else {
                        //모든 토큰 삭제
                        jwtService.deleteAllTokensFromClient(response);

                        //로그인 페이지로 redirect
                        response.sendRedirect("/auth/login");
                        return;
                    }
                }

                //refresh token도 없으면, 인증이 없는 것
                else {
                    // 예외로 메인 페이지는 볼 수 있음
                    if (request.getRequestURI().equals("/")) {
                        filterChain.doFilter(request, response); //다음 필터 호출
                        return;
                    }

                    response.sendRedirect("/auth/login");
                    return;
                }
            }

        //해당 사용자가 없을 때
        } catch (NoSuchMemberException | NoSuchJwtException e) {
            //모든 토큰 삭제
            jwtService.deleteAllTokensFromClient(response);

            //로그인 페이지로 redirect
            response.sendRedirect("/auth/login");
            return;

        //access token이 유효하지 않을 때
        } catch (NotValidJwtException e) {
            //모든 토큰 삭제
            jwtService.deleteAllTokensFromClient(response);

            //로그인 페이지로 redirect
            response.sendRedirect("/auth/login");
            return;
        }
    }

    /**
     * [인증 허가 메소드]
     * 파라미터의 유저 : 우리가 만든 회원 객체 / 빌더의 유저 : UserDetails의 User 객체
     *
     * new UsernamePasswordAuthenticationToken()로 인증 객체인 Authentication 객체 생성
     * UsernamePasswordAuthenticationToken의 파라미터
     * 1. 위에서 만든 UserDetailsUser 객체 (유저 정보)
     * 2. credential(보통 비밀번호로, 인증 시에는 보통 null로 제거)
     * 3. Collection < ? extends GrantedAuthority>로,
     * UserDetails의 User 객체 안에 Set<GrantedAuthority> authorities이 있어서 getter로 호출한 후에,
     * new NullAuthoritiesMapper()로 GrantedAuthoritiesMapper 객체를 생성하고 mapAuthorities()에 담기
     *
     * SecurityContextHolder.getContext()로 SecurityContext를 꺼낸 후,
     * setAuthentication()을 이용하여 위에서 만든 Authentication 객체에 대한 인증 허가 처리
     */
    private void saveAuthentication(Member member) {
        UserDetails userDetails = new PrincipalUserDetails(member);

//        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, List.of(new SimpleGrantedAuthority(member.getRole().toString())));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
