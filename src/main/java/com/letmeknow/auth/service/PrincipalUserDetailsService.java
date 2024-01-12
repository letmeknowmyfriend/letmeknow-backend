package com.letmeknow.auth.service;

import com.letmeknow.auth.userdetail.PrincipalUserDetails;
import com.letmeknow.entity.member.Member;
import com.letmeknow.enumstorage.errormessage.member.MemberErrorMessage;
import com.letmeknow.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// Transactional 붙이지 마
@RequiredArgsConstructor
public class PrincipalUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    //이런 요청이 들어왔는데, 얘 혹시 회원이야?
    @Transactional(noRollbackFor = UsernameNotFoundException.class) // UserNameNotFoundException이 발생할 때, rollback하지 않는다.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Member를 찾는다.
        Member member = memberRepository.findNotDeletedByEmail(email)
                // 없으면, UsernameNotFoundException 발생
                .orElseThrow(() -> new UsernameNotFoundException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_EMAIL.getMessage()));

        // 있으면, PrincipalUserDetails 생성
        return new PrincipalUserDetails(member);
    }
}
