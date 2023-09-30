package com.security.auth;

import com.letmeknow.dto.jwt.JwtFindDto;
import com.letmeknow.dto.member.MemberFindDto;
import com.letmeknow.enumstorage.message.EmailMessage;
import com.letmeknow.exception.member.temporarymember.NoSuchTemporaryMemberException;
import com.letmeknow.service.auth.jwt.JwtService;
import com.letmeknow.service.member.MemberService;
import com.letmeknow.service.member.TemporaryMemberService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;
import java.net.URLEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public class JwtTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TemporaryMemberService temporaryMemberService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private JwtService jwtService;

    private String verificationCode = "";

    private String refreshToken = "";

    @Test
    @DisplayName("임시회원가입")
    @Order(100)
    void temporarySignUp() throws Exception {
        //when
        mockMvc.perform(post("/auth/signup")
                        .param("name", "Cha Cha")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "abcde102938!")
                        .param("passwordAgain", "abcde102938!")
                        .param("city", "Seoul")
                        .param("street", "Gangnam")
                        .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("message/message"))
                .andExpect(model().attribute("message", EmailMessage.VERIFICATION_EMAIL_SENT.getMessage()))
                .andExpect(model().attribute("href", "/auth/login"));

        //then
        //임시회원정보 생성되었는지 확인
        verificationCode = temporaryMemberService.findVerificationCodeByEmail("cha3088@gmail.com");
        assertThat(verificationCode).isNotBlank();
    }

    @Test
    @DisplayName("이메일 인증")
    @Order(200)
    void emailAuthentication() throws Exception {
        //when
        //verrifcationCode로 URL 접근하여 인증
        mockMvc.perform(get("/auth/member/verification-email/" + URLEncoder.encode(verificationCode, "UTF-8")))
                .andExpect(status().isOk())
                .andExpect(view().name("message/message"))
                .andExpect(model().attribute("message", EmailMessage.VERIFICATION_EMAIL_SUCCESS.getMessage()))
                .andExpect(model().attribute("href", "/auth/login"));

        //then
        //임시회원정보가 지워졌는지
        //회원정보가 생성되었는지
        assertThatThrownBy(() -> temporaryMemberService.findIdByEmail("cha3088@gmail.com")).isInstanceOf(NoSuchTemporaryMemberException.class);

        MemberFindDto memberFindDtoByEmail = memberService.findMemberFindDtoByEmail("cha3088@gmail.com");
        assertThat(memberFindDtoByEmail).isNotNull();
    }

    @Test
    @DisplayName("로그인 성공")
    @Order(300)
    void logInSuccess() throws Exception {
        //로그인 제대로
        refreshToken = mockMvc.perform(post("/auth/login/member")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "abcde102938!"))
                //메인 페이지로 redirect
                .andExpect(redirectedUrl("/"))
                //클라이언트에 AccessToken O, RefreshToken O
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                //refreshToken을 반환
                .andReturn().getResponse().getCookie("refreshToken").getValue();

        //서버에 RefreshToken O
        JwtFindDto jwtFindDtoByRefreshToken = jwtService.findJwtFindDtoByRefreshToken(refreshToken);

        assertThat(jwtFindDtoByRefreshToken.getRefreshToken()).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("refreshToken으로 accessToken 재발급")
    @Order(400)
    void reissueAccessToken() throws Exception {
        //when
        //refreshToken으로 accessToken 재발급
        mockMvc.perform(get("/members/1")
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"));

        //then
        //서버에 RefreshToken O
        JwtFindDto jwtFindDtoByRefreshToken = jwtService.findJwtFindDtoByRefreshToken(refreshToken);
        assertThat(jwtFindDtoByRefreshToken.getRefreshToken()).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("아무것도 없으면, 로그인 페이지로 redirect")
    @Order(410)
    void noToken() throws Exception {
        //when
        //accessToken, refreshToken 없이 접근
        mockMvc.perform(get("/members/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }
}
