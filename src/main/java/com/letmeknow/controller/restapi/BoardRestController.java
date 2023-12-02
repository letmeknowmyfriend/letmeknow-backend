package com.letmeknow.controller.restapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.letmeknow.dto.BoardDtoWithSubscription;
import com.letmeknow.dto.Response;
import com.letmeknow.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.letmeknow.enumstorage.response.Status.FAIL;
import static com.letmeknow.enumstorage.response.Status.SUCCESS;

@RestController
@RequestMapping(value = "/api/board", consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class BoardRestController {
    private final BoardService boardService;
    private final ObjectMapper objectMapper;

    // 게시판 리스트 조회
    @GetMapping(value = "/v1")
    public ResponseEntity list_v1(@RequestParam Long collegeId, HttpServletRequest request) throws JsonProcessingException {
        String email = request.getAttribute("email").toString();

        List<BoardDtoWithSubscription> board = boardService.findAllByCollegeIdWithSubscription(collegeId, email);

        return ResponseEntity.ok(
            Response.builder()
                .status(SUCCESS.getStatus())
                .data(objectMapper.writeValueAsString(board))
            .build()
        );
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity handleException(Exception e) {
        return ResponseEntity.internalServerError().body(
            Response.builder()
                .status(FAIL.getStatus())
                .message(e.getMessage())
                .build()
        );
    }
}
