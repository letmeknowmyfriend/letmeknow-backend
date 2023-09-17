package com.letmeknow.quartz.analyser;

import com.letmeknow.domain.BoardNumber;
import com.letmeknow.dto.crawling.ArticleCreationDto;
import com.letmeknow.dto.crawling.ArticleDto;
import com.letmeknow.service.ArticleService;
import com.letmeknow.service.BoardNumberService;
import lombok.NoArgsConstructor;
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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@NoArgsConstructor
public class Analyser extends QuartzJobBean {
    @Autowired
    private BoardNumberService boardNumberService;
    @Autowired
    private ArticleService articleService;

    @Override
    public void executeInternal(JobExecutionContext context) {
        ////////////////////////////////////////////////////////////////////////////
        // 나중에 어떤 게시판을 크롤링 할지 인자로 받고
        // 보드에서 게시판 번호를 받아오는거로 리팩토링

        final Long boardSeq = 700L;

        ////////////////////////////////////////////////////////////////////////////

        Optional<BoardNumber> oneByBoardSeq = boardNumberService.findOneByBoardSeq(boardSeq);

        BoardNumber boardNumber = null;
        if (oneByBoardSeq.isEmpty()) {
            BoardNumber generalNotice = BoardNumber.builder()
                    .boardName("일반 공지")
                    .boardSeq(boardSeq)
                    .isThereNotice(true)
                    .build();

            boardNumber = boardNumberService.save(generalNotice);
        }
        else {
            boardNumber = oneByBoardSeq.get();
        }
        ////////////////////////////////////////////////////////////////////////////
        try {
            Connection connection = Jsoup.connect("https://caku.konkuk.ac.kr/noticeList.do?siteId=CAKU&boardSeq=" + boardSeq + "&menuSeq=5168&curBoardDispType=LIST&curPage=60&pageNum=1");
            connection.timeout(100000);
            final Response response = connection.execute();
            System.out.println("#########################Execute##############################");

            final Document doc = response.parse();

            List<String> elementIds = new ArrayList();
            elementIds.add("dispList");

            // 공지가 있는 게시판이면
            if (boardNumber.getIsThereNotice()) {
                elementIds.add("noticeList");
            }

            List<ArticleCreationDto> crawledArticles = null;
            List<ArticleCreationDto> whatToSave = new ArrayList();

            for (int noticeIndex = 0; noticeIndex < elementIds.size(); noticeIndex++) {
                crawledArticles = new ArrayList();

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
                    Long link = Long.parseLong(a.attr("data-itsp-view-link"));

                    // 작성 일자
                    String date = tds.get(3).text();

                    // 그리고 그 태그들을 모두 저장
                    crawledArticles.add(ArticleCreationDto.builder()
                            .boardId(boardNumber.getId())
                            .title(title)
                            .link(link)
                            .createdAt(date)
                            .isNotice(noticeIndex == 1)
                        .build());

                    // DB에 일반 공지를 모두 저장

                    // 그리고 그 태그들을 모두 저장

                    // DB에 일반 공지를 모두 저장
                }

                List<ArticleDto> dbArticles = articleService.findAllByBoardIdAndIsNoticeOrderByIdDescLimit(boardNumber.getId(), 60L, noticeIndex == 1);

                whatToSave = findWhereToStart(dbArticles, crawledArticles, whatToSave);
            }

            articleService.saveAllArticles(whatToSave);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ArticleCreationDto> findWhereToStart(List<ArticleDto> dbArticles, List<ArticleCreationDto> crawledArticles, List<ArticleCreationDto> whatToSave) {
        // 어디서부터 넣어야할지 비교하는 로직
        int crawledArticleIndex = 0;

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

        if (isSuccess) {
            // tempArticles에서 필요한거 넣어주는 로직
            for (int i = crawledArticleIndex; i >= 0; i--) {
                whatToSave.add(crawledArticles.get(i));
            }
        }
        // 60개동안 겹치는게 하나도 없는거니까 다 넣어주자.
        else {
            for (int i = crawledArticles.size() - 1; i >= 0 ; i--) {
                whatToSave.add(crawledArticles.get(i));
            }
        }

        return whatToSave;
    }
}
