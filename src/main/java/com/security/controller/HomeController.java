package com.security.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.security.config.auth.PrincipalUserDetails;
import com.security.dto.member.MemberFindDto;
import com.security.service.member.MemberService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {
    private final MemberService memberService;

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        log.info("home controller");

        if (authentication != null) {
            PrincipalUserDetails principal = (PrincipalUserDetails) authentication.getPrincipal();
            model.addAttribute("username", principal.getUsername());
            MemberFindDto memberFindDtoByEmail = memberService.findMemberFindDtoByEmail(principal.getUsername());
            model.addAttribute("memberFindDto", memberFindDtoByEmail);
        }

        return "home";
    }
}
