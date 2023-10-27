package com.letmeknow.controller.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letmeknow.dto.auth.Response;
import com.letmeknow.enumstorage.errormessage.RequestArgumentErrorMessage;
import com.letmeknow.enumstorage.errormessage.notification.SubscriptionErrorMessage;
import com.letmeknow.exception.notification.NotificationException;
import com.letmeknow.enumstorage.response.Status;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.message.MessageMaker;
import com.letmeknow.service.auth.jwt.JwtService;
import com.letmeknow.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.impl.InvalidContentTypeException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.letmeknow.message.reason.HttpReason.CONTENT_TYPE;
import static com.letmeknow.message.reason.HttpReason.NOT_JSON;
import static com.letmeknow.message.reason.MemberReason.MEMBER;
import static com.letmeknow.message.reason.NotificationReason.FCM;
import static com.letmeknow.message.Message.NOT_EXISTS;
import static com.letmeknow.message.reason.SubscriptionReason.*;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class RestSubscriptionController {
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final JwtService jwtService;
    private final MessageMaker messageMaker;

    @PostMapping("/subscribe/v1")
    public Response subscribe(HttpServletRequest request, HttpServletResponse response, @RequestParam String boardId) throws IOException {
        try {
            if (!request.getContentType().equals("application/json")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new InvalidContentTypeException(RequestArgumentErrorMessage.CONTENT_TYPE_IS_NOT_JSON.getMessage());
            }

            // boardId가 없으면
            if (boardId.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new IllegalArgumentException(SubscriptionErrorMessage.NO_BOARD_ID.getMessage());
            }

            String email = (String) request.getAttribute("email");

//            PrincipalUserDetails principalUserDetails = (PrincipalUserDetails) authentication.getDetails();
//            Member member = principalUserDetails.getMember();

            boolean subscribeResult = notificationService.subscribe(email, Long.valueOf(boardId));

            // 구독 성공 시
            if (subscribeResult) {
                return Response.builder()
                    .status(Status.SUCCESS)
                    .command("")
                    .reason(messageMaker.add(FCM).add(SUBSCRIPTION).add(SUCCESS).toString())
                .build();
            }
            // 구독 실패 시
            else {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);

                return Response.builder()
                    .status(Status.FAIL)
                    .command("")
                    .reason(messageMaker.add(FCM).add(SUBSCRIPTION).add(FAIL).toString())
                    .build();
            }
        }
        catch (InvalidContentTypeException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            return Response.builder()
                .status(Status.FAIL)
                .command("")
                .reason(messageMaker.add(CONTENT_TYPE).add(NOT_JSON).toString())
                .build();
        }
        catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            return Response.builder()
                .status(Status.FAIL)
                .command("")
                .reason(messageMaker.add(BOARD_ID).add(EMPTY).toString())
                .build();
        }
        catch (NoSuchMemberException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            return Response.builder()
                .status(Status.FAIL)
                .command("")
                .reason(messageMaker.add(MEMBER).add(NOT_EXISTS).toString())
                .build();
        }
        // 구독 실패 시
        catch (NotificationException e) {
            return Response.builder()
                        .status(Status.FAIL)
                        .command("")
                        .reason(e.getMessage())
                .build();
        }
    }
}
