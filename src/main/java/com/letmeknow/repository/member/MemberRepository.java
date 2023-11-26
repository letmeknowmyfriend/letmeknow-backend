package com.letmeknow.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;
import com.letmeknow.entity.member.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryQueryDsl {
    Optional<Member> findNotDeletedById(long id);
    Optional<Member> findNotDeletedByEmail(String email);
    Optional<Member> findNotDeletedByEmailWithSubscription(String email);
    Optional<Member> findNotDeletedByEmailWithDeviceToken(String email);
    Optional<Member> findNotDeletedByEmailWithSubscriptionAndDeviceToken(String email);
    Optional<Member> findNotDeletedByEmailWithRefreshTokenAndSubscriptionAndDeviceToken(String email);
    Optional<Member> findNotDeletedByPasswordVerificationCode(String passwordVerificationCode);
    Optional<Long> findIdByEmail(String email);
    Optional<Long> findNotDeletedIdByEmail(String email);
    Optional<Member> findNotDeletedByEmailAndDeviceTokenAndSubscription(String email);
}
