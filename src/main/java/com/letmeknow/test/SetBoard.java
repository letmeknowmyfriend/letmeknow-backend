//package com.letmeknow.test;
//
//import com.letmeknow.entity.Board;
//import com.letmeknow.repository.BoardRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Component
//public class SetBoard {
//    @Autowired
//    private BoardRepository boardRepository;
//
//    @Transactional
//    @EventListener(ApplicationReadyEvent.class) // Application이 실행되고 나서 이 메소드를 실행한다.
//    public void setUp() {
//        Board 일반공지 = Board.builder()
//            .boardName("일반공지")
//            .boardSeq(700l)
//            .menuSeq(5168l)
//            .isThereNotice(true)
//            .build();
//
//        Board 취업_장학 = Board.builder()
//            .boardName("취업/장학")
//            .boardSeq(701l)
//            .menuSeq(5170l)
//            .isThereNotice(false)
//            .build();
//
//        Board 공모_특강 = Board.builder()
//            .boardName("공모/특강")
//            .boardSeq(702l)
//            .menuSeq(5172l)
//            .isThereNotice(false)
//            .build();
//
//        boardRepository.saveAll(
//            List.of(일반공지, 취업_장학, 공모_특강)
//        );
//    }
//}
