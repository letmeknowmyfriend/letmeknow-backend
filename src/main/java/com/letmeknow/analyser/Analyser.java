package com.letmeknow.analyser;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.letmeknow.entity.Article;
import com.letmeknow.entity.Board;
import com.letmeknow.dto.crawling.ArticleCreationDto;
import com.letmeknow.dto.crawling.ArticleDto;
import com.letmeknow.service.ArticleService;
import com.letmeknow.service.BoardService;
import com.letmeknow.service.notification.NotificationService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@NoArgsConstructor
public class Analyser extends QuartzJobBean {
    @Autowired
    private BoardService boardService;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private NotificationService notificationService;

    @Transactional
    @Override
    public void executeInternal(JobExecutionContext context) {
        ////////////////////////////////////////////////////////////////////////////
        // 나중에 어떤 게시판을 크롤링 할지 인자로 받고
        // 보드에서 게시판 번호를 받아오는거로 리팩토링

        List<Board> allBoards = boardService.findAll();

        System.out.println("@@@@@ 크롤링 시작 @@@@@");

        for (Board board : allBoards) {
            try {
//                ////////////////////////////////////////////////////////////////////////////
//                Optional<Board> oneByBoardSeq = boardService.findOneByBoardSeq(boardSeq);
//
//                Board board = null;
//
//                // 게시판이 DB에 없으면
//                if (oneByBoardSeq.isEmpty()) {
//                    Elements pageTit = doc.getElementsByClass("page_tit");
//                    String boardName = pageTit.get(0).getElementsByTag("span").get(0).text();
//
//                    Board newBoard = Board.builder()
//                        .boardName(boardName)
//                        .boardSeq(boardSeq)
//                        .menuSeq()
//                        .isThereNotice(true)
//                        .build();
//
//                    board = boardService.save(newBoard);
//                }
//                // 게시판이 DB에 있으면
//                else {
//                    board = oneByBoardSeq.get();
//                }
//                ////////////////////////////////////////////////////////////////////////////
                // 크롤링
                Connection connection = Jsoup.connect(board.getBoardUrl());
                connection.timeout(100_000); // 100초
                final Response response = connection.execute();
                final Document doc = response.parse();



                List<String> elementIds = new ArrayList();
                elementIds.add("dispList");

                // 공지가 있는 게시판이면
                if (board.getIsThereNotice()) {
                    // 공지사항도 크롤링
                    elementIds.add("noticeList");
                }

                List<Article> whatToSave = new ArrayList();

                // 푸시 알림 리스트
                List<Message> messages = new ArrayList();

                // 크롤링 할 대상에 대해
                for (int noticeIndex = 0; noticeIndex < elementIds.size(); noticeIndex++) {
                    List<Article> crawledArticles = new ArrayList();

                    // tag가 tbody이고 id가 dispList인 태그의 자식 태그들을 모두 가져옴
                    Element list = doc.getElementById(elementIds.get(noticeIndex));

                    // 그 태그들 중에서 tag가 tr인 태그들을 모두 가져옴
                    List<Element> trs = list.getElementsByTag("tr");

                    for (int i = 0; i < trs.size(); i++) {
                        // 그 태그들 중에서 tag가 td인 태그들을 모두 가져옴
                        List<Element> tds = trs.get(i).getElementsByTag("td");

                        // 제목
                        Element td = tds.get(1);
                        Node a = td.childNode(1);
                        String title = a.childNode(0).toString().trim();

                        // 링크
                        long link = Long.parseLong(a.attr("data-itsp-view-link"));

                        // 작성 일자
                        String date = tds.get(3).text();

                        // 그리고 그 태그들을 모두 저장
                        crawledArticles.add(Article.builder()
                            .board(board)
                            .title(title)
                            .link(link)
                            .createdAt(date)
                            .isNotice(noticeIndex == 1)
                            .build());

                        // DB에 일반 공지를 모두 저장

                        // 그리고 그 태그들을 모두 저장

                        // DB에 일반 공지를 모두 저장
                    }

                    List<ArticleDto> dbArticles = articleService.findAllByBoardIdAndIsNoticeOrderByIdDescLimit(board.getId(), 60L, noticeIndex == 1);

                    findWhereToStartAndSaveIntoWhatToSave(dbArticles, crawledArticles, whatToSave, messages, board.getId());
                }

                articleService.saveAllArticles(whatToSave);

                // 새로 들어온게 있으면
                if (!whatToSave.isEmpty()) {
                    // notification을 보내고, 푸시 알림을 보낸다.
                    notificationService.saveAndSendNotifications(board, whatToSave);
                }
            } catch (IOException e) {
                log.warn(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    private void findWhereToStartAndSaveIntoWhatToSave(List<ArticleDto> dbArticles, List<Article> crawledArticles, List<Article> whatToSave, List<Message> messages, long boardId) {
        // 어디서부터 넣어야할지 비교하는 로직
        int crawledArticleIndex = -1;

        boolean isSuccess = false;
        for (int i = 0; i < dbArticles.size(); i++) {
            boolean sameArticleFound = false;

            for (int j = 0; j < crawledArticles.size(); j++) {
                if (dbArticles.get(i).getTitle().equals(crawledArticles.get(j).getTitle())) {
                    crawledArticleIndex = j - 1;

                    sameArticleFound = true;

                    break;
                }
            }

            // 찾았으면
            if (sameArticleFound) {
                isSuccess = true;
                break;
            }
        }

        int index;
        if (isSuccess) {
            // tempArticles에서 필요한거 넣어주는 로직
            index = crawledArticleIndex;
        }
        else {
            // 60개동안 겹치는게 하나도 없는거니까 다 넣어주자.
            index = crawledArticles.size() - 1;
        }

        for (int i = index; i >= 0; i--) {
            whatToSave.add(crawledArticles.get(i));

            // 푸시 알림 리스트에 추가
            messages.add(Message.builder()
                    .setTopic(String.valueOf(boardId))
                    .putData("body", crawledArticles.get(i).getTitle())
                .build());
        }
    }
}
