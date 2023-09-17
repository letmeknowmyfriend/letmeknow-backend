//package com.letmeknow.filter.auth.jwt;
//
////Security는 Filter를 가지고 있는데, 그 필터 중 BasicAuthenticationFilter라는 것이 있다.
////권한이나 인증이 필요한 특정 주소를 요청했을 때 위 필터를 거친다.
//
//import com.auth0.jwt.interfaces.DecodedJWT;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.authorization.AuthorizationDecision;
//import org.springframework.security.authorization.AuthorizationEventPublisher;
//import org.springframework.security.authorization.AuthorizationManager;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.web.access.intercept.RequestMatcherDelegatingAuthorizationManager;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import com.letmeknow.config.auth.PrincipalUserDetails;
//import com.letmeknow.domain.member.Member;
//import com.letmeknow.enumstorage.errormessage.MemberErrorMessage;
//import com.letmeknow.enumstorage.errormessage.auth.jwt.JwtErrorMessage;
//import com.letmeknow.exception.auth.jwt.AccessTokenException;
//import com.letmeknow.repository.member.MemberRepository;
//import com.letmeknow.service.auth.jwt.JwtService;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.function.Supplier;
//
//@Component
//@RequiredArgsConstructor
//public class JwtAuthorizationFilter extends OncePerRequestFilter {
//    private final AuthorizationManager<HttpServletRequest> authorizationManager;
//    private final AuthorizationEventPublisher eventPublisher;
//
//    //권한이 필요한 주소 요청이 있을 때 해당 필터를 거친다.
//    //header
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
//        RequestMatcherDelegatingAuthorizationManager.builder()
//                .add()
//
//
//
//        Supplier<Authentication> authentication = (Supplier<Authentication>) SecurityContextHolder.getContext().getAuthentication();
//        AuthorizationDecision decision = authorizationManager.check(authentication, request);
//        eventPublisher.publishAuthorizationEvent(authentication, request, decision);
//
//        if (decision != null && !decision.isGranted()) {
//            throw new AccessDeniedException("Access denied");
//        }
//
//        chain.doFilter(request, response);
//
////        String authorizationHeader = request.getHeader(jwtService.getAccessTokenHeader());
////
////        //header가 있는지 확인
////        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
////            chain.doFilter(request, response);
////            return;
////        }
////
////        //쿠키가 있는지 확인
////
////        //access token을 검증해서 정상적인 사용자인지 확인
////        String requestAccessToken = jwtService.extractAccessToken(request)
////                .orElseThrow(() -> new AccessTokenException(JwtErrorMessage.ACCESS_TOKEN_NOT_FOUND.getMessage()));
////
////        DecodedJWT decodedAccessToken = jwtService.decodeJwt(response, requestAccessToken);
////
////        String id = decodedAccessToken.getClaim("id").asString();
////        String email = decodedAccessToken.getClaim("email").asString();
////
////        //JWT 서명을 통해 서명이 정상적으로 되면 Authentication 객체를 만들어준다.
////        if (!email.isBlank() | !id.isBlank()) {
////            //DB에 해당 회원이 존재하는지 확인
////            Member member = memberRepository.findActiveByEmail(email)
////                    .orElseThrow(() -> new UsernameNotFoundException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()));
////            //DB에 해당 회원이 존재하면
////            if (member.getId().equals(id)) {
////                //Authentication 객체를 만들어서
////                PrincipalUserDetails principalUserDetails = new PrincipalUserDetails(member);
////                Authentication authentication = new UsernamePasswordAuthenticationToken(principalUserDetails, member.getPassword(), principalUserDetails.getAuthorities());
////
////                //SecurityContextHolder에 저장
////                SecurityContextHolder.getContext().setAuthentication(authentication);
////
////                chain.doFilter(request, response);
////
////            //DB에 해당 회원이 존재하지 않으면
////            } else {
////
////            }
////        }
//    }
//}
