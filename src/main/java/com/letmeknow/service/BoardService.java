package com.letmeknow.service;

import com.letmeknow.dto.BoardDtoWithSubscription;
import com.letmeknow.entity.Board;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.repository.board.BoardInterface;
import com.letmeknow.repository.board.BoardRepository;
import com.letmeknow.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

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

    public List<BoardDtoWithSubscription> findAllByCollegeIdWithSubscription(Long collegeId, String email) throws NoSuchMemberException {
        Long memberId = memberRepository.findNotDeletedIdByEmail(email)
            .orElseThrow(() -> new NoSuchMemberException(new StringBuffer().append(SUCH.getMessage()).append(MEMBER.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        return boardRepository.findByCollegeIdWithSubscriptionByMemberId(collegeId, memberId).stream()
            .map(boardInterface -> BoardDtoWithSubscription.builder()
                .id(boardInterface.getId())
                .boardName(boardInterface.getBoardName())
                .boardUrl(boardInterface.getBoardUrl())
                .isSubscribed(boardInterface.getIsSubscribed())
                .build())
            .collect(Collectors.toList());
    }

    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    @Transactional
    public Board save(Board board) {
        return boardRepository.save(board);
    }
}
