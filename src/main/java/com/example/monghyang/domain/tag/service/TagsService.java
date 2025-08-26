package com.example.monghyang.domain.tag.service;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.tag.dto.ResTagDto;
import com.example.monghyang.domain.tag.entity.TagCategory;
import com.example.monghyang.domain.tag.entity.Tags;
import com.example.monghyang.domain.tag.repository.TagCategoryRepository;
import com.example.monghyang.domain.tag.repository.TagsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TagsService {
    private final TagsRepository tagsRepository;
    private final TagCategoryRepository tagCategoryRepository;
    private final Integer tagPageSize = 10;

    @Autowired
    public TagsService(TagsRepository tagsRepository, TagCategoryRepository tagCategoryRepository) {
        this.tagsRepository = tagsRepository;
        this.tagCategoryRepository = tagCategoryRepository;
    }

    // 태그 생성
    public void createTag(Integer tagCategoryId, String tagName) throws DataIntegrityViolationException {
        TagCategory tagCategory = tagCategoryRepository.findById(tagCategoryId).orElseThrow(() ->
                new ApplicationException(ApplicationError.TAG_CATEGORY_NOT_FOUND));

        tagsRepository.save(Tags.categoryNameOf(tagCategory, tagName));
    }


    // 태그 이름 수정
    public void updateTag(Integer tagId, String tagName) throws DataIntegrityViolationException {
        Tags tag = tagsRepository.findById(tagId).orElseThrow(() ->
                new ApplicationException(ApplicationError.TAG_NOT_FOUND));
        tag.updateName(tagName);
        tagsRepository.save(tag);
    }

    // 태그 삭제 처리
    public void deleteTag(Integer tagId) {
        Tags tag = tagsRepository.findById(tagId).orElseThrow(() ->
                new ApplicationException(ApplicationError.TAG_NOT_FOUND));
        tag.setDeleted();
        tagsRepository.save(tag);
    }

    // 태그 복구
    public void restoreTag(Integer tagId) {
        Tags tag = tagsRepository.findById(tagId).orElseThrow(() ->
                new ApplicationException(ApplicationError.TAG_NOT_FOUND));
        tag.unSetDeleted();
        tagsRepository.save(tag);
    }

    // 태그 최신순 조회
    public Page<ResTagDto> getTagListLatest(Integer startOffset) {
        if(startOffset == null) {
            startOffset = 0;
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "id"); // 정렬 기준: 기본키 기준으로 내림차순 정렬
        Pageable pageable = PageRequest.of(startOffset, tagPageSize, sort);
        Page<ResTagDto> result = tagsRepository.findActivePaging(pageable).map(tags -> ResTagDto.idTagCategoryNameName(tags.getId(), tags.getCategory().getName(), tags.getName()));
        if(!result.hasContent()) {
            throw new ApplicationException(ApplicationError.TAG_NOT_FOUND);
        }
        return result;
    }

    // 태그 키워드 조회
    public Page<ResTagDto> getTagListKeyword(String keyword, Integer startOffset) {
        if(keyword.isBlank()) {
            throw new ApplicationException(ApplicationError.TAG_NOT_FOUND);
        }
        if(startOffset == null) {
            startOffset = 0;
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(startOffset, tagPageSize, sort);
        Page<ResTagDto> result = tagsRepository.findByKeywordActivePaging(keyword ,pageable).map(tags -> ResTagDto.idTagCategoryNameName(tags.getId(), tags.getCategory().getName(), tags.getName()));
        if(!result.hasContent()) {
            throw new ApplicationException(ApplicationError.TAG_NOT_FOUND);
        }
        return result;
    }
}
