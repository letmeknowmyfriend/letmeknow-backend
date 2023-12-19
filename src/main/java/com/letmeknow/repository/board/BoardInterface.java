package com.letmeknow.repository.board;

public interface BoardInterface {
    Long getId();
    String getBoardName();
    String getBoardViewUrl();
    Boolean getIsSubscribed();
}
