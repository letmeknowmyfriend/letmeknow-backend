package com.letmeknow.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import com.letmeknow.dto.member.MemberCreationDto;
import com.letmeknow.enumstorage.status.StoreStatus;
import com.letmeknow.service.member.MemberService;
import com.letmeknow.service.store.StoreService;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoreService storeService;

    @Autowired
    private MemberService memberService;

    @Autowired
    EntityManager em;

    private Long memberTestId;


    @BeforeAll
    void setUp() {
        Long savedMemberId = memberService.joinMember(MemberCreationDto.builder()
                .name("memberTest")
                .email("memberTest@gmail.com")
                .password("password")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        memberTestId = savedMemberId;
    }

    @Test
    void storeInfo() throws Exception {
        //given
        mockMvc.perform(post("/members/{memberId}/stores/new", memberTestId)
                        .param("name", "storeTest")
                        .param("city", "city")
                        .param("street", "street")
                        .param("zipcode", "zipcode"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/members/" + memberTestId + "/stores/6"));

        //then
        mockMvc.perform(get("/members/{memberId}/stores/{storeId}", memberTestId, 6L))
                .andExpect(status().isOk())
                .andExpect(view().name("stores/storeInfo"))
                .andExpect(model().attributeExists("memberId"))
                .andExpect(model().attributeExists("storeDto"));
    }

    @Test
    void createStoreForm() throws Exception {
        mockMvc.perform(get("/members/{memberId}/stores/new", memberTestId))
                .andExpect(status().isOk())
                .andExpect(view().name("stores/storeCreationForm"))
                .andExpect(model().attributeExists("storeCreationForm"));
    }

    @Test
    void createStoreError() throws Exception {
        mockMvc.perform(post("/members/{memberId}/stores/new", memberTestId)
                        .param("name", "storeTest")
//                        .param("city", "city")
                        .param("street", "street")
                        .param("zipcode", "zipcode"))
                .andExpect(view().name("stores/storeCreationForm"))
                .andExpect(model().attributeExists("storeCreationForm"));
    }

    @Test
    void updateStoreForm() throws Exception {
        //given
        //가게 생성
        mockMvc.perform(post("/members/{memberId}/stores/new", memberTestId)
                        .param("name", "storeTest")
                        .param("city", "city")
                        .param("street", "street")
                        .param("zipcode", "zipcode"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/members/" + memberTestId + "/stores/2"));

        //then
        //가게 수정 폼
        mockMvc.perform(get("/members/{memberId}/stores/{storeId}/update", memberTestId, 2L))
                .andExpect(status().isOk())
                .andExpect(view().name("stores/storeUpdateForm"))
                .andExpect(model().attributeExists("storeUpdateForm"));
    }
}