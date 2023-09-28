package com.letmeknow.service;

import com.letmeknow.service.store.StoreService;
import org.junit.jupiter.api.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import com.letmeknow.domain.member.Member;
import com.letmeknow.dto.member.MemberUpdateDto;
import com.letmeknow.dto.store.StoreCreationDto;
import com.letmeknow.enumstorage.errormessage.member.MemberErrorMessage;
import com.letmeknow.dto.member.MemberCreationDto;
import com.letmeknow.dto.member.MemberFindDto;
import com.letmeknow.dto.member.MemberPasswordUpdateDto;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.service.member.MemberService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MemberServiceTest {
    @Autowired
    private MemberService memberService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager em;

    private Long memberTestId;

    private Long storeTestId;

    @BeforeEach
    void setUp() {
        Long newMemberId = memberService.joinMember(MemberCreationDto.builder()
                .name("memberTest")
                .email("memberTest@gmail.com")
                .password("password")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        Long newStoreId = storeService.createStore(StoreCreationDto.builder()
                .name("storeTest")
                .memberId(newMemberId)
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        memberTestId = newMemberId;
        storeTestId = newStoreId;
    }

    @Test
    @DisplayName("memberId로 MemberFindDto를 조회한다.")
    void findMemberFindDtoById() {
        //when
        MemberFindDto memberFindDtoById = memberService.findMemberFindDtoById(memberTestId);

        //then
        assertThat(memberFindDtoById.getName()).isEqualTo("memberTest");
    }

    @Test
    @DisplayName("memberId로 회원을 찾지 못하면 예외 발생")
    void findMemberFindDtoByIdException() {
        //then
        assertThatThrownBy(() -> {
            memberService.findMemberFindDtoById(1234L);
        }).isInstanceOf(NoSuchMemberException.class);
    }

    @Test
    @DisplayName("email로 MemberFindDto를 조회한다.")
    void findMemberFindDtoByEmail() {
        //when
        MemberFindDto memberFindDtoByEmail = memberService.findMemberFindDtoByEmail("memberTest@gmail.com");

        //then
        assertThat(memberFindDtoByEmail.getName()).isEqualTo("memberTest");
    }

    @Test
    @DisplayName("email로 회원을 찾지 못하면 예외 발생")
    void findMemberFindDtoByEmailException() {
        //then
        assertThatThrownBy(() -> {
            memberService.findMemberFindDtoByEmail("asdfg@gmail.com");
        }).isInstanceOf(NoSuchMemberException.class)
        .hasMessageContaining(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_EMAIL.getMessage());
    }

    @Test
    @DisplayName("회원 모두 조회")
    public void findAllMemberFindDto(){
        //given
        memberService.joinMember(MemberCreationDto.builder()
                .name("memberTestA")
                .email("memberTestA@gmail.com")
                .password("password")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        memberService.joinMember(MemberCreationDto.builder()
                .name("memberTestB")
                .email("memberTestB@gmail.com")
                .password("password")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //when
        List<MemberFindDto> findAllMember = memberService.findAllMemberFindDto();

        //then
        assertThat(findAllMember.size()).isEqualTo(3);
    }
    @Test
    @DisplayName("회원 가입")
    void joinMember() {
        //when
        //회원 가입
        Long createdMemberId = memberService.joinMember(MemberCreationDto.builder()
                .name("memberTestB")
                .email("memberTestB@gmail.com")
                .password("password")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //then
        //회원 가입 잘 됐는지 확인
        MemberFindDto findMemberFindDtoById = memberService.findMemberFindDtoById(createdMemberId);

        assertThat(findMemberFindDtoById.getName()).isEqualTo("memberTestB");
    }

    @Test
    @DisplayName("회원 가입 시 이미 있는 이메일이면 예외 발생")
    void joinMemberException() {
        //then
        assertThatThrownBy(() ->
            memberService.joinMember(MemberCreationDto.builder()
                    .name("memberTest")
                    .email("memberTest@gmail.com")
                    .password("password")
                    .city("city")
                    .street("street")
                    .zipcode("zipcode")
                    .build())
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("회원 정보 수정")
    void updateMember() {
        //given
        memberService.updateMember(MemberUpdateDto.builder()
                .id(memberTestId)
                .name("Cha Cha")
                .city("newCity")
                .street("newStreet")
                .zipcode("newZipcode")
                .build());

        //when
        MemberFindDto findMemberFindDtoById = memberService.findMemberFindDtoById(memberTestId);

        //then
        assertThat(findMemberFindDtoById.getName()).isEqualTo("Cha Cha");
        assertThat(findMemberFindDtoById.getCity()).isEqualTo("newCity");
        assertThat(findMemberFindDtoById.getStreet()).isEqualTo("newStreet");
        assertThat(findMemberFindDtoById.getZipcode()).isEqualTo("newZipcode");
    }

    @Test
    @DisplayName("회원 비밀번호 수정")
    void updateMemberPassword() {
        //given
        //회원 비밀번호 수정
        memberService.updateMemberPassword(MemberPasswordUpdateDto.builder()
                .id(memberTestId)
                .password("password")
                .newPassword("Cha Cha")
                .build());

        //when
        //회원 조회
        Member member = memberRepository.findNotDeletedById(memberTestId)
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()));

        //then
        assertThat(member.getPassword()).isEqualTo("Cha Cha");
    }
}
