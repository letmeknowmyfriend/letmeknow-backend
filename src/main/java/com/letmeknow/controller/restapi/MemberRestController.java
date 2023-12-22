package com.letmeknow.controller.restapi;

import com.letmeknow.dto.Response;
import com.letmeknow.dto.member.MemberChangePasswordDto;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.message.messages.Messages;
import com.letmeknow.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotBlank;
import java.io.UnsupportedEncodingException;

import static com.letmeknow.auth.messages.MemberMessages.*;
import static com.letmeknow.enumstorage.response.Status.FAIL;
import static com.letmeknow.enumstorage.response.Status.SUCCESS;
import static com.letmeknow.message.messages.Messages.*;
import static org.springframework.http.HttpStatus.GONE;

@RestController
@RequestMapping(value = "/api/member", consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MemberRestController {
    private final MemberService memberService;

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

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity handle400Exception(Exception e) {
        // 400 Bad Request
        return ResponseEntity.badRequest()
            .body(Response.builder()
                .status(FAIL.getStatus())
                .message(new StringBuffer().append(REQUEST.getMessage()).append(INVALID.getMessage()).toString())
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
    public ResponseEntity handle500Exception(Exception e) {
        // 500 Internal Server Error
        return ResponseEntity.internalServerError()
            .body(Response.builder()
                .status(FAIL.getStatus())
                .message(new StringBuffer().append(EMAIL.getMessage()).append(SEND.getMessage()).append(Messages.FAIL.getMessage()).toString())
                .build());
    }
}
