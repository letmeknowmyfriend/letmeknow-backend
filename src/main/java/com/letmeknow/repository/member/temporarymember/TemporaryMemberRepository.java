package com.letmeknow.repository.member.temporarymember;

import org.springframework.data.jpa.repository.JpaRepository;
import com.letmeknow.entity.member.TemporaryMember;

import java.util.Optional;

public interface TemporaryMemberRepository extends JpaRepository<TemporaryMember, Long>, TemporaryMemberRepositoryQueryDsl {
    Optional<TemporaryMember> findByEmail(String email);
    Optional<String> findVerificationCodeByEmail(String email);
    Optional<TemporaryMember> findByVerificationCode(String verificationCode);
}
