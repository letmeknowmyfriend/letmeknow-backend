package com.letmeknow.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import com.letmeknow.dto.member.MemberCreationDto;
import com.letmeknow.dto.member.MemberFindDto;
import com.letmeknow.dto.store.StoreCreationDto;
import com.letmeknow.service.member.MemberService;
import com.letmeknow.service.store.StoreService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberService memberService;
    @Autowired
    private StoreService storeService;

    @Test
    void memberList() throws Exception {
        //given
        mockMvc.perform(post("/members/new")
                        .param("name", "memberTest1")
                        .param("email", "memberTest1@email.com")
                        .param("password", "password")
                        .param("city", "city")
                        .param("street", "street")
                        .param("zipcode", "zipcode"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/members/1"));

        mockMvc.perform(post("/members/new")
                        .param("name", "memberTest2")
                        .param("email", "memberTest2@email.com")
                        .param("password", "password")
                        .param("city", "city")
                        .param("street", "street")
                        .param("zipcode", "zipcode"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/members/2"));

        //then
        mockMvc.perform(get("/members"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/memberList"))
                .andExpect(model().attributeExists("memberFindDtos"));

        List<MemberFindDto> findAllMemberFindDto = memberService.findAllMemberFindDto();
        assertThat(findAllMemberFindDto.size()).isEqualTo(2);
    }

    @Test
    void memberInfo() throws Exception {
        //given
        Long savedMemberId = memberService.joinMember(MemberCreationDto.builder()
                .name("memberTest")
                .email("memberTest@email.com")
                .password("password")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        Long savedStoreId = storeService.createStore(StoreCreationDto.builder()
                .memberId(savedMemberId)
                .name("storeTest")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //then
        mockMvc.perform(get("/members/{memberId}", savedMemberId))
                .andExpect(status().isOk())
                .andExpect(view().name("members/memberInfo"))
                .andExpect(model().attributeExists("memberFindDto"))
                .andExpect(model().attributeExists("storeDtos"));
    }

    @Test
    void createMemberForm() throws Exception {
        mockMvc.perform(get("/members/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/memberCreationForm"))
                .andExpect(model().attributeExists("memberCreationForm"));
    }

    @Test
    void createMember() throws Exception {
        mockMvc.perform(post("/members/new")
                        .param("name", "memberTest")
                        .param("email", "memberTest@email.com")
                        .param("password", "password")
                        .param("city", "city")
                        .param("street", "street")
                        .param("zipcode", "zipcode"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/members/1"));

        MemberFindDto findMemberByMemberEmail = memberService.findMemberFindDtoByEmail("memberTest@email.com");
        assertThat(findMemberByMemberEmail.getName()).isEqualTo("memberTest");
        assertThat(findMemberByMemberEmail.getEmail()).isEqualTo("memberTest@email.com");
        assertThat(findMemberByMemberEmail.getCity()).isEqualTo("city");
        assertThat(findMemberByMemberEmail.getStreet()).isEqualTo("street");
        assertThat(findMemberByMemberEmail.getZipcode()).isEqualTo("zipcode");
    }

    @Test
    void createMemberError() throws Exception {
        //then
        mockMvc.perform(post("/members/new")
                        .param("name", "memberTest")
                        .param("email", "memberTest")
                        .param("password", "password")
                        .param("city", "city")
                        .param("street", "street")
                        .param("zipcode", "zipcode"))
                .andExpect(view().name("members/memberCreationForm"));
    }

    @Test
    void updateMemberForm() throws Exception {
        //given
        mockMvc.perform(post("/members/new")
                        .param("name", "memberTestName")
                        .param("email", "memberTest@email.com")
                        .param("password", "password")
                        .param("city", "city")
                        .param("street", "street")
                        .param("zipcode", "zipcode"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/members/1"));

        //then
        mockMvc.perform(get("/members/{memberId}/update", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("members/memberUpdateForm"))
                .andExpect(model().attributeExists("memberUpdateForm"));
    }

    @Test
    void updateMember() throws Exception {
        //given
        mockMvc.perform(post("/members/new")
                .param("name", "memberTestName")
                .param("email", "memberTest@email.com")
                .param("password", "password")
                .param("city", "city")
                .param("street", "street")
                .param("zipcode", "zipcode"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/members/1"));

        mockMvc.perform(post("/members/1/update")
                .param("name", "updateMemberTestName")
                .param("city", "updateCity")
                .param("street", "updateStreet")
                .param("zipcode", "updateZipcode"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/members/1"));

        MemberFindDto findMemberByMemberEmail = memberService.findMemberFindDtoByEmail("memberTest@email.com");
        assertThat(findMemberByMemberEmail.getName()).isEqualTo("updateMemberTestName");
        assertThat(findMemberByMemberEmail.getCity()).isEqualTo("updateCity");
        assertThat(findMemberByMemberEmail.getStreet()).isEqualTo("updateStreet");
        assertThat(findMemberByMemberEmail.getZipcode()).isEqualTo("updateZipcode");
    }

    @Test
    void updateMemberError() throws Exception {
        //given
        mockMvc.perform(post("/members/new")
                        .param("name", "memberTestName")
                        .param("email", "memberTest@email.com")
                        .param("password", "password")
                        .param("city", "city")
                        .param("street", "street")
                        .param("zipcode", "zipcode"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/members/1"));

        //then
        mockMvc.perform(post("/members/{memberId}/update", 1L)
                        .param("name", "")
                        .param("city", "city")
                        .param("street", "street")
                        .param("zipcode", "zipcode"))
                .andExpect(view().name("members/memberUpdateForm"));
    }
}