package com.example.monghyang.domain.tag.service;

import com.example.monghyang.domain.tag.repository.TagsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TagsService {
    private final TagsRepository tagsRepository;
    @Autowired
    public TagsService(TagsRepository tagsRepository) {
        this.tagsRepository = tagsRepository;
    }

    // 태그 생성

    // 태그 이름 수정

    // 태그 삭제 처리

    // 태그 복구

    // 태그 최신순 조회

    // 태그 키워드 조회
}
