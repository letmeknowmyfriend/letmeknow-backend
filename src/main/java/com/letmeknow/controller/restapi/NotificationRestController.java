package com.letmeknow.controller.restapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.letmeknow.dto.NotificationDtoWithBoardViewUrlAndArticleDto;
import com.letmeknow.dto.Response;
import com.letmeknow.exception.auth.jwt.NoSuchDeviceTokenException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.List;

import static com.letmeknow.enumstorage.response.Status.FAIL;
import static com.letmeknow.enumstorage.response.Status.SUCCESS;
import static javax.servlet.http.HttpServletResponse.SC_GONE;

@RestController
@RequestMapping(value = "/api/notification", consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class NotificationRestController {
    private final NotificationService notificationService;

    private final ObjectMapper objectMapper;

    // 알림 목록 조회
    @GetMapping(value = "/list/v1")
    public ResponseEntity list_v1(@RequestParam(required = false) Long lastId, @RequestParam(required = false) Long pageSize, HttpServletRequest request) throws JsonProcessingException {
        String email = (String) request.getAttribute("email");

        List<NotificationDtoWithBoardViewUrlAndArticleDto> notificationDtoWithBoardViewUrlAndArticleDtos = notificationService.findByNoOffset(lastId, pageSize, email);

        return ResponseEntity.ok(
            Response.builder()
                .status(SUCCESS.getStatus())
                .data(objectMapper.writeValueAsString(notificationDtoWithBoardViewUrlAndArticleDtos))
            .build()
        );
    }

    // 알릶 검색
    @GetMapping(value = "/search/v1")
    public ResponseEntity search_v1(@RequestParam(required = false) String keyword, @RequestParam(required = false) Long lastId, @RequestParam(required = false) Long pageSize, HttpServletRequest request) throws JsonProcessingException {
        String email = (String) request.getAttribute("email");

        List<NotificationDtoWithBoardViewUrlAndArticleDto> byNoOffsetWithKeyword = notificationService.findByNoOffsetWithKeyword(keyword, lastId, pageSize, email);

        return ResponseEntity.ok(
            Response.builder()
                .status(SUCCESS.getStatus())
                .data(objectMapper.writeValueAsString(byNoOffsetWithKeyword))
            .build()
        );
    }

    // 알림 읽음 처리
    @PostMapping(value = "/read/v1")
    public ResponseEntity read_v1(@RequestParam Long notificationId, HttpServletRequest request) throws NoSuchMemberException {
        String email = (String) request.getAttribute("email");

        notificationService.readNotification(notificationId, email);

        return ResponseEntity.ok(
            Response.builder()
                .status(SUCCESS.getStatus())
            .build()
        );
    }

    // 알림 삭제
    @DeleteMapping(value = "/delete/v1")
    public ResponseEntity delete_v1(@RequestParam Long notificationId, HttpServletRequest request) throws NoSuchMemberException {
        String email = (String) request.getAttribute("email");

        notificationService.deleteNotification(notificationId, email);

        return ResponseEntity.ok(
            Response.builder()
                .status(SUCCESS.getStatus())
            .build()
        );
    }

    @ExceptionHandler({IllegalArgumentException.class, NoSuchDeviceTokenException.class, ConstraintViolationException.class})
    public ResponseEntity handleException(Exception e) {
        return ResponseEntity.badRequest().body(
            Response.builder()
                .status(FAIL.getStatus())
                .message(e.getMessage())
            .build()
        );
    }

    @ExceptionHandler(NoSuchMemberException.class)
    public ResponseEntity handle410Exception(RuntimeException e) {
        return ResponseEntity.status(SC_GONE).body(
            Response.builder()
                .status(FAIL.getStatus())
                .message(e.getMessage())
            .build()
        );
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity jsonProcessingException(Exception e) {
        return ResponseEntity.internalServerError().body(
            Response.builder()
                .status(FAIL.getStatus())
                .message(e.getMessage())
            .build()
        );
    }
}
