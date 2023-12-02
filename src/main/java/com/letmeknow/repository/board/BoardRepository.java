package com.letmeknow.repository.board;

import com.letmeknow.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface BoardRepository extends JpaRepository<Board, Long> {
    @Query(value = "SELECT b.*, subscription.*\n" +
        "FROM Board b\n" +
        "LEFT JOIN subscription ON b.board_id = subscription.board_id\n" +
        "AND subscription.member_id = :memberId\n" +
        "WHERE b.college_id = :collegeId",
    nativeQuery = true)
    List<Board> findByCollegeIdWithSubscriptionByMemberId(@Param("collegeId") Long collegeId, @Param("memberId") Long memberId);
    List<Board> findAll();
}
