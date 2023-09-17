package com.letmeknow.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import com.letmeknow.dto.member.MemberCreationDto;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.service.member.MemberService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;

    @PersistenceContext
    private EntityManager em;

    private Long memberTestId;
    private String memberTestEmail;

    @BeforeAll
    void setUp() {
        Long newMemberId = memberService.joinMember(MemberCreationDto.builder()
                .name("memberTest")
                .email("memberTest@gmail.com")
                .password("password")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        memberTestId = newMemberId;
        memberTestEmail = "memberTest@gmail.com";
    }
}