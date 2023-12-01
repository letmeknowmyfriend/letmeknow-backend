package com.letmeknow.repository;

import com.letmeknow.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findOneById(long id);
    List<Board> findAll();

}
