package com.letmeknow.entity;

import com.letmeknow.dto.BoardDtoWithSubscription;
import com.letmeknow.entity.notification.Subscription;
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

    @NotBlank
    private String boardUrl;

    @NotNull
    private Boolean isThereNotice;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COLLEGE_ID")
    private College college;

    @NotNull
    @OneToMany(mappedBy = "board")
    private List<Subscription> subscriptions = new ArrayList<>();

    @NotNull
    @OneToMany(mappedBy = "board")
    private List<Article> articles = new ArrayList();

    @Builder
    protected Board(String boardName, String boardUrl, Boolean isThereNotice, College college) {
        this.boardName = boardName;
        this.boardUrl = boardUrl;
        this.isThereNotice = isThereNotice;
        this.college = college;

        college.addBoard(this);
    }

    // 연관 관계 편의 메소드
    public void addArticle(Article article) {
        this.articles.add(article);
    }

    //== DTO ==//
    public BoardDtoWithSubscription toDtoWithSubscription() {
        return BoardDtoWithSubscription.builder()
                .id(this.id)
                .boardName(this.boardName)
                .isThereSubscription(subscriptions.size() > 0)
                .build();
    }
}
