package com.letmeknow.controller.restapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.letmeknow.dto.Response;
import com.letmeknow.dto.crawling.ArticleDto;
import com.letmeknow.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

import static com.letmeknow.enumstorage.response.Status.FAIL;
import static com.letmeknow.enumstorage.response.Status.SUCCESS;

@RestController
@RequestMapping(value = "/api/article", consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ArticleRestController {
    private final ArticleService articleService;
    private final ObjectMapper objectMapper;

    // 게시글 리스트 조회
    @GetMapping(value = "/v1")
    public ResponseEntity list_v1(@RequestParam Long boardId, @RequestParam(required = false) Long lastId, @RequestParam(required = false) Long pageSize, HttpServletRequest request) throws JsonProcessingException {
        String email = request.getAttribute("email").toString();

        List<ArticleDto> articleDtos = articleService.findByNoOffset(boardId, lastId, pageSize);

        return ResponseEntity.ok(
            Response.builder()
                .status(SUCCESS.getStatus())
                .data(objectMapper.writeValueAsString(articleDtos))
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

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity handleConstraintViolationException(ConstraintViolationException e) {
        return ResponseEntity.badRequest().body(
            Response.builder()
                .status(FAIL.getStatus())
                .message(e.getMessage())
                .build()
        );
    }
}
