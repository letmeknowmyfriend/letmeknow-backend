package com.letmeknow.service;

import com.letmeknow.dto.BoardDtoWithSubscription;
import com.letmeknow.entity.Board;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.repository.board.BoardRepository;
import com.letmeknow.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.letmeknow.auth.messages.MemberMessages.MEMBER;
import static com.letmeknow.message.messages.Messages.NOT_EXISTS;
import static com.letmeknow.message.messages.Messages.SUCH;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    // ToDo: 쿼리 최적화 모르겠음
    public List<BoardDtoWithSubscription> findAllByCollegeIdWithSubscription(Long collegeId, String email) throws NoSuchMemberException {
        Long memberId = memberRepository.findNotDeletedIdByEmail(email)
            .orElseThrow(() -> new NoSuchMemberException(new StringBuffer().append(SUCH.getMessage()).append(MEMBER.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        List<Board> boards = boardRepository.findByCollegeIdWithSubscriptionByMemberId(collegeId, memberId);

        List<BoardDtoWithSubscription> boardDtoWithSubscriptions = new ArrayList<>();

        for (Board board : boards) {
            boardDtoWithSubscriptions.add(board.toDtoWithSubscription());
        }

        return boardDtoWithSubscriptions;
    }

    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    @Transactional
    public Board save(Board board) {
        return boardRepository.save(board);
    }
}
