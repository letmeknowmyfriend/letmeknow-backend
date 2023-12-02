package com.letmeknow.entity;

import com.letmeknow.dto.SchoolDto;
import com.letmeknow.dto.SchoolDtoWithCollegeDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class School extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCHOOL_ID")
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String schoolName;

    @NotNull
    private String branchName;

    @NotNull
    @OneToMany(mappedBy = "school")
    private Set<College> colleges = new HashSet<>();

    @Builder
    protected School(String schoolName, String branchName) {
        this.schoolName = schoolName;
        this.branchName = branchName;
    }

    //-- 연관관계 편의 메서드 --//
    public void addCollege(College college) {
        this.colleges.add(college);
    }

    //-- DTO 생성 메서드 --//
    public SchoolDto toDto() {
        return SchoolDto.builder()
            .id(this.id)
            .schoolName(this.schoolName)
            .branchName(this.branchName)
            .build();
    }

    public SchoolDtoWithCollegeDto toDtoWithCollegeDto() {
        return SchoolDtoWithCollegeDto.builder()
            .id(this.id)
            .schoolName(this.schoolName)
            .branchName(this.branchName)
            .colleges(colleges.stream()
                .map(College::toDto)
                .collect(Collectors.toList()))
            .build();
    }
}
