package com.letmeknow.entity;

import com.letmeknow.entity.notification.Notification;
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
    private String boardCrawlingUrl;

    @NotBlank
    private String boardViewUrl;

    @NotNull
    private Boolean isThereNotice;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COLLEGE_ID")
    private College college;

    @NotNull
    @OneToMany(mappedBy = "board")
    private List<Notification> notifications = new ArrayList<>();

    @NotNull
    @OneToMany(mappedBy = "board")
    private List<Subscription> subscriptions = new ArrayList<>();

    @NotNull
    @OneToMany(mappedBy = "board")
    private List<Article> articles = new ArrayList();

    @Builder
    protected Board(String boardName, String boardCrawlingUrl, String boardViewUrl, Boolean isThereNotice, College college) {
        this.boardName = boardName;
        this.boardCrawlingUrl = boardCrawlingUrl;
        this.boardViewUrl = boardViewUrl;
        this.isThereNotice = isThereNotice;
        this.college = college;

        college.addBoard(this);
    }
}
