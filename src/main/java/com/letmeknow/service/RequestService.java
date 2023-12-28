package com.letmeknow.service;

import com.letmeknow.entity.request.BoardRequest;
import com.letmeknow.form.BoardRequestForm;
import com.letmeknow.repository.request.board.BoardRequestRepository;
import com.letmeknow.service.email.EmailService;
import com.letmeknow.util.email.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RequestService {
    private final BoardRequestRepository boardRequestRepository;

    private final EmailService emailService;

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

        // 나한테 메일 보내기
        try {
            emailService.sendMail(Email.builder()
                .subject("게시판 신청")
                .receiver("cha3088@gmail.com")
                .message(new StringBuffer()
                    .append("학교 이름: ").append(boardRequestForm.getSchoolName()).append("\n")
                    .append("학과 이름: ").append(boardRequestForm.getBranchName()).append("\n")
                    .append("대학 이름: ").append(boardRequestForm.getCollegeName()).append("\n")
                    .append("게시판 이름: ").append(boardRequestForm.getBoardName()).append("\n")
                    .append("신청자 이메일: ").append(email).append("\n")
                    .toString())
                .build()
            );
        }
        catch (Exception e) {
            log.info("게시판 요청 이메일 전송 실패");
            e.printStackTrace();
        }
    }
}
