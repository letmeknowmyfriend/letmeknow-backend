package com.letmeknow.service;

import com.letmeknow.entity.Board;
import com.letmeknow.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    public Optional<Board> findOneByBoardSeq(long boardSeq) {
        return boardRepository.findOneByBoardSeq(boardSeq);
    }

    @Transactional
    public Board save(Board boardNumber) {
        return boardRepository.save(boardNumber);
    }
}
