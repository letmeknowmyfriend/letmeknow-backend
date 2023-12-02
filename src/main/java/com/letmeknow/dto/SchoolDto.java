package com.letmeknow.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED, force = true)
public class SchoolDto {
    private final Long id;
    private final String schoolName;
    private final String branchName;

    @Builder
    protected SchoolDto(Long id, String schoolName, String branchName) {
        this.id = id;
        this.schoolName = schoolName;
        this.branchName = branchName;
    }
}
