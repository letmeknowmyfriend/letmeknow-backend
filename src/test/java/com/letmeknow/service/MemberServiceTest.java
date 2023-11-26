package com.letmeknow.service;

import com.letmeknow.entity.member.Member;
import com.letmeknow.dto.member.MemberPasswordUpdateDto;
import com.letmeknow.exception.member.*;
import com.letmeknow.form.MemberAddressUpdateForm;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.service.member.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional // 테스트 메소드 실행 후 롤백
@TestMethodOrder(OrderAnnotation.class) // 테스트 메소드 순서 지정
//@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MemberServiceTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Order(100)
    @DisplayName("회원 주소 수정 - 성공")
    void updateMemberAddress() throws NoSuchMemberException {
        // given
        memberService.updateMemberAddress(MemberAddressUpdateForm.builder()
            .city("newCity")
            .street("newStreet")
            .zipcode("newZipcode")
            .build(), "member1@gmail.com");

        // when
        Member member = memberRepository.findNotDeletedByEmail("member1@gmail.com").get();

        // then
        assertThat(member.getAddress().getCity()).isEqualTo("newCity");
    }

    @Test
    @Order(110)
    @DisplayName("회원 주소 수정 - MemberAddressUpdateForm 유효성 검증")
    void updateMemberAddressFormValidation() {
            // given
        assertThatThrownBy(() -> memberService.updateMemberAddress(MemberAddressUpdateForm.builder()
            .city(null)
            .street("newStreet")
            .zipcode("newZipcode")
            .build(), "member3@gmail.com"))
        .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @Order(120)
    @DisplayName("회원 주소 수정 - 임시 회원 주소 수정")
    void updateTemporaryMemberAddress() {
        // given
        assertThatThrownBy(() -> memberService.updateMemberAddress(MemberAddressUpdateForm.builder()
            .city("newCity")
            .street("newStreet")
            .zipcode("newZipcode")
            .build(), "temporaryMember1@gmail.com"))
            .isInstanceOf(NoSuchMemberException.class);
    }

    @Test
    @Order(130)
    @DisplayName("회원 주소 수정 - 없는 회원 주소 수정")
    void updateNotExistingMemberAddress() {
        // given
        assertThatThrownBy(() -> memberService.updateMemberAddress(MemberAddressUpdateForm.builder()
            .city("newCity")
            .street("newStreet")
            .zipcode("newZipcode")
            .build(), "cha3088@gmail.com"))
            .isInstanceOf(NoSuchMemberException.class);
    }

    @Test
    @Order(140)
    @DisplayName("회원 주소 수정 - 삭제된 회원 주소 수정")
    void updateDeletedMemberAddress() {
        // given
        assertThatThrownBy(() -> memberService.updateMemberAddress(MemberAddressUpdateForm.builder()
            .city("newCity")
            .street("newStreet")
            .zipcode("newZipcode")
            .build(), "member5@gmail.com"))
            .isInstanceOf(NoSuchMemberException.class);
    }

    @Test
    @Order(150)
    @DisplayName("회원 주소 수정 - 테스트 메소드 별로 트랜잭션 롤백이 작동하는지 확인")
    void updateMemberAddressTransactionRollBackTest() throws NoSuchMemberException {
        // when
        Member member = memberRepository.findNotDeletedByEmail("member1@gmail.com").get();

        // then
        assertThat(member.getAddress().getCity()).isEqualTo("city");
    }

    @Test
    @Order(160)
    @DisplayName("회원 비밀번호 수정 - 성공")
    void updateMemberPassword() throws InvalidPasswordException, NewPasswordNotMatchException, NoSuchMemberException, PasswordIncorrectException, MemberSignUpValidationException {
        // given
        memberService.updateMemberPassword(MemberPasswordUpdateDto.builder()
            .password("password")
            .newPassword("newPassword1029!")
            .newPasswordAgain("newPassword1029!")
            .build()
        , "member1@gmail.com");

        // when
        Member member = memberRepository.findNotDeletedByEmail("member1@gmail.com").get();

        // then
        assertThat(member.getPassword()).isEqualTo("newPassword1029!");
    }

    @Test
    @Order(200)
    @DisplayName("회원 비밀번호 수정 - 새 비밀번호끼리 일치하지 않을 때")
    void updateMemberPasswordNotMatch() {
        // given
        assertThatThrownBy(() -> memberService.updateMemberPassword(MemberPasswordUpdateDto.builder()
            .password("password")
            .newPassword("newPassword1029!")
            .newPasswordAgain("newPassword1029!@")
            .build()
            ,"member1@gmail.com"))
        .isInstanceOf(NewPasswordNotMatchException.class);
    }

    @Test
    @Order(210)
    @DisplayName("회원 비밀번호 수정 - 비밀번호 규칙 위반")
    void updateMemberPasswordInvalid() {
        // given
        assertThatThrownBy(() -> memberService.updateMemberPassword(MemberPasswordUpdateDto.builder()
            .password("password")
            .newPassword("newPassword")
            .newPasswordAgain("newPassword")
            .build()
        , "member1@gmail.com"))
        .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @Order(220)
    @DisplayName("회원 비밀번호 수정 - 기존 비밀번호가 틀렸을 때")
    void updateMemberPasswordIncorrect() {
        // given
        assertThatThrownBy(() -> memberService.updateMemberPassword(MemberPasswordUpdateDto.builder()
            .password("password123")
            .newPassword("newPassword1029!")
            .newPasswordAgain("newPassword1029!")
            .build()
        , "member1@gmail.com"))
        .isInstanceOf(PasswordIncorrectException.class);
    }

    @Test
    @Order(230)
    @DisplayName("회원 비밀번호 수정 - 임시 회원 비밀번호 수정")
    void updateTemporaryMemberPassword() {
        // given
        assertThatThrownBy(() -> memberService.updateMemberPassword(MemberPasswordUpdateDto.builder()
            .password("password")
            .newPassword("newPassword1029!")
            .newPasswordAgain("newPassword1029!")
            .build()
        , "temporaryMember1@gmail.com"))
        .isInstanceOf(NoSuchMemberException.class);
    }

    @Test
    @Order(240)
    @DisplayName("회원 비밀번호 수정 - 없는 회원 비밀번호 수정")
    void updateNotExistingMemberPassword() {
        // given
        assertThatThrownBy(() -> memberService.updateMemberPassword(MemberPasswordUpdateDto.builder()
            .password("password")
            .newPassword("newPassword1029!")
            .newPasswordAgain("newPassword1029!")
            .build()
        , "abc123@gmail.com"))
        .isInstanceOf(NoSuchMemberException.class);
    }

    @Test
    @Order(250)
    @DisplayName("회원 비밀번호 수정 - 삭제된 회원 비밀번호 수정")
    void updateDeletedMemberPassword() {
        // given
        assertThatThrownBy(() -> memberService.updateMemberPassword(MemberPasswordUpdateDto.builder()
            .password("password")
            .newPassword("newPassword1029!")
            .newPasswordAgain("newPassword1029!")
            .build()
        , "member5@gmail.com"))
        .isInstanceOf(NoSuchMemberException.class);
    }
}
