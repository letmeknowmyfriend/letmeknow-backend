package com.letmeknow.controller.restapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.letmeknow.dto.CollegeDto;
import com.letmeknow.dto.Response;
import com.letmeknow.service.CollegeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.letmeknow.enumstorage.response.Status.FAIL;
import static com.letmeknow.enumstorage.response.Status.SUCCESS;

@RestController
@RequestMapping(value = "/api/college", consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CollegeRestController {
    private final CollegeService collegeService;
    private final ObjectMapper objectMapper;

    // 단과대 리스트 조회
    @GetMapping(value = "/v1")
    public ResponseEntity list_v1(@RequestParam Long schoolId) throws JsonProcessingException {
        List<CollegeDto> collegeDtos = collegeService.findBySchoolId(schoolId);

        return ResponseEntity.ok(
            Response.builder()
                .status(SUCCESS.getStatus())
                .data(objectMapper.writeValueAsString(collegeDtos))
            .build()
        );
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity exceptionHandler(Exception e) {
        return ResponseEntity.internalServerError().body(
            Response.builder()
                .status(FAIL.getStatus())
                .message(e.getMessage())
            .build()
        );
    }
}
