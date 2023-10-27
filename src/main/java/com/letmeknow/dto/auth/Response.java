package com.letmeknow.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class Response {
    @NotBlank
    private String status;
    @NotBlank
    private String command;
    @NotBlank
    private String reason;

    @Builder
    protected Response(String status, String command, String reason) {
        this.status = status;
        this.command = command;
        this.reason = reason;
    }
}
