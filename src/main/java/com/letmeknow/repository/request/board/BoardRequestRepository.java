package com.letmeknow.repository.request.board;

import com.letmeknow.entity.request.BoardRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRequestRepository extends JpaRepository<BoardRequest, Long>, BoardRequestRepositoryQueryDsl {

}
