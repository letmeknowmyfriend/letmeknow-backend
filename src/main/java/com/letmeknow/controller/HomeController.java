package com.letmeknow.controller;

import com.letmeknow.exception.member.NoSuchMemberException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.letmeknow.auth.userdetail.PrincipalUserDetails;
import com.letmeknow.dto.member.MemberFindDto;
import com.letmeknow.service.member.MemberService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {
    private final MemberService memberService;

    @GetMapping("/")
    public String home() throws NoSuchMemberException {
        return "home";
    }
}
