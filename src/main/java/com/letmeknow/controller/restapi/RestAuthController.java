package com.letmeknow.controller.restapi;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.letmeknow.dto.auth.Response;
import com.letmeknow.dto.member.MemberCreationDto;
import com.letmeknow.exception.auth.jwt.NoRefreshTokenException;
import com.letmeknow.enumstorage.response.Command;
import com.letmeknow.enumstorage.response.Status;
import com.letmeknow.exception.auth.EmailException;
import com.letmeknow.exception.auth.NameException;
import com.letmeknow.exception.member.InvalidPasswordException;
import com.letmeknow.exception.member.PasswordException;
import com.letmeknow.exception.member.PasswordNotEqualException;
import com.letmeknow.exception.controller.NoRedirectURLException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.exception.notification.NotificationException;
import com.letmeknow.form.auth.MemberSignUpForm;
import com.letmeknow.message.MessageMaker;
import com.letmeknow.service.auth.jwt.JwtService;
import com.letmeknow.service.member.MemberService;
import com.letmeknow.service.member.TemporaryMemberService;
import com.letmeknow.util.Validator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import static com.letmeknow.enumstorage.response.Command.*;
import static com.letmeknow.message.reason.HttpReason.*;
import static com.letmeknow.message.reason.JwtReason.REFRESH_TOKEN;
import static com.letmeknow.message.reason.MemberReason.*;
import static com.letmeknow.message.reason.MemberReason.EMAIL;
import static com.letmeknow.message.reason.NotificationReason.DEVICE_TOKEN;
import static com.letmeknow.message.reason.NotificationReason.FCM;
import static com.letmeknow.message.Message.*;
import static com.letmeknow.service.auth.jwt.JwtService.*;
import static com.letmeknow.service.auth.jwt.JwtService.BEARER;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RestAuthController {
    private final ObjectMapper objectMapper;
    private final MemberService memberService;
    private final TemporaryMemberService temporaryMemberService;
    private final JwtService jwtService;
    private final MessageMaker messageMaker;
    private final Validator validator;

    @PostMapping("signup/v1")
    public Response temporarySignUpV1(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if (!request.getContentType().equals("application/json")) {
                response.setStatus(SC_BAD_REQUEST);

                return Response.builder()
                    .status(Status.FAIL)
                    .command(messageMaker.add(CONTENT_TYPE).add(NOT_JSON).toString())
                    .reason(messageMaker.add(CONTENT_TYPE).add(NOT_JSON).toString())
                    .build();
            }

            MemberSignUpForm memberSignUpForm = objectMapper.readValue(StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8), MemberSignUpForm.class);

            // 이름 검사
            String name = Optional.ofNullable(memberSignUpForm.getName()).orElseThrow(
                () -> new NameException(messageMaker.add(NAME).add(NOT_EXISTS).toString()));

            validator.isValidName(name);

            // 이메일 규칙 검사
            String email = Optional.ofNullable(memberSignUpForm.getEmail()).orElseThrow(
                () -> new EmailException(messageMaker.add(EMAIL).add(NOT_EXISTS).toString()));

            // 이메일이 유효하지 않으면
            validator.isValidEmail(email);

            // 비밀번호
            String password = Optional.ofNullable(memberSignUpForm.getPassword()).orElseThrow(
                () -> new PasswordException(messageMaker.add(PASSWORD).add(NOT_EXISTS).toString()));

            String passwordAgain = Optional.ofNullable(memberSignUpForm.getPasswordAgain()).orElseThrow(
                () -> new PasswordException(messageMaker.add(PASSWORD).add(AGAIN).add(NOT_EXISTS).toString()));

            // 비밀번호 일치 검사
            if (!password.equals(passwordAgain)) {
                throw new PasswordNotEqualException(messageMaker.add(PASSWORD).add(AGAIN).add(NOT_EQUAL).toString());
            }

            // 비밀번호 규칙 검사
            validator.isValidPassword(email, password);

            // 중복된 이메일이 존재하는지 확인한다.
            if (!memberService.isEmailUsed(email))
            // 중복된 이메일이 존재하지 않으면,
            {
                try {
                    // 임시 회원가입을 시도한다.
                    temporaryMemberService.joinTemporaryMember(MemberCreationDto.builder()
                        .name(memberSignUpForm.getName())
                        .email(memberSignUpForm.getEmail())
                        .password(memberSignUpForm.getPassword())
                        .city(memberSignUpForm.getCity())
                        .street(memberSignUpForm.getStreet())
                        .zipcode(memberSignUpForm.getZipcode())
                        .build());

                    return Response.builder()
                        .status(Status.SUCCESS)
                        .command(messageMaker.add(Command.VERIFICATION).add(Command.EMAIL).toString())
                        .reason(messageMaker.add(TEMPORARY_SIGN_UP).add(SUCCESS).toString())
                        .build();

                } catch (DataIntegrityViolationException e) {
                    // 중복된 이메일은 존재 하지 않지만, 임시 회원가입 기록이 존재하는 경우
                    response.setStatus(SC_BAD_REQUEST);

                    return Response.builder()
                        .status(Status.FAIL)
                        .command(messageMaker.add(Command.VERIFICATION).add(Command.EMAIL).toString())
                        .reason(messageMaker.add(TEMPORARY_MEMBER).add(EXISTS).toString())
                        .build();
                } catch (UnsupportedEncodingException | MessagingException e) {
                    // 메일 전송에 실패한 경우, 임시 회원가입 안된 상황
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                    return Response.builder()
                        .status(Status.FAIL)
                        .command(messageMaker.add(AUTH).add(API).add(SIGNUP).add(V1).toString())
                        .reason(messageMaker.add(TEMPORARY_SIGN_UP).add(EMAIL).add(SEND).add(FAIL).toString())
                        .build();
                }
            }
            // 중복된 이메일이 존재하면
            else {
                response.setStatus(SC_BAD_REQUEST);

                return Response.builder()
                    .status(Status.FAIL)
                    .command(messageMaker.add(AUTH).add(API).add(SIGNUP).add(V1).toString())
                    .reason(messageMaker.add(DUPLICATED).add(EMAIL).add(EXISTS).toString())
                    .build();
            }
        }
        // 이름 규칙 위반 || 이메일 규칙 위반 || 비밀번호 규칙 위반
        catch (NameException | EmailException | InvalidPasswordException e) {
            response.setStatus(SC_BAD_REQUEST);

            return Response.builder()
                .status(Status.FAIL)
                .command("")
                .reason(e.getMessage())
                .build();
        }
        // 새 비밀번호끼리 일치하지 않을 때
        catch (PasswordNotEqualException e) {
            response.setStatus(SC_BAD_REQUEST);

            return Response.builder()
                .status(Status.FAIL)
                .command(messageMaker.add(AUTH).add(API).add(SIGNUP).add(V1).toString())
                .reason(e.getMessage())
                .build();
        }
        // 비밀번호 규칙 위반
        catch (PasswordException e) {
            response.setStatus(SC_BAD_REQUEST);

            return Response.builder()
                .status(Status.FAIL)
                .command(messageMaker.add(AUTH).add(API).add(SIGNUP).add(V1).toString())
                .reason(messageMaker.add(PASSWORD).add(INVALID).toString())
                .build();
        }
    }

    @PostMapping("/reissue/v1")
    public Response reissueJwts(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // redirectURL 추출
            String redirectURL = extractRedirectURL(request);

            // Header에서 deviceToken 추출
            String deviceToken = extractDeviceToken(request);

            // Header에서 refreshToken 추출
            String refreshToken = jwtService.extractRefreshToken(request);

            String email = jwtService.validateAndExtractEmailFromToken(refreshToken);

            // token들을 발급한다.
            String[] accessTokenAndRefreshToken = jwtService.issueTokens(email, deviceToken);

            // access token, refresh token을 헤더에 실어서 보낸다.
            response.setHeader(ACCESS_TOKEN_HEADER, BEARER + accessTokenAndRefreshToken[0]);
            response.setHeader(REFRESH_TOKEN_HEADER, BEARER + accessTokenAndRefreshToken[1]);

            // redirect URL로 보내야함
            response.sendRedirect(redirectURL);
        }
        // redirectURL이 없으면 || deviceToken이 없으면
        catch (NoRedirectURLException | NotificationException e) {
            // 400 Bad Request
            response.setStatus(SC_BAD_REQUEST);

            return Response.builder()
                        .status(Status.FAIL)
                        .command("")
                        .reason(e.getMessage())
                    .build();
        }
        // refreshToken이 없으면 || refreshToken이 유효하지 않으면 | 해당 회원이 없으면
        catch (NoRefreshTokenException | JWTVerificationException | NoSuchMemberException e) {
            // 로그인 다시 해야지
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            return Response.builder()
                        .status(Status.FAIL)
                        .command(messageMaker.add(AUTH).add(API).add(SIGNUP).add(V1).toString())
                        .reason(messageMaker.add(REFRESH_TOKEN).add(NOT_EXISTS).toString())
                .build();
        }

        return Response.builder()
                    .status(Status.SUCCESS)
                    .command("")
                    .reason("")
            .build();
    }

    private String extractRedirectURL(HttpServletRequest request) throws NoRedirectURLException {
        String redirectURL = Optional.ofNullable(request.getParameter("redirect-url"))
            .orElseThrow(() -> new NoRedirectURLException(messageMaker.add(REDIRECT_URL).add(NOT_EXISTS).toString()));

        return redirectURL;
    }

    private String extractDeviceToken(HttpServletRequest request) throws NotificationException {
        String deviceToken = Optional.ofNullable(request.getHeader("DeviceToken"))
            .orElseThrow(() -> new NotificationException(messageMaker.add(FCM).add(DEVICE_TOKEN).add(NOT_EXISTS).toString()));

        return deviceToken;
    }
}
