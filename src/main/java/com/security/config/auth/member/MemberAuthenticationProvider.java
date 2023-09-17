package com.security.config.auth.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.security.config.auth.PrincipalUserDetails;
import com.security.domain.member.Member;
import com.security.enumstorage.errormessage.auth.EmailErrorMessage;
import com.security.enumstorage.errormessage.auth.LogInErrorMessage;
import com.security.enumstorage.errormessage.auth.PasswordErrorMessage;
import com.security.enumstorage.status.MemberStatus;
import com.security.exception.auth.EmailException;
import com.security.exception.auth.PasswordException;
import com.security.repository.member.MemberRepository;
import com.security.util.Validator;

@Component
@RequiredArgsConstructor
public class MemberAuthenticationProvider implements AuthenticationProvider {
    private final PrincipalUserDetailsService principalUserDetailsService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    /**
     * 사용자가 입력한 email과 password를 검증하는 메소드
     * @param authentication the authentication request object.
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException, EmailException, PasswordException, BadCredentialsException {
        //email 검증
        String email = (String) authentication.getPrincipal();
        if (!validator.isValidEmail(email)) {
            throw new AuthenticationException(EmailErrorMessage.NOT_VALID_EMAIL.getMessage()) {
                @Override
                public String getMessage() {
                    return super.getMessage();
                }
            };
        }

        //password 검증
        String password = (String) authentication.getCredentials();
        if (password.isBlank()) {
            throw new AuthenticationException(PasswordErrorMessage.PASSWORD_IS_EMPTY.getMessage()) {
                @Override
                public String getMessage() {
                    return super.getMessage();
                }
            };
        }

        //email로 회원정보 조회
        UserDetails user = principalUserDetailsService.loadUserByUsername(email);

        PrincipalUserDetails principalUserDetails = (PrincipalUserDetails) user;
        Member member = principalUserDetails.getMember();

        //잠겨있는지 확인
        if (member.getStatus() == MemberStatus.LOCKED) {
            throw new BadCredentialsException(LogInErrorMessage.LOG_IN_ATTEMPT_EXCEEDED.getMessage());
        }

        //비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException(PasswordErrorMessage.PASSWORD_DOES_NOT_MATCH.getMessage());
        }

        //로그인 성공했으므로, 로그인 시도 횟수 초기화
        member.resetLogInAttempt();
        memberRepository.save(member);

        return new UsernamePasswordAuthenticationToken(email, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        boolean assignableFrom = UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
        return assignableFrom;
    }
}
