//package com.letmeknow.interceptor.auth;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//import com.letmeknow.auth.userdetail.PrincipalUserDetails;
//import com.letmeknow.enumstorage.errormessage.member.MemberErrorMessage;
//import com.letmeknow.enumstorage.role.MemberRole;
//import com.letmeknow.exception.member.NoSuchMemberException;
//import com.letmeknow.repository.member.MemberRepository;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.Collection;
//
//@Component
//@RequiredArgsConstructor
//public class MemberAuthorizationInterceptor implements HandlerInterceptor {
//    private final MemberRepository memberRepository;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (!authentication.isAuthenticated()) {
//            return false;
//        }
//
//        //admin이면 프리 패스
//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//        for (GrantedAuthority authority : authorities) {
//            if (authority.getAuthority().equals(MemberRole.ADMIN.toString())) {
//                return true;
//            }
//        }
//
//        //아니면, username으로 찾은 memberId와 URL의 memberId가 일치하는지 확인
//        PrincipalUserDetails principal = (PrincipalUserDetails) authentication.getPrincipal();
//        String email = principal.getUsername();
//
//        long memberId = memberRepository.findNotDeletedIdByEmail(email)
//                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()));
//
//        String requestMemberId = request.getRequestURI().split("/")[2];
//
//        if (memberId.equals(Long.parseLong(requestMemberId))) {
//            return true;
//        }
//
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//
//        return false;
//    }
//}
