package com.letmeknow.repository.member.temporarymember;

import com.letmeknow.entity.member.TemporaryMember;

import java.util.Optional;

public interface TemporaryMemberRepositoryQueryDsl {
    Optional<Long> findIdByEmail(String email);
    Optional<TemporaryMember> findByEmail(String email);
    Optional<String> findVerificationCodeByEmail(String email);
    Optional<TemporaryMember> findByVerificationCode(String verificationCode);
}
