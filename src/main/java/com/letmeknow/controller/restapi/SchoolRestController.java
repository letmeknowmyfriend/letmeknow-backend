package com.letmeknow.controller.restapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.letmeknow.dto.SchoolDto;
import com.letmeknow.dto.Response;
import com.letmeknow.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.letmeknow.enumstorage.response.Status.FAIL;
import static com.letmeknow.enumstorage.response.Status.SUCCESS;

@RestController
@RequestMapping(value = "/api/school", consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class SchoolRestController {
    private final SchoolService schoolService;
    private final ObjectMapper objectMapper;

    // 학교 리스트 조회
    @GetMapping(value = "/list/v1")
    public ResponseEntity list_v1() throws JsonProcessingException {
        List<SchoolDto> schoolDtos = schoolService.findAll();

        System.out.println();

        return ResponseEntity.ok(
            Response.builder()
                .status(SUCCESS.getStatus())
                .data(objectMapper.writeValueAsString(schoolDtos))
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
