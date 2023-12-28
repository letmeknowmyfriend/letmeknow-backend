package com.letmeknow.service;

import com.letmeknow.entity.request.BoardRequest;
import com.letmeknow.form.BoardRequestForm;
import com.letmeknow.repository.request.board.BoardRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestService {
    private final BoardRequestRepository boardRequestRepository;

    @Transactional
    public void saveBoardRequest(BoardRequestForm boardRequestForm, String email) {
        boardRequestRepository.save(BoardRequest.builder()
            .schoolName(boardRequestForm.getSchoolName())
            .branchName(boardRequestForm.getBranchName())
            .collegeName(boardRequestForm.getCollegeName())
            .boardName(boardRequestForm.getBoardName())
            .requestEmail(email)
            .build()
        );
    }
}
