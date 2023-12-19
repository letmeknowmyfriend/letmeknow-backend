package com.letmeknow.repository.board;

import com.letmeknow.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface BoardRepository extends JpaRepository<Board, Long> {
    @Query(
        value = "SELECT b.BOARD_ID as id, b.board_name as boardName, b.board_view_url as boardViewUrl, IF(COUNT(s.SUBSCRIPTION_ID) > 0, 'TRUE', 'FALSE') as isSubscribed\n" +
        "FROM Board b\n" +
        "LEFT JOIN subscription as s ON b.board_id = s.board_id\n" +
        "AND s.member_id = :memberId\n" +
        "WHERE b.college_id = :collegeId\n" +
        "GROUP BY b.board_id\n",
        nativeQuery = true)
    List<BoardInterface> findByCollegeIdWithSubscriptionByMemberId(@Param("collegeId") Long collegeId, @Param("memberId") Long memberId);
    List<Board> findAll();
}
