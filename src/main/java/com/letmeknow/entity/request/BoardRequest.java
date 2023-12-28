package com.letmeknow.entity.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class BoardRequest {
    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "BOARD_REQUEST_ID")
    private Long id;

    @NotBlank
    private String schoolName;

    @NotNull
    private String branchName;

    @NotBlank
    private String collegeName;

    @NotBlank
    private String boardName;

    @NotBlank String requestEmail;

    @Builder
    protected BoardRequest(String schoolName, String branchName, String collegeName, String boardName, String requestEmail) {
        this.schoolName = schoolName;
        this.branchName = branchName;
        this.collegeName = collegeName;
        this.boardName = boardName;
        this.requestEmail = requestEmail;
    }
}
