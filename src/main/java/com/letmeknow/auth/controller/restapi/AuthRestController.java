package com.letmeknow.auth.controller.restapi;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.letmeknow.auth.service.AuthService;
import com.letmeknow.dto.Response;
import com.letmeknow.dto.member.MemberChangePasswordDto;
import com.letmeknow.dto.member.MemberCreationDto;
import com.letmeknow.exception.auth.jwt.NoSuchDeviceTokenException;
import com.letmeknow.exception.auth.jwt.NoSuchRefreshTokenInDBException;
import com.letmeknow.exception.member.MemberSignUpValidationException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.form.auth.MemberSignUpForm;
import com.letmeknow.message.cause.JwtCause;
import com.letmeknow.message.cause.MemberCause;
import com.letmeknow.message.messages.Messages;
import com.letmeknow.service.member.MemberService;
import com.letmeknow.service.member.TemporaryMemberService;
import com.letmeknow.util.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import static com.letmeknow.auth.messages.MemberMessages.*;
import static com.letmeknow.auth.service.JwtService.*;
import static com.letmeknow.enumstorage.response.Status.FAIL;
import static com.letmeknow.enumstorage.response.Status.SUCCESS;
import static com.letmeknow.message.cause.JwtCause.JWT;
import static com.letmeknow.message.cause.MemberCause.FORM;
import static com.letmeknow.message.cause.MemberCause.FORM.getCause();
import static com.letmeknow.message.messages.Messages.*;
import static com.letmeknow.message.messages.Messages.REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/auth", consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthRestController {
    private final TemporaryMemberService temporaryMemberService;
    private final AuthService authService;
    private final MemberService memberService;

    @PostMapping("/signup/v1")
    public ResponseEntity<Response> temporarySignUpV1(@RequestBody @Valid MemberSignUpForm memberSignUpForm) throws MemberSignUpValidationException, MessagingException, UnsupportedEncodingException {
        // 중복된 이메일이 존재하지 않으면,
        // 임시 회원가입을 시도한다.
        temporaryMemberService.joinTemporaryMember(MemberSignUpForm.builder()
            .name(memberSignUpForm.getName())
            .email(memberSignUpForm.getEmail())
            .password(memberSignUpForm.getPassword())
            .passwordAgain(memberSignUpForm.getPasswordAgain())
            .city(memberSignUpForm.getCity())
            .street(memberSignUpForm.getStreet())
            .zipcode(memberSignUpForm.getZipcode())
            .build());

        return ResponseEntity.ok(Response.builder()
            .status(SUCCESS.getStatus())
            .message(new StringBuffer().append(TEMPORARY_MEMBER.getMessage()).append(SIGN_UP.getMessage()).append(Messages.SUCCESS.getMessage()).toString())
            .build());
    }

    @PostMapping(value = "/reissue/v1")
    public ResponseEntity reissueJwtsV1(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, NoSuchDeviceTokenException, JWTVerificationException, IOException {
        String[] jwts = authService.reissueJwts(request);

        // access token, refresh token을 헤더에 실어서 보낸다.
        response.setHeader(ACCESS_TOKEN_HEADER, BEARER + jwts[0]);
        response.setHeader(REFRESH_TOKEN_HEADER, BEARER + jwts[1]);

        // redirect URL로 보내야함
        return ResponseEntity.ok()
            .build();
    }

    @PostMapping("/reset-password/v1")
    public ResponseEntity sendResetPasswordEmail_v1(@RequestBody @Valid MemberChangePasswordDto memberChangePasswordDto) throws ConstraintViolationException, NoSuchMemberException, UnsupportedEncodingException, MessagingException {
        memberService.sendChangePasswordVerificationEmail(memberChangePasswordDto.getEmail());

        return ResponseEntity.ok()
            .body(Response.builder()
                .status(SUCCESS.getStatus())
                .message(new StringBuffer().append(EMAIL.getMessage()).append(SEND.getMessage()).append(Messages.SUCCESS.getMessage()).toString())
                .build());
    }

    // 임시 회원가입 시, 폼에 빈 값이 있으면
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity handleConstraintViolationException(ConstraintViolationException e) {
        return ResponseEntity.badRequest().body(Response.builder()
                .status(FAIL.getStatus())
                .cause(FORM.getCause())
                .message(REQUEST.getMessage() + INVALID.getMessage())
            .build());
    }

    // 임시 회원가입 시, 폼이 유효하지 않으면
    @ExceptionHandler(MemberSignUpValidationException.class)
    public ResponseEntity handleMemberSignUpValidationException(MemberSignUpValidationException e) {
        return ResponseEntity.badRequest()
            .body(Response.builder()
                .status(FAIL.getStatus())
                .cause(e.getReason())
                .message(e.getMessage())
            .build());
    }

    // 필요한 인자가 없으면
    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    public ResponseEntity handle400Exception(Exception e) {
        // 400 Bad Request
        return ResponseEntity.badRequest()
            .body(Response.builder()
                .status(FAIL.getStatus())
                .message(REQUEST.getMessage() + INVALID.getMessage())
                .build());
    }

    // Refresh Token이 유효하지 않으면 || Refresh Token이 DB에 없으면 || Device Token이 DB에 없으면
    @ExceptionHandler({JWTVerificationException.class, NoSuchRefreshTokenInDBException.class, NoSuchDeviceTokenException.class})
    public ResponseEntity handleUnauthorized(Exception e, HttpServletResponse response) {
        // ToDo: null 해도 되는지 테스트
        response.setHeader(ACCESS_TOKEN_HEADER, null);

        response.setCharacterEncoding("UTF-8");

        return ResponseEntity.status(SC_UNAUTHORIZED)
            .body(Response.builder()
                .status(FAIL.getStatus())
                .cause(JWT.getCause())
                .message(e.getMessage())
                .build());
    }

    @ExceptionHandler({NoSuchMemberException.class})
    public ResponseEntity handle410Exception(Exception e, HttpServletResponse response) {
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(ACCESS_TOKEN_HEADER, null);
        response.setHeader(REFRESH_TOKEN_HEADER, null);

        // 410 Gone
        return ResponseEntity.status(GONE)
            .body(Response.builder()
                .status(FAIL.getStatus())
                .message(e.getMessage())
                .build());
    }

    // 메일 전송에 실패한 경우로, 임시 회원가입 안된 상황
    @ExceptionHandler({MessagingException.class, UnsupportedEncodingException.class})
    public ResponseEntity handle500Exception() {
        return ResponseEntity.internalServerError().body(Response.builder()
            .status(FAIL.getStatus())
            .message(new StringBuffer().append(EMAIL.getMessage()).append(SEND.getMessage()).append(Messages.FAIL.getMessage()).toString())
            .build());
    }

    private String extractRedirectURL(HttpServletRequest request) throws IllegalArgumentException {
        String redirectURL = Optional.ofNullable(request.getParameter("redirectUrl"))
            .orElseThrow(() -> new IllegalArgumentException(new StringBuffer().append(REDIRECT_URL.getMessage()).append(NOT_FOUND.getMessage()).toString()));

        return redirectURL;
    }
}
