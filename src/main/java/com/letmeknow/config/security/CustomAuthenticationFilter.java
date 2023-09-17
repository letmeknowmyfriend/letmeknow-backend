//package com.letmeknow.config.security;
//
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
//import org.springframework.security.web.util.matcher.RequestMatcher;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//public class CustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
//
//    public CustomAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
//        super(requiresAuthenticationRequestMatcher);
//    }
//
//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest request,
//                                                HttpServletResponse response) {
//        String email = request.getParameter("email");
//        String password = request.getParameter("password");
//
//        return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(email, password));
//    }
//}
