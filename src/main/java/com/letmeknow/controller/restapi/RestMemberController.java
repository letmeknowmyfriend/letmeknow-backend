package com.letmeknow.controller.restapi;

import com.letmeknow.dto.member.MemberPasswordUpdateDto;
import com.letmeknow.exception.member.*;
import com.letmeknow.form.MemberAddressUpdateForm;
import com.letmeknow.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class RestMemberController {
    private final MemberService memberService;

    @PostMapping("/update/address")
    public ResponseEntity<String> updateMemberAddress(MemberAddressUpdateForm memberAddressUpdateForm, HttpServletRequest request) throws NoSuchMemberException {
        String email = request.getAttribute("email").toString();

        memberService.updateMemberAddress(memberAddressUpdateForm, email);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/update/password")
    public ResponseEntity<String> updateMemberPassword(MemberPasswordUpdateDto memberPasswordUpdateDto, HttpServletRequest request) throws NoSuchMemberException, PasswordIncorrectException, InvalidPasswordException, NewPasswordNotMatchException {
        String email = request.getAttribute("email").toString();

        memberService.updateMemberPassword(memberPasswordUpdateDto, email);

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler({NoSuchMemberException.class, NewPasswordNotMatchException.class, InvalidPasswordException.class, PasswordIncorrectException.class})
    public ResponseEntity<String> handleNoSuchMemberException(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
