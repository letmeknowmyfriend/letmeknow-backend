package com.letmeknow.domain;

import com.letmeknow.domain.notification.Subscription;
import com.letmeknow.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOARD_ID")
    private Long id;

    @NotBlank
    private String boardName;

    @NotNull
    private Long boardSeq;

    @NotNull
    private Boolean isThereNotice;

    @NotNull
    @OneToMany(mappedBy = "board")
    private List<Subscription> subscriptions = new ArrayList<>();

    @NotNull
    @OneToMany(mappedBy = "boardNumber")
    private List<Article> articles = new ArrayList();

    @Builder
    protected Board(String boardName, Long boardSeq, Boolean isThereNotice) {
        this.boardName = boardName;
        this.boardSeq = boardSeq;
        this.isThereNotice = isThereNotice;
    }

    // 연관 관계 편의 메소드
    public void addArticle(Article article) {
        this.articles.add(article);
    }
}
