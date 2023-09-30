package com.letmeknow.controller;

import com.letmeknow.auth.PrincipalUserDetails;
import com.letmeknow.domain.member.Member;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.service.auth.jwt.JwtService;
import com.letmeknow.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/subscription")
@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final JwtService jwtService;

    @PostMapping("/subscribe/{boardId}/")
    public String subscribe(@PathVariable("boardId") Long boardId, @RequestParam("deviceToken") String deviceToken) throws NoSuchMemberException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PrincipalUserDetails principalUserDetails = (PrincipalUserDetails) principal;

        Member member = principalUserDetails.getMember();

        notificationService.subscribe(member.getEmail(), deviceToken, boardId);

        return "subscribed";
    }
}
