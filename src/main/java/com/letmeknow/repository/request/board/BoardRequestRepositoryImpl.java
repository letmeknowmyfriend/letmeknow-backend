package com.letmeknow.repository.request.board;

import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;

@RequiredArgsConstructor
public class BoardRequestRepositoryImpl implements BoardRequestRepositoryQueryDsl {
    private final EntityManager em;
}
