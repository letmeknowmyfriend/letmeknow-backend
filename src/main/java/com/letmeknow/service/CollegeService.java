package com.letmeknow.service;

import com.letmeknow.dto.CollegeDto;
import com.letmeknow.entity.College;
import com.letmeknow.repository.college.CollegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollegeService {
    private final CollegeRepository collegeRepository;

    public List<CollegeDto> findBySchoolId(Long schoolId) {
        return collegeRepository.findBySchoolId(schoolId).stream()
            .map(College::toDto)
            .collect(Collectors.toList());
    }
}
