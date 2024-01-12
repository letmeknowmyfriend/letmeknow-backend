package com.letmeknow.controller.restapi;

import com.letmeknow.dto.Response;
import com.letmeknow.form.BoardRequestForm;
import com.letmeknow.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;

import static com.letmeknow.enumstorage.response.Status.FAIL;
import static com.letmeknow.enumstorage.response.Status.SUCCESS;
import static com.letmeknow.message.messages.Messages.INVALID;
import static com.letmeknow.message.messages.Messages.REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/request", consumes = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class RequestRestController {
    private final RequestService requestService;

    @PostMapping("/board/v1")
    public ResponseEntity requestBoard_v1(@RequestBody @Valid BoardRequestForm boardRequestForm, HttpServletRequest request) {
        String email = request.getAttribute("email").toString();

        requestService.saveBoardRequest(boardRequestForm, email);

        return ResponseEntity.ok(
            Response.builder()
                .status(SUCCESS.getStatus())
            .build()
        );
    }

    // 필요한 인자가 없으면
    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    public ResponseEntity handle400Exception(Exception e) {
        // 400 Bad Request
        return ResponseEntity.badRequest()
            .body(Response.builder()
                .status(FAIL.getStatus())
                .message(REQUEST.getMessage() + INVALID.getMessage())
                .build());
    }
}
