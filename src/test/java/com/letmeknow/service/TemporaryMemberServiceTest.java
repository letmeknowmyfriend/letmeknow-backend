package com.letmeknow.service;

import com.letmeknow.entity.member.TemporaryMember;
import com.letmeknow.dto.member.MemberCreationDto;
import com.letmeknow.exception.member.MemberSignUpValidationException;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.service.member.TemporaryMemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.validation.ConstraintViolationException;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional // 테스트 메소드 실행 후 롤백
@TestMethodOrder(OrderAnnotation.class) // 테스트 메소드 순서 지정
//@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TemporaryMemberServiceTest {
    @Autowired
    private TemporaryMemberService temporaryMemberService;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Order(100)
    @DisplayName("임시 회원 가입 - 성공")
    void joinTemporaryMember() throws MemberSignUpValidationException, MessagingException, UnsupportedEncodingException {
        // given
        long temporaryMemberId = temporaryMemberService.joinTemporaryMember(MemberCreationDto.builder()
                .name("temporaryMember2")
                .email("temporaryMember2@gmail.com")
                .password("password")
                .city("city")
                .street("street")
                .zipcode("zipcode")
            .build());

        // when
        TemporaryMember newTemporaryMember = temporaryMemberService.findTemporaryMemberByEmail("temporaryMember2@gmail.com");

        // then
        assertThat(newTemporaryMember.getId()).isEqualTo(temporaryMemberId);
    }

    @Test
    @Order(110)
    @DisplayName("임시 회원 가입 - MemberCreationDto 유효성 검증")
    void joinTemporaryMemberFormValidation() {
        // given
        // password 없을 때
        assertThatThrownBy(() -> temporaryMemberService.joinTemporaryMember(MemberCreationDto.builder()
            .name("temporaryMember2")
            .email("temporaryMember2@gmail.com")
            .password("")
            .city("city")
            .street("street")
            .zipcode("zipcode")
            .build()))
        .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @Order(120)
    @DisplayName("임시 회원 가입 - 중복된 회원 이메일")
    void joinTemporaryMemberWithDuplicatedEmail() {
        // given
        // 중복된 이메일
        assertThatThrownBy(() -> temporaryMemberService.joinTemporaryMember(MemberCreationDto.builder()
            .name("temporaryMember2")
            .email("member2@gmail.com")
            .password("password")
            .city("city")
            .street("street")
            .zipcode("zipcode")
            .build()))
            .isInstanceOf(MemberSignUpValidationException.class);
    }

    @Test
    @Order(130)
    @DisplayName("임시 회원 가입 - 중복된 임시 회원 이메일")
    void joinTemporaryMemberWithDuplicatedTemporaryMemberEmail() {
        // given
        // 중복된 이메일
        assertThatThrownBy(() -> temporaryMemberService.joinTemporaryMember(MemberCreationDto.builder()
            .name("temporaryMember1")
            .email("temporaryMember1@gmail.com")
            .password("password")
            .city("city")
            .street("street")
            .zipcode("zipcode")
            .build()))
            .isInstanceOf(MemberSignUpValidationException.class);
    }

    @Test
    @Order(140)
    @DisplayName("임시 회원 가입 - 지워진 이메일")
    void joinTemporaryMemberWithDeletedEmail() {
        // given
        // 지워진 이메일
        assertThatThrownBy(() -> temporaryMemberService.joinTemporaryMember(MemberCreationDto.builder()
            .name("temporaryMember1")
            .email("member5@gmail.com")
            .password("password")
            .city("city")
            .street("street")
            .zipcode("zipcode")
            .build()))
            .isInstanceOf(MemberSignUpValidationException.class);
    }
}
