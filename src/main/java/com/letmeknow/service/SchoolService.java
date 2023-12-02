package com.letmeknow.service;

import com.letmeknow.dto.SchoolDto;
import com.letmeknow.dto.SchoolDtoWithCollegeDto;
import com.letmeknow.entity.School;
import com.letmeknow.repository.school.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchoolService {
    private final SchoolRepository schoolRepository;

    public List<SchoolDto> findAll() {
        return schoolRepository.findAll().stream()
            .map(School::toDto)
            .collect(Collectors.toList());
    }

    public List<SchoolDtoWithCollegeDto> findWithCollege() {
        return schoolRepository.findWithCollege().stream()
            .map(School::toDtoWithCollegeDto)
            .collect(Collectors.toList());
    }
}
