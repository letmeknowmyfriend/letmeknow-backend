package com.security.filter.auth.jwt;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.security.config.auth.member.MemberLogInFailureHandler;
import com.security.config.auth.member.MemberLogInSuccessHandler;
import com.security.enumstorage.errormessage.auth.EmailErrorMessage;
import com.security.enumstorage.errormessage.auth.PasswordErrorMessage;
import com.security.repository.member.temporarymember.TemporaryMemberRepository;
import com.security.service.auth.jwt.JwtService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MemberAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final TemporaryMemberRepository temporaryMemberRepository;
    private final JwtService jwtService;

    public MemberAuthenticationFilter(AuthenticationManager authenticationManager, TemporaryMemberRepository temporaryMemberRepository, JwtService jwtService, MemberLogInSuccessHandler memberLogInSuccessHandler, MemberLogInFailureHandler memberLogInFailureHandler) {
        super(authenticationManager);
        this.temporaryMemberRepository = temporaryMemberRepository;
        this.jwtService = jwtService;
        setFilterProcessesUrl("/auth/login/member");
        setAuthenticationSuccessHandler(memberLogInSuccessHandler);
        setAuthenticationFailureHandler(memberLogInFailureHandler);
    }

    //로그인 요청이 들어오면 로그인 시도하는 메소드
    //1. username, password 받아서
    //2. 정상인지 로그인 시도 -> AuthenticationManager로 로그인 시도
    //3. PrincipalDetailsService 호출 -> loadUserByUsername() 함수 실행
    //4. PrincipalDetails를 세션에 담고 (권한 관리를 위해서)
    //5. JWT를 만들어서 응답해주면 됨
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //1. email, password 받아서
        Enumeration<String> params = request.getParameterNames();
        ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>();
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            concurrentHashMap.put(param, request.getParameter(param));
        }

        String email = Optional.ofNullable(concurrentHashMap.get("email"))
                .orElseThrow(() -> new AuthenticationException(EmailErrorMessage.EMAIL_IS_EMPTY.getMessage()) {});
        String password = Optional.ofNullable(concurrentHashMap.get("password"))
                .orElseThrow(() -> new AuthenticationException(PasswordErrorMessage.PASSWORD_IS_EMPTY.getMessage()) {});

        //authentication 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);

        //PrincipalUserDetailsService의 loadUserByUsername() 함수가 실행됨
        return super.getAuthenticationManager().authenticate(authentication);
    }

//    @Override
//    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
//        PrincipalUserDetails principal = (PrincipalUserDetails) authentication.getPrincipal();
//
//        String email = Optional.ofNullable(principal.getUsername())
//                .orElseThrow(() -> new EmailException(EmailErrorMessage.EMAIL_IS_EMPTY.getMessage()));
//
//        //AccessToken을 발급한다.
//        String accessToken = jwtService.createAccessToken(email);
//
//        //RefreshToken을 재발급한다.
//        String refreshToken = jwtService.createRefreshToken(email);
//
//        //AccessToken과 RefreshToken을 Header에 담아 보낸다.
//        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
//
//        //Member 로그인 성공 시, 메인 페이지로 이동
//        response.setStatus(HttpServletResponse.SC_OK);
//        response.sendRedirect("/");
//    }
//
//    @Override
//    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
//        PrincipalUserDetails principal = (PrincipalUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//        try
//        {
//            //TemporaryMember를 찾는다.
//            Long temporaryMemberId = temporaryMemberRepository.findIdByEmail(principal.getUsername())
//                    .orElseThrow(() -> new NoSuchTemporaryMemberException(TemporaryMemberErrorMessage.NO_SUCH_TEMPORARY_MEMBER_WITH_THAT_VERIFICATION_CODE.getMessage()));
//
//            //있다면, 이메일 인증 요구 페이지로 redirect
//            response.sendRedirect("/auth/member/notice/verification-email/" + temporaryMemberId);
//
//        }
//        catch (NoSuchTemporaryMemberException e)
//        {
//            //없다면, 로그인 페이지로 redirect
//            response.sendRedirect("/auth/login");
//        }
//    }
}