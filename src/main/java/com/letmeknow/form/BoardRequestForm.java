package com.letmeknow.form;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED, force = true)
public class BoardRequestForm {
    @NotBlank
    private final String schoolName;

    @NotNull
    private final String branchName;

    @NotBlank
    private final String collegeName;

    @NotBlank
    private final String boardName;

    @Builder
    protected BoardRequestForm(String schoolName, String branchName, String collegeName, String boardName) {
        this.schoolName = schoolName;
        this.branchName = branchName;
        this.collegeName = collegeName;
        this.boardName = boardName;
    }
}
