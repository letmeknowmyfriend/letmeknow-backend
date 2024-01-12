package com.letmeknow.auth.provider;

import static com.letmeknow.auth.messages.MemberMessages.EMAIL;
import static com.letmeknow.auth.messages.MemberMessages.SEND;
import static com.letmeknow.auth.messages.MemberMessages.SIGN_IN;
import static com.letmeknow.message.messages.Messages.CHECK;
import static com.letmeknow.message.messages.Messages.FAIL;
import static com.letmeknow.message.messages.Messages.PLEASE;

import com.letmeknow.auth.service.PrincipalUserDetailsService;
import com.letmeknow.auth.userdetail.PrincipalUserDetails;
import com.letmeknow.entity.member.Member;
import com.letmeknow.enumstorage.errormessage.auth.LogInErrorMessage;
import com.letmeknow.enumstorage.status.MemberStatus;
import com.letmeknow.exception.member.MemberStateException;
import com.letmeknow.service.member.MemberService;
import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원을 찾고, 인증을 수행하는 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberAuthenticationProvider implements AuthenticationProvider {
    private final PrincipalUserDetailsService principalUserDetailsService;
    private final MemberService memberService;

    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자가 입력한 email과 password를 검증하는 메소드
     *
     * @param authentication the authentication request object.
     * @return
     * @throws AuthenticationException
     */
    @Override
    @Transactional(noRollbackFor = AuthenticationException.class)
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 값 검증 완료된 email, password
        String email = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        // email로 회원정보 조회
        // Transaction 있음
        UserDetails user = principalUserDetailsService.loadUserByUsername(email);

        PrincipalUserDetails principalUserDetails = (PrincipalUserDetails) user;
        Member member = principalUserDetails.getMember();

        // 잠겨있는지 확인
        if (member.getStatus() == MemberStatus.LOCKED) {
            // 이메일 보내주기
            try {
                memberService.sendChangePasswordVerificationEmail(email);
            }
            catch (UnsupportedEncodingException | MessagingException e) {
                log.info(EMAIL.getMessage() + SEND.getMessage() + FAIL.getMessage());
            }

          throw new MemberStateException(LogInErrorMessage.LOG_IN_ATTEMPT_EXCEEDED.getMessage() + "\n" + EMAIL.getMessage() + CHECK.getMessage() + PLEASE.getMessage());
        }

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // 로그인 시도 횟수 증가
            member.countUpLogInAttempt();

            throw new BadCredentialsException(new StringBuffer().append(SIGN_IN.getMessage()).append(FAIL.getMessage()).toString());
        }

        // 로그인 성공했으므로, 로그인 시도 횟수 초기화
        member.resetLogInAttempt();

        return new UsernamePasswordAuthenticationToken(email, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        boolean assignableFrom = UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
        return assignableFrom;
    }
}
