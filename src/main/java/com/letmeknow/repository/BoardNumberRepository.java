package com.letmeknow.repository;

import com.letmeknow.domain.BoardNumber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface BoardNumberRepository extends JpaRepository<BoardNumber, Long> {
    Optional<BoardNumber> findOneByBoardSeq(Long boardSeq);
    Optional<BoardNumber> findOneById(Long id);
    List<BoardNumber> findAll();

}
