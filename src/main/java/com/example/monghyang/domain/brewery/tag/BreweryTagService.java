package com.example.monghyang.domain.brewery.tag;

import com.example.monghyang.domain.brewery.main.entity.Brewery;
import com.example.monghyang.domain.brewery.main.repository.BreweryRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.tag.entity.Tags;
import com.example.monghyang.domain.tag.repository.TagsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class BreweryTagService {
    private final BreweryTagRepository breweryTagRepository;
    private final BreweryRepository breweryRepository;
    private final TagsRepository tagsRepository;

    @Autowired
    public BreweryTagService(BreweryTagRepository breweryTagRepository, BreweryRepository breweryRepository, TagsRepository tagsRepository) {
        this.breweryTagRepository = breweryTagRepository;
        this.breweryRepository = breweryRepository;
        this.tagsRepository = tagsRepository;
    }

    // 태그 수정
    public void updateTag(Long userId, ReqBreweryTagDto reqBreweryTagDto) throws DataIntegrityViolationException {
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));

        // 태그 추가
        for(Integer curTagId : reqBreweryTagDto.getAdd_tag_list()) {
            Tags tags = tagsRepository.findById(curTagId).orElseThrow(() ->
                    new ApplicationException(ApplicationError.TAG_NOT_FOUND));
            breweryTagRepository.save(BreweryTag.breweryTags(brewery, tags));
        }

        // 태그 삭제
        for(Integer curTagId : reqBreweryTagDto.getDelete_tag_list()) {
            breweryTagRepository.deleteByBreweryIdAndTagId(brewery.getId(), curTagId);
        }
    }

}
