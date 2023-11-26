//package com.letmeknow.controller;
//
//import com.letmeknow.auth.userdetail.PrincipalUserDetails;
//import com.letmeknow.domain.member.Member;
//import com.letmeknow.dto.auth.Response;
//import com.letmeknow.enumstorage.response.Status;
//import com.letmeknow.exception.member.NoSuchMemberException;
//import com.letmeknow.exception.notification.NotificationException;
//import com.letmeknow.service.notification.NotificationService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//@RequestMapping("/subscription")
//@RestController
//@RequiredArgsConstructor
//public class NotificationController {
//    private final NotificationService notificationService;
//
//    @PostMapping("/subscribe/{boardId}/")
//    public Response subscribe(@PathVariable("boardId") long boardId) throws NoSuchMemberException {
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        PrincipalUserDetails principalUserDetails = (PrincipalUserDetails) principal;
//
//        Member member = principalUserDetails.getMember();
//
//        try {
//            notificationService.subscribe(member.getEmail(), boardId);
//        }
//        // 이미 구독한 게시판이면
//        catch (NotificationException e) {
//            return Response.builder()
//                    .status(Status.FAIL)
//                    .command("")
//                    .cause(e.getMessage())
//                .build();
//        }
//
//        return Response.builder()
//                .status(Status.SUCCESS)
//                .command("")
//                .cause("")
//            .build();
//    }
//}
