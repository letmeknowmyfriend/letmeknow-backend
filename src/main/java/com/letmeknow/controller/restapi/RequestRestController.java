package com.letmeknow.controller.restapi;

import com.letmeknow.dto.Response;
import com.letmeknow.form.BoardRequestForm;
import com.letmeknow.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.letmeknow.enumstorage.response.Status.SUCCESS;
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
}
