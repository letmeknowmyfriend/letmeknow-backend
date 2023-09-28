package com.letmeknow.repository;

import com.letmeknow.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findOneByBoardSeq(Long boardSeq);
    Optional<Board> findOneById(Long id);
    List<Board> findAll();

}
