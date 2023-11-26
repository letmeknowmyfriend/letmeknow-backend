package com.letmeknow.controller.restapi;

import com.letmeknow.exception.NoSuchBoardException;
import com.letmeknow.exception.auth.jwt.NoSuchDeviceTokenException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.exception.subscription.SubscriptionException;
import com.letmeknow.service.DeviceTokenService;
import com.letmeknow.service.SubscriptionService;
import com.letmeknow.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionRestController {
    private final MemberService memberService;
    private final SubscriptionService subscriptionService;
    private final DeviceTokenService deviceTokenService;

    @PostMapping(value = "/subscribe/v1/{boardId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity subscribe_v1(@PathVariable long boardId, HttpServletRequest request) {
        String email = (String) request.getAttribute("email");

        // 구독
        subscriptionService.subscribeToTopic(email, String.valueOf(boardId));

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/unsubscribe/v1/{boardId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity unsubscribe_v1(@PathVariable Long boardId, HttpServletRequest request) {
        String email = (String) request.getAttribute("email");

        // 구독 취소
        subscriptionService.unsubscribeFromTopic(email, boardId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/consent/v1", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity consent_v1(HttpServletRequest request, HttpServletResponse response) {
        String deviceToken = deviceTokenService.extractDeviceTokenFromHeader(request);

        String email = (String) request.getAttribute("email");

        // 푸시 알림 동의
        memberService.consentToNotification(email, deviceToken, response);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/refuse/v1", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity refuse_v1(HttpServletRequest request, HttpServletResponse response) {
        String deviceToken = deviceTokenService.extractDeviceTokenFromHeader(request);

        String email = (String) request.getAttribute("email");

        // 푸시 알림 거부
        memberService.refuseToNotification(email, deviceToken, response);

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class, NoSuchBoardException.class, NoSuchMemberException.class})
    public ResponseEntity handleIllegalArgumentException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(NoSuchDeviceTokenException.class)
    public ResponseEntity handleNoSuchDeviceTokenException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(SubscriptionException.class)
    public ResponseEntity handleSubscriptionException(RuntimeException e) {
        return ResponseEntity.internalServerError().body(e.getMessage());
    }
}
