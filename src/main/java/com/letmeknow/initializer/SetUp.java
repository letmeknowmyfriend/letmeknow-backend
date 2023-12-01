package com.letmeknow.initializer;

import com.letmeknow.entity.Board;
import com.letmeknow.entity.College;
import com.letmeknow.entity.School;
import com.letmeknow.repository.BoardRepository;
import com.letmeknow.repository.CollegeRepository;
import com.letmeknow.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SetUp {
    private final SchoolRepository schoolRepository;
    private final CollegeRepository collegeRepository;
    private final BoardRepository boardRepository;

    @Transactional
    @EventListener(ApplicationReadyEvent.class) // Application이 실행되고 나서 이 메소드를 실행한다.
    public void setUp() {
        // 건국대학교
        School school = School.builder()
            .schoolName("건국대학교")
            .branchName("서울캠퍼스")
            .build();

        schoolRepository.save(school);

        College college = College.builder()
            .collegeName("건축대학")
            .school(school)
            .build();

        collegeRepository.save(college);

        Board 일반공지 = Board.builder()
            .boardName("일반공지")
            .boardUrl("https://caku.konkuk.ac.kr/noticeList.do?siteId=CAKU&boardSeq=700&menuSeq=5168&searchBy=&searchValue=&categorySeq=0&curBoardDispType=LIST&curPage=60&pageNum=1")
            .isThereNotice(true)
            .college(college)
            .build();

        Board 취업_장학 = Board.builder()
            .boardName("취업/장학")
            .boardUrl("https://caku.konkuk.ac.kr/noticeList.do?siteId=CAKU&boardSeq=701&menuSeq=5170&searchBy=&searchValue=&categorySeq=0&curBoardDispType=LIST&curPage=60&pageNum=1")
            .isThereNotice(false)
            .college(college)
            .build();

        Board 공모_특강 = Board.builder()
            .boardName("공모/특강")
            .boardUrl("https://caku.konkuk.ac.kr/noticeList.do?siteId=CAKU&boardSeq=702&menuSeq=5172&searchBy=&searchValue=&categorySeq=0&curBoardDispType=LIST&curPage=60&pageNum=1")
            .isThereNotice(false)
            .college(college)
            .build();

        Board 학사_규정 = Board.builder()
            .boardName("학사규정")
            .boardUrl("https://caku.konkuk.ac.kr/noticeList.do?siteId=CAKU&boardSeq=703&menuSeq=5174&searchBy=&searchValue=&categorySeq=0&curBoardDispType=LIST&curPage=60&pageNum=1")
            .isThereNotice(true)
            .college(college)
            .build();

        boardRepository.saveAll(
            List.of(일반공지, 취업_장학, 공모_특강, 학사_규정)
        );
    }
}
