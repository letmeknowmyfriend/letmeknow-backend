package com.letmeknow.controller.restapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.letmeknow.dto.Response;
import com.letmeknow.dto.member.MemberFindDto;
import com.letmeknow.exception.auth.jwt.NoSuchDeviceTokenException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.message.messages.Messages;
import com.letmeknow.service.DeviceTokenService;
import com.letmeknow.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.UnsupportedEncodingException;

import static com.letmeknow.auth.messages.MemberMessages.EMAIL;
import static com.letmeknow.auth.messages.MemberMessages.SEND;
import static com.letmeknow.enumstorage.response.Status.FAIL;
import static com.letmeknow.enumstorage.response.Status.SUCCESS;
import static com.letmeknow.message.messages.Messages.*;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/member", consumes = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MemberRestController {
    private final MemberService memberService;
    private final DeviceTokenService deviceTokenService;

    private final ObjectMapper objectMapper;

    // 회원 조회
    @GetMapping(value = "/v1")
    public ResponseEntity getMember_v1(HttpServletRequest request) throws NoSuchMemberException, JsonProcessingException {
        String email = request.getAttribute("email").toString();

        MemberFindDto memberFindDtoByEmail = memberService.findMemberFindDtoByEmail(email);

        return ResponseEntity.ok(
            Response.builder()
                .status(SUCCESS.getStatus())
                .data(objectMapper.writeValueAsString(memberFindDtoByEmail))
            .build()
        );
    }

    @PostMapping("/change-password/v1")
    public ResponseEntity sendChangePasswordEmail_v1(HttpServletRequest request) throws NoSuchMemberException, MessagingException, UnsupportedEncodingException {
        String email = request.getAttribute("email").toString();

        memberService.sendChangePasswordVerificationEmail(email);

        return ResponseEntity.ok(
            Response.builder()
                .status(SUCCESS.getStatus())
                .message(new StringBuffer().append(EMAIL.getMessage()).append(SEND.getMessage()).append(Messages.SUCCESS.getMessage()).toString())
                .build()
        );
    }

    // 푸시 알림 동의
    @PostMapping(value = "/notification/consent/v1")
    public ResponseEntity consent_v1(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, NoSuchDeviceTokenException, NoSuchMemberException {
        String deviceToken = deviceTokenService.extractDeviceTokenFromHeader(request);

        String email = (String) request.getAttribute("email");

        memberService.consentToNotification(email, deviceToken, response);

        return ResponseEntity.ok(
            Response.builder()
                .status(SUCCESS.getStatus())
                .build()
        );
    }

    // 푸시 알림 거부
    @PostMapping(value = "/notification/refuse/v1")
    public ResponseEntity refuse_v1(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, NoSuchDeviceTokenException, NoSuchMemberException {
        String deviceToken = deviceTokenService.extractDeviceTokenFromHeader(request);

        String email = (String) request.getAttribute("email");

        memberService.refuseToNotification(email, deviceToken, response);

        return ResponseEntity.ok(
            Response.builder()
                .status(SUCCESS.getStatus())
                .build()
        );
    }

    @PostMapping(value = "/delete/v1")
    public ResponseEntity deleteMember(HttpServletRequest request, HttpServletResponse response) {
        String email = request.getAttribute("email").toString();

        memberService.deleteMember(email, response);

        return ResponseEntity.ok(
            Response.builder()
                .status(SUCCESS.getStatus())
                .build()
        );
    }

//    @PostMapping("/update/address")
//    public ResponseEntity<String> updateMemberAddress(MemberAddressUpdateForm memberAddressUpdateForm, HttpServletRequest request) throws NoSuchMemberException {
//        String email = request.getAttribute("email").toString();
//
//        memberService.updateMemberAddress(memberAddressUpdateForm, email);
//
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/update/password")
//    public ResponseEntity<String> updateMemberPassword(MemberPasswordUpdateDto memberPasswordUpdateDto, HttpServletRequest request) throws NoSuchMemberException, PasswordIncorrectException, MemberSignUpValidationException, NewPasswordNotMatchException {
//        String email = request.getAttribute("email").toString();
//
//        memberService.updateMemberPassword(memberPasswordUpdateDto, email);
//
//        return ResponseEntity.ok().build();
//    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity handleConstraintViolationException(Exception e) {
        // 400 Bad Request
        return ResponseEntity.badRequest()
                .body(Response.builder()
                    .status(FAIL.getStatus())
                    .message(new StringBuffer().append(EMAIL.getMessage()).append(NOT_FOUND.getMessage()).toString())
                .build());
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity handle400Exception(Exception e) {
        // 400 Bad Request
        return ResponseEntity.badRequest()
                .body(Response.builder()
                    .status(FAIL.getStatus())
                    .message(e.getMessage())
                .build());
    }

    @ExceptionHandler(NoSuchDeviceTokenException.class)
    public ResponseEntity handle401Exception(Exception e) {
        // 401 Unauthorized
        return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                .body(Response.builder()
                    .status(FAIL.getStatus())
                    .message(e.getMessage())
                .build());
    }

    @ExceptionHandler({NoSuchMemberException.class})
    public ResponseEntity handle410Exception(Exception e) {
        // 410 Gone
        return ResponseEntity.status(GONE)
                .body(Response.builder()
                    .status(FAIL.getStatus())
                    .message(e.getMessage())
                .build());
    }

    @ExceptionHandler({UnsupportedEncodingException.class, MessagingException.class})
    public ResponseEntity handleEmailException() {
        // 500 Internal Server Error
        return ResponseEntity.internalServerError()
                .body(Response.builder()
                    .status(FAIL.getStatus())
                    .message(new StringBuffer().append(EMAIL.getMessage()).append(SEND.getMessage()).append(Messages.FAIL.getMessage()).toString())
                .build());
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity handle500Exception() {
        // 500 Internal Server Error
        return ResponseEntity.internalServerError()
                .body(Response.builder()
                    .status(FAIL.getStatus())
                    .message(new StringBuffer().append(SERVER.getMessage()).append(ERROR.getMessage()).append(OCCURRED.getMessage()).toString())
                .build());
    }
}
