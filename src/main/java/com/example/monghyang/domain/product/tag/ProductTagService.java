package com.example.monghyang.domain.product.tag;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.product.entity.Product;
import com.example.monghyang.domain.product.repository.ProductRepository;
import com.example.monghyang.domain.tag.dto.ReqTagDto;
import com.example.monghyang.domain.tag.dto.ResTagListDto;
import com.example.monghyang.domain.tag.entity.Tags;
import com.example.monghyang.domain.tag.repository.TagsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductTagService {
    private final ProductTagRepository productTagRepository;
    private final ProductRepository productRepository;
    private final TagsRepository tagsRepository;

    @Autowired
    public ProductTagService(ProductTagRepository productTagRepository, ProductRepository productRepository, TagsRepository tagsRepository) {
        this.productTagRepository = productTagRepository;
        this.productRepository = productRepository;
        this.tagsRepository = tagsRepository;
    }

    // 태그 추가 및 삭제
    @Transactional
    public void updateTag(Long userId, Long productId, ReqTagDto reqTagDto) {
        Product product = productRepository.findByUserId(userId, productId).orElseThrow(() ->
                new ApplicationException(ApplicationError.PRODUCT_NOT_FOUND));

        // 태그 일괄삭제
        productTagRepository.deleteByProductIdAndTagId(product.getId(), reqTagDto.getAdd_tag_list());

        // 태그 추가
        for(Integer curTagId : reqTagDto.getAdd_tag_list()) {
            Tags tags = tagsRepository.findById(curTagId).orElseThrow(() ->
                    new ApplicationException(ApplicationError.TAG_NOT_FOUND));
            productTagRepository.save(ProductTag.productTagsOf(product, tags));
        }
    }

    // 특정 상품이 가진 태그 조회
    public List<ResTagListDto> getProductTagsById(Long productId) {
        List<ProductTag> result = productTagRepository.findByProductId(productId);
        if(result.isEmpty()) {
            throw new ApplicationException(ApplicationError.TAG_NOT_FOUND);
        }
        return result.stream().map(productTag ->
                    ResTagListDto.tagIdTagName(productTag.getTags().getId(), productTag.getTags().getName()))
                .toList();
    }
}
