package com.letmeknow.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;
import com.letmeknow.domain.member.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryQueryDsl {
    Optional<Member> findNotDeletedById(Long id);
    Optional<Member> findNotDeletedByEmail(String email);
    Optional<Member> findNotDeletedByEmailWithJwt(String email);
    Optional<Member> findNotDeletedByPasswordVerificationCode(String passwordVerificationCode);

    /**
     * 이메일로 회원의 id를 찾는다.
     * 회원가입할 때만 사용
     * @param email
     * @return
     */
    Optional<Long> findIdByEmail(String email);
    Optional<Long> findNotDeletedIdByEmail(String email);

    Optional<Member> findNotDeletedByEmailAndDeviceTokenAndSubscription(String email);
}
