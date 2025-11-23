package com.example.monghyang.domain.tag.service;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.tag.dto.ResTagCategoryDto;
import com.example.monghyang.domain.tag.entity.TagCategory;
import com.example.monghyang.domain.tag.repository.TagCategoryRepository;
import lombok.Getter;
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
public class TagCategoryService {
    private final TagCategoryRepository tagCategoryRepository;
    @Getter
    private final int TAG_CATEGORY_PAGE_SIZE = 10;
    @Autowired
    public TagCategoryService(TagCategoryRepository tagCategoryRepository) {
        this.tagCategoryRepository = tagCategoryRepository;
    }

    // 태그 카테고리 생성
    public void createTagCategory(String categoryName) throws DataIntegrityViolationException {
        tagCategoryRepository.save(TagCategory.categoryNameFrom(categoryName));
    }

    // 태그 카테고리 이름 수정
    public void updateTagCategoryName(Integer categoryId, String categoryName) throws DataIntegrityViolationException {
        TagCategory tagCategory = tagCategoryRepository.findById(categoryId).orElseThrow(() ->
                new ApplicationException(ApplicationError.TAG_CATEGORY_NOT_FOUND));
        tagCategory.updateCategoryName(categoryName);
        tagCategoryRepository.save(tagCategory);
    }

    // 태그 카테고리 삭제 처리
    public void deleteTagCategory(Integer categoryId) {
        TagCategory tagCategory = tagCategoryRepository.findById(categoryId).orElseThrow(() ->
                new ApplicationException(ApplicationError.TAG_CATEGORY_NOT_FOUND));
        tagCategory.setDeleted();
        tagCategoryRepository.save(tagCategory);
    }

    // 태그 카테고리 복구
    public void restoreTagCategory(Integer categoryId) {
        TagCategory tagCategory = tagCategoryRepository.findById(categoryId).orElseThrow(() ->
                new ApplicationException(ApplicationError.TAG_CATEGORY_NOT_FOUND));
        tagCategory.unSetDeleted();
        tagCategoryRepository.save(tagCategory);
    }

    // 태그 카테고리 최신순 조회
    public Page<ResTagCategoryDto> getTagCategoryListLatest(Integer startOffset) { // startOffset: 0부터 시작. 페이지 번호
        if(startOffset == null) {
            startOffset = 0;
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "id"); // 정렬 기준: 기본키 기준으로 내림차순 정렬
        Pageable pageable = PageRequest.of(startOffset, TAG_CATEGORY_PAGE_SIZE, sort);
        Page<ResTagCategoryDto> result = tagCategoryRepository.findActivePaging(pageable).map(ResTagCategoryDto::tagCategoryFrom); // 조회 결과를 dto에 담아 반환
        if(!result.hasContent()) {
            throw new ApplicationException(ApplicationError.TAG_CATEGORY_NOT_FOUND);
        }
        return result;
    }

    // 태그 카테고리 키워드 조회
    public Page<ResTagCategoryDto> getTagCategoryListKeyword(String keyword, Integer startOffset) {
        if(keyword.isBlank()) {
            throw new ApplicationException(ApplicationError.TAG_CATEGORY_NOT_FOUND);
        }
        if(startOffset == null) {
            startOffset = 0;
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(startOffset, TAG_CATEGORY_PAGE_SIZE, sort);
        Page<ResTagCategoryDto> result = tagCategoryRepository.findByKeywordActivePaging(keyword, pageable).map(ResTagCategoryDto::tagCategoryFrom);
        if(!result.hasContent()) {
            throw new ApplicationException(ApplicationError.TAG_CATEGORY_NOT_FOUND);
        }
        return result;
    }
}
