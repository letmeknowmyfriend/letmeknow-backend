package com.letmeknow.service;

import com.letmeknow.domain.BoardNumber;
import com.letmeknow.repository.BoardNumberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardNumberService {
    private final BoardNumberRepository boardNumberRepository;

    public List<BoardNumber> findAll() {
        return boardNumberRepository.findAll();
    }

    public Optional<BoardNumber> findOneByBoardSeq(Long boardSeq) {
        return boardNumberRepository.findOneByBoardSeq(boardSeq);
    }

    @Transactional
    public BoardNumber save(BoardNumber boardNumber) {
        return boardNumberRepository.save(boardNumber);
    }
}
