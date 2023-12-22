package com.letmeknow.controller.restapi;

import com.letmeknow.dto.Response;
import com.letmeknow.exception.NoSuchBoardException;
import com.letmeknow.exception.auth.jwt.NoSuchDeviceTokenException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.exception.subscription.SubscriptionException;
import com.letmeknow.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.letmeknow.enumstorage.response.Status.FAIL;
import static com.letmeknow.enumstorage.response.Status.SUCCESS;
import static com.letmeknow.message.messages.Messages.INVALID;
import static com.letmeknow.message.messages.Messages.REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.GONE;

@RestController
@RequestMapping(value = "/api/subscription", consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class SubscriptionRestController {
    private final SubscriptionService subscriptionService;

    // 게시판 구독
    @PostMapping(value = "/subscribe/v1/{boardId}")
    public ResponseEntity subscribe_v1(@PathVariable Long boardId, HttpServletRequest request) {
        String email = (String) request.getAttribute("email");

        subscriptionService.subscribeToTopic(email, String.valueOf(boardId));

        return ResponseEntity.ok(
            Response.builder()
                .status(SUCCESS.getStatus())
            .build()
        );
    }

    // 게시판 구독 취소
    @DeleteMapping(value = "/unsubscribe/v1/{boardId}")
    public ResponseEntity unsubscribe_v1(@PathVariable Long boardId, HttpServletRequest request) {
        String email = (String) request.getAttribute("email");

        subscriptionService.unsubscribeFromTopic(email, boardId);

        return ResponseEntity.ok(
            Response.builder()
                .status(SUCCESS.getStatus())
            .build()
        );
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class})
    public ResponseEntity handle400Exception(RuntimeException e) {
        return ResponseEntity.badRequest().body(
            Response.builder()
                    .status(FAIL.getStatus())
                    .message(new StringBuffer().append(REQUEST.getMessage()).append(INVALID.getMessage()).toString())
                .build()
        );
    }

    @ExceptionHandler({NoSuchBoardException.class, NoSuchMemberException.class})
    public ResponseEntity handle410Exception(RuntimeException e) {
        return ResponseEntity.status(GONE).body(
            Response.builder()
                    .status(FAIL.getStatus())
                    .message(e.getMessage())
                .build()
        );
    }

    @ExceptionHandler(NoSuchDeviceTokenException.class)
    public ResponseEntity handleNoSuchDeviceTokenException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            Response.builder()
                    .status(FAIL.getStatus())
                    .message(e.getMessage())
                .build()
        );
    }

    @ExceptionHandler(SubscriptionException.class)
    public ResponseEntity handleSubscriptionException(RuntimeException e) {
        return ResponseEntity.internalServerError().body(
            Response.builder()
                    .status(FAIL.getStatus())
                    .message(e.getMessage())
                .build()
        );
    }
}
