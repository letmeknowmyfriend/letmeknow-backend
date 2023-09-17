package com.letmeknow.auth;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import com.letmeknow.dto.jwt.JwtFindDto;
import com.letmeknow.dto.member.MemberFindDto;
import com.letmeknow.enumstorage.errormessage.auth.EmailErrorMessage;
import com.letmeknow.enumstorage.errormessage.auth.PasswordErrorMessage;
import com.letmeknow.enumstorage.message.EmailMessage;
import com.letmeknow.enumstorage.status.MemberStatus;
import com.letmeknow.exception.member.temporarymember.NoSuchTemporaryMemberException;
import com.letmeknow.service.auth.jwt.JwtService;
import com.letmeknow.service.member.MemberService;
import com.letmeknow.service.member.TemporaryMemberService;

import java.net.URLEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public class FormTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TemporaryMemberService temporaryMemberService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private JwtService jwtService;

    private String verificationCode = "";

    private String passwordVerificationCode = "";

    @Test
    @DisplayName("이메일 형식 검증")
    @Order(0)
    void validateEmail() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .param("name", "Cha Cha")
                        .param("email", "")
                        .param("password", "abcde102938!")
                        .param("passwordAgain", "abcde102938!")
                        .param("city", "Seoul")
                        .param("street", "Gangnam")
                        .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signUpForm"))
                .andExpect(model().attributeHasFieldErrors("memberSignUpForm", "email"));

        mockMvc.perform(post("/auth/signup")
                .param("name", "Cha Cha")
                .param("email", "cha3088gmail.com")
                .param("password", "abcde102938!")
                .param("passwordAgain", "abcde102938!")
                .param("city", "Seoul")
                .param("street", "Gangnam")
                .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signUpForm"))
                .andExpect(model().attributeHasFieldErrors("memberSignUpForm", "email"));

        mockMvc.perform(post("/auth/signup")
                        .param("name", "Cha Cha")
                        .param("email", "cha3088@gmailcom")
                        .param("password", "abcde102938!")
                        .param("passwordAgain", "abcde102938!")
                        .param("city", "Seoul")
                        .param("street", "Gangnam")
                        .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signUpForm"))
                .andExpect(model().attributeHasFieldErrors("memberSignUpForm", "email"));
    }

    @Test
    @DisplayName("비밀번호가 공백인 경우")
    @Order(1)
    void validatePasswordBlank() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .param("name", "Cha Cha")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "")
                        .param("passwordAgain", "")
                        .param("city", "Seoul")
                        .param("street", "Gangnam")
                        .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signUpForm"))
                .andExpect(model().attributeHasFieldErrors("memberSignUpForm", "password"));
    }

    @Test
    @DisplayName("비밀번호가 8자 미만인 경우")
    @Order(2)
    void validatePasswordLessThan8() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .param("name", "Cha Cha")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "dl@j102")
                        .param("passwordAgain", "dl@j102")
                        .param("city", "Seoul")
                        .param("street", "Gangnam")
                        .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signUpForm"))
                .andExpect(model().attributeHasFieldErrors("memberSignUpForm", "password"));
    }

    @Test
    @DisplayName("비밀번호가 30자 초과인 경우")
    @Order(3)
    void validatePasswordMoreThan30() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .param("name", "Cha Cha")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "aldsfkjaejkheufhuekh@kdjsffkdshjfjkshdfjlshdlkvnxcnvbxk10582subhskdfhskhdskjh!")
                        .param("passwordAgain", "aldsfkjaejkheufhuekh@kdjsffkdshjfjkshdfjlshdlkvnxcnvbxk10582subhskdfhskhdskjh!")
                        .param("city", "Seoul")
                        .param("street", "Gangnam")
                        .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signUpForm"))
                .andExpect(model().attributeHasFieldErrors("memberSignUpForm", "password"));
    }

    @Test
    @DisplayName("비밀번호에 숫자가 없는 경우")
    @Order(4)
    void validatePasswordNoNumbers() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .param("name", "Cha Cha")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "kh@kdjsffkdshjfjskjh!")
                        .param("passwordAgain", "kh@kdjsffkdshjfjskjh!")
                        .param("city", "Seoul")
                        .param("street", "Gangnam")
                        .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signUpForm"))
                .andExpect(model().attributeHasFieldErrors("memberSignUpForm", "password"));
    }

    @Test
    @DisplayName("비밀번호에 특수문자가 없는 경우")
    @Order(5)
    void validatePasswordNoSpecialCharacter() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .param("name", "Cha Cha")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "khkdjsffkd1983")
                        .param("passwordAgain", "khkdjsffkd1983")
                        .param("city", "Seoul")
                        .param("street", "Gangnam")
                        .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signUpForm"))
                .andExpect(model().attributeHasFieldErrors("memberSignUpForm", "password"));
    }

    @Test
    @DisplayName("비밀번호에 허용되지 않은 특수문자가 있는 경우")
    @Order(6)
    void validatePasswordContainsNotPermittedSpecialCharacters() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .param("name", "Cha Cha")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "khkdjsffk,d1983")
                        .param("passwordAgain", "khkdjsffk,d1983")
                        .param("city", "Seoul")
                        .param("street", "Gangnam")
                        .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signUpForm"))
                .andExpect(model().attributeHasFieldErrors("memberSignUpForm", "password"));
    }

    @Test
    @DisplayName("비밀번호에 영어가 아닌 언어가 있는 경우")
    @Order(7)
    void validatePasswordOtherLanguages() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .param("name", "Cha Cha")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "khㅁd엇!sffk@d1983")
                        .param("passwordAgain", "khㅁd엇!sffk@d1983")
                        .param("city", "Seoul")
                        .param("street", "Gangnam")
                        .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signUpForm"))
                .andExpect(model().attributeHasFieldErrors("memberSignUpForm", "password"));
    }

    @Test
    @DisplayName("비밀번호에 반복된 문자가 3개 이상 있는 경우")
    @Order(8)
    void validatePasswordContainsRepeated3CharactersMoreThan3() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .param("name", "Cha Cha")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "khkdj!sfffk@d1983")
                        .param("passwordAgain", "khkdj!sfffk@d1983")
                        .param("city", "Seoul")
                        .param("street", "Gangnam")
                        .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signUpForm"))
                .andExpect(model().attributeHasFieldErrors("memberSignUpForm", "password"));
    }

    @Test
    @DisplayName("비밀번호에 아이디 포함되어있는 경우")
    @Order(9)
    void validatePasswordContainsId() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .param("name", "Cha Cha")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "khcha3088fk@d1983")
                        .param("passwordAgain", "khcha3088fk@d1983")
                        .param("city", "Seoul")
                        .param("street", "Gangnam")
                        .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signUpForm"))
                .andExpect(model().attributeHasFieldErrors("memberSignUpForm", "password"));
    }

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
    @DisplayName("임시회원가입된 회원인 경우")
    @Order(101)
    void temporarySignUpAlreadyTemporaryMember() throws Exception {
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
                .andExpect(model().attribute("message", EmailMessage.CHECK_VERIFICATION_EMAIL.getMessage()))
                .andExpect(model().attribute("href", "/auth/member/notice/verification-email"));
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
    @DisplayName("이미 가입된 회원인 경우")
    @Order(210)
    void temporarySignUpAlreadyMember() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .param("name", "Cha Cha")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "abcde102938!")
                        .param("passwordAgain", "abcde102938!")
                        .param("city", "Seoul")
                        .param("street", "Gangnam")
                        .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signUpForm"))
                .andExpect(model().attributeHasFieldErrors("memberSignUpForm", "email"));
    }

    @Test
    @DisplayName("로그인 폼에 잘못된 값이 들어왔을 때, 로그인 실패")
    @Order(300)
    void wrongValueLogInFail() throws Exception {
        //로그인 폼에 잘못된 이메일이 들어왔을 때
        mockMvc.perform(post("/auth/login/member")
                        .param("email", "cha3088gmail.com")
                        .param("password", "abcde102938!"))
                //로그인 폼으로 돌아간다.
                .andExpect(redirectedUrl("/auth/login?error=email&exception=" + URLEncoder.encode(EmailErrorMessage.NOT_VALID_EMAIL.getMessage(), "UTF-8")));

        //로그인 폼에 비밀번호가 비어있는 경우
        mockMvc.perform(post("/auth/login/member")
                        .param("email", "cha3088@gmail.com")
                        .param("password", ""))
                //로그인 폼으로 돌아간다.
                .andExpect(redirectedUrl("/auth/login?error=password&exception=" + URLEncoder.encode(PasswordErrorMessage.PASSWORD_IS_EMPTY.getMessage(), "UTF-8") + "&email=" + "cha3088@gmail.com"));

        //서버에 RefreshToken X
        MemberFindDto memberFindDtoByEmail = memberService.findMemberFindDtoByEmail("cha3088@gmail.com");
        assertThat(memberFindDtoByEmail.getJwtId()).isNull();
    }

    @Test
    @DisplayName("비밀번호를 틀렸을 때, 로그인 실패")
    @Order(301)
    void wrongPasswordLogInFail() throws Exception {
        //비밀번호를 틀렸을 때
        //1번 틀렸을 때
        mockMvc.perform(post("/auth/login/member")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "tmdgP201029"))
                .andExpect(status().is3xxRedirection())
                //로그인 폼으로 돌아간다.
                .andExpect(redirectedUrl("/auth/login?error=password&exception=" + URLEncoder.encode(PasswordErrorMessage.PASSWORD_DOES_NOT_MATCH.getMessage(), "UTF-8") + "&email=" + "cha3088@gmail.com"))
                //클라이언트에 AccessToken X, RefreshToken X
                .andExpect(cookie().doesNotExist("accessToken"))
                .andExpect(cookie().doesNotExist("refreshToken"));

        //2번 틀렸을 때
        mockMvc.perform(post("/auth/login/member")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "tmdgP201029"))
                .andExpect(status().is3xxRedirection())
                //로그인 폼으로 돌아간다.
                .andExpect(redirectedUrl("/auth/login?error=password&exception=" + URLEncoder.encode(PasswordErrorMessage.PASSWORD_DOES_NOT_MATCH.getMessage(), "UTF-8") + "&email=" + "cha3088@gmail.com"))
                //클라이언트에 AccessToken X, RefreshToken X
                .andExpect(cookie().doesNotExist("accessToken"))
                .andExpect(cookie().doesNotExist("refreshToken"));

        //3번 틀렸을 때
        mockMvc.perform(post("/auth/login/member")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "tmdgP201029"))
                .andExpect(status().is3xxRedirection())
                //로그인 폼으로 돌아간다.
                .andExpect(redirectedUrl("/auth/login?error=password&exception=" + URLEncoder.encode(PasswordErrorMessage.PASSWORD_DOES_NOT_MATCH.getMessage(), "UTF-8") + "&email=" + "cha3088@gmail.com"))
                //클라이언트에 AccessToken X, RefreshToken X
                .andExpect(cookie().doesNotExist("accessToken"))
                .andExpect(cookie().doesNotExist("refreshToken"));

        //4번 틀렸을 때
        mockMvc.perform(post("/auth/login/member")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "tmdgP201029"))
                .andExpect(status().is3xxRedirection())
                //로그인 폼으로 돌아간다.
                .andExpect(redirectedUrl("/auth/login?error=password&exception=" + URLEncoder.encode(PasswordErrorMessage.PASSWORD_DOES_NOT_MATCH.getMessage(), "UTF-8") + "&email=" + "cha3088@gmail.com"))
                //클라이언트에 AccessToken X, RefreshToken X
                .andExpect(cookie().doesNotExist("accessToken"))
                .andExpect(cookie().doesNotExist("refreshToken"));

        //5번 틀렸을 때
        mockMvc.perform(post("/auth/login/member")
                        .param("email", "cha3088@gmail.com")
                        .param("password", "tmdgP201029"))
                .andExpect(status().is3xxRedirection())
                //로그인 폼으로 돌아간다.
                .andExpect(redirectedUrl("/auth/member/notice/change-password"))
                //클라이언트에 AccessToken X, RefreshToken X
                .andExpect(cookie().doesNotExist("accessToken"))
                .andExpect(cookie().doesNotExist("refreshToken"));

        //계정 잠긴거 확인
        MemberFindDto memberFindDtoByEmail = memberService.findMemberFindDtoByEmail("cha3088@gmail.com");
        assertThat(memberFindDtoByEmail.getStatus()).isEqualTo(MemberStatus.LOCKED.toString());
        assertThat(memberFindDtoByEmail.getLogInAttempt()).isGreaterThanOrEqualTo(5);

        //passwordVerificationCode 저장
        passwordVerificationCode = memberFindDtoByEmail.getPasswordVerificationCode();
        assertThat(memberFindDtoByEmail.getPasswordVerificationCode()).isNotBlank();

        //서버에 RefreshToken X
        assertThat(memberFindDtoByEmail.getJwtId()).isNull();
    }

    @Test
    @DisplayName("비밀번호 바꾸자")
    @Order(303)
    void changePassword() throws Exception {
        //비밀번호 재설정
        //"/auth/member/change-password/{passwordVerificationCode}"로 post 요청
        mockMvc.perform(post("/auth/member/change-password/{passwordVerificationCode}", passwordVerificationCode)
                        .param("newPassword", "abcde102938!")
                        .param("newPasswordAgain", "abcde102938!"))
                .andExpect(status().isOk())
                //로그인 페이지로 redirect
                .andExpect(view().name("message/message"))
                //클라이언트에 AccessToken X, RefreshToken X
                .andExpect(cookie().doesNotExist("accessToken"))
                .andExpect(cookie().doesNotExist("refreshToken"));

        //계정 잠금 해제 확인
        MemberFindDto memberFindDtoByEmailAgain = memberService.findMemberFindDtoByEmail("cha3088@gmail.com");
        assertThat(memberFindDtoByEmailAgain.getStatus()).isEqualTo(MemberStatus.ACTIVE.toString());

        //passwordVerificationCode 삭제 확인
        assertThat(memberFindDtoByEmailAgain.getPasswordVerificationCode()).isBlank();

        //logInAttemptCount 0으로 초기화 확인
        assertThat(memberFindDtoByEmailAgain.getLogInAttempt()).isEqualTo(0);
    }

    @Test
    @DisplayName("로그인 성공")
    @Order(304)
    void logInSuccess() throws Exception {
        //로그인 제대로
        String refreshToken = mockMvc.perform(post("/auth/login/member")
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
    @DisplayName("로그인 된 상태에서, 접근 가능한 페이지를 가보자")
    @Order(400)
    void accessiblePages() {
        //given


        //when


        //then

        assertThat(1).isEqualTo(2);
    }

    @Test
    @DisplayName("로그아웃")
    @Order(500)
    void logOut() {
        //given


        //when


        //then
        //클라이언트에 AccessToken X, RefreshToken X
        //DB에 RefreshToken X
        //로그인 폼으로 돌아간다.
        assertThat(1).isEqualTo(2);
    }
}
