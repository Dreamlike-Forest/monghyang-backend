package com.example.monghyang.domain.product.service;

import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.tag.BreweryTagRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.image.dto.AddImageDto;
import com.example.monghyang.domain.image.dto.ModifySeqImageDto;
import com.example.monghyang.domain.image.service.ImageType;
import com.example.monghyang.domain.image.service.StorageService;
import com.example.monghyang.domain.product.dto.*;
import com.example.monghyang.domain.product.entity.Product;
import com.example.monghyang.domain.product.entity.ProductImage;
import com.example.monghyang.domain.product.repository.ProductImageRepository;
import com.example.monghyang.domain.product.repository.ProductRepository;
import com.example.monghyang.domain.product.tag.ProductTagRepository;
import com.example.monghyang.domain.seller.repository.SellerRepository;
import com.example.monghyang.domain.tag.dto.TagNameDto;
import com.example.monghyang.domain.users.dto.UserSimpleInfoDto;
import com.example.monghyang.domain.users.entity.RoleType;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductService {
    private final int PRODUCT_PAGE_SIZE = 12;
    private final ProductRepository productRepository;
    private final UsersRepository usersRepository;
    private final StorageService storageService;
    private final ProductImageRepository productImageRepository;
    private final ProductTagRepository productTagRepository;
    private final BreweryRepository breweryRepository;
    private final SellerRepository sellerRepository;
    private final BreweryTagRepository breweryTagRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, UsersRepository usersRepository, StorageService storageService, ProductImageRepository productImageRepository, ProductTagRepository productTagRepository, BreweryRepository breweryRepository, SellerRepository sellerRepository, BreweryTagRepository breweryTagRepository) {
        this.productRepository = productRepository;
        this.usersRepository = usersRepository;
        this.storageService = storageService;
        this.productImageRepository = productImageRepository;
        this.productTagRepository = productTagRepository;
        this.breweryRepository = breweryRepository;
        this.sellerRepository = sellerRepository;
        this.breweryTagRepository = breweryTagRepository;
    }

    private void addTagListToResult(Page<ResProductListDto> result) {
        List<Long> productIdList = result.getContent().stream().map(ResProductListDto::getProduct_id).toList();
        // 상품 ID 리스트 추출하여 상품들이 가진 '인증' 태그 조회
        List<TagNameDto> productAuthTagList = productTagRepository.findAuthTagListByProductIdList(productIdList);
        HashMap<Long, List<String>> productIdTagMap = new HashMap<>();
        for(TagNameDto cur : productAuthTagList) { // 결과를 Map에 저장
            productIdTagMap.computeIfAbsent(cur.ownerId(), k -> new ArrayList<>()).add(cur.tagName());
        }
        for(ResProductListDto dto : result) { // Map에 저장된 태그 리스트들을 각각의 상품 dto에 알맞게 추가
            dto.setTag_name(productIdTagMap.getOrDefault(dto.getProduct_id(), Collections.emptyList()));
        }
    }

    // 상품 최신순 조회 페이징
    public Page<ResProductListDto> getProductLatest(Integer startOffset) {
        if(startOffset == null) {
            startOffset = 0;
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "registeredAt");
        Pageable pageable = PageRequest.of(startOffset, PRODUCT_PAGE_SIZE, sort);

        // 상품 조회
        Page<ResProductListDto> result = productRepository.findActiveLatest(pageable);
        if(result.isEmpty()) {
            throw new ApplicationException(ApplicationError.PRODUCT_NOT_FOUND);
        }

        addTagListToResult(result);
        return result;
    }

    // 상품 필터링 검색 페이징
    public Page<ResProductListDto> dynamicSearch(Integer startOffset, String keyword,
                 Integer minPrice, Integer maxPrice, Double minAlcohol, Double maxAlcohol, List<Integer> tagIdList) {
        if(startOffset == null) {
            startOffset = 0;
        }
        boolean tagListIsEmpty = tagIdList == null || tagIdList.isEmpty();
        Sort sort = Sort.by(Sort.Direction.DESC, "registeredAt");
        Pageable pageable = PageRequest.of(startOffset, PRODUCT_PAGE_SIZE, sort);
        Page<ResProductListDto> result = productRepository.findByDynamicFiltering(pageable, tagListIsEmpty, keyword, minPrice, maxPrice, minAlcohol, maxAlcohol, tagIdList);
        if(result.isEmpty()) {
            throw new ApplicationException(ApplicationError.PRODUCT_NOT_FOUND);
        }
        addTagListToResult(result);
        return result;
    }

    // 상품 상세 조회(식별자 기준)
    public ResProductDto getProductById(Long productId) {
        // 1. 상품 조회
        Product product = productRepository.findById(productId).orElseThrow(() ->
                new ApplicationException(ApplicationError.PRODUCT_NOT_FOUND));
        ResProductDto result = ResProductDto.productFrom(product);

        // 2. 회원 테이블에서 'username'과 'role_type'만 조회하기
        // 이 role type에 따라서 판매자/양조장 중 하나의 테이블을 조회
        UserSimpleInfoDto userInfo = usersRepository.findNicknameAndRoleType(product.getUser().getId()).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        result.setUser_nickname(userInfo.nickname());

        // 3. 상품이 가진 모든 태그 리스트 반환
        List<String> productTagList = productTagRepository.findTagListByProductId(productId);
        result.setTags_name(productTagList);

        // 4. 상품이 가진 모든 이미지 key 리스트 반환
        List<ResProductImageDto> productImageKeyList = productImageRepository.findSimpleByProductId(productId);
        result.setProduct_image_image_key(productImageKeyList);

        // 5. 판매자의 회원 타입 유형에 따라 상품의 '소유자' 간단 정보를 dto에 삽입
        if(userInfo.roleType().equals(RoleType.ROLE_BREWERY)) {
            ResProductOwnerDto owner = breweryRepository.findSimpleInfoByUserId(product.getUser().getId()).orElseThrow(() ->
                    new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
            result.setOwner(owner);
            // 양조장의 경우 태그 리스트까지 조회하여 반환
            List<String> tagList = breweryTagRepository.findTagListByBreweryId(owner.getOwner_id());
            result.getOwner().setTags_name(tagList);
        } else if(userInfo.roleType().equals(RoleType.ROLE_SELLER)) {
            ResProductOwnerDto owner = sellerRepository.findSimpleInfoByUserId(product.getUser().getId()).orElseThrow(() ->
                    new ApplicationException(ApplicationError.SELLER_NOT_FOUND));
            result.setOwner(owner);
        } else {
            // role 정보가 잘못됨.
            throw new ApplicationException(ApplicationError.SERVER_ERROR);
        }
        return result;
    }

    // 특정 판매자(혹은 양조장)의 모든 상품 조회
    public Page<ResProductListDto> getProductByUserId(Long userId, Integer startOffset) {
        if(startOffset == null) {
            startOffset = 0;
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "registeredAt");
        Pageable pageable = PageRequest.of(startOffset, PRODUCT_PAGE_SIZE, sort);
        Page<ResProductListDto> result = productRepository.findActiveByUserId(userId, pageable);
        if(result.isEmpty()) {
            throw new ApplicationException(ApplicationError.PRODUCT_NOT_FOUND);
        }

        addTagListToResult(result);
        return result;
    }

    /**
     * 상품 재고 증가(입고)
     * @param userId 판매자의 userId
     * @param productId 상품 식별자
     * @param quantity 증가할 수량 값
     */
    @Transactional
    public void increseInventory(Long productId, Long userId, Integer quantity) {
        int ret = productRepository.increseInventory(productId, userId, quantity);
        if(ret == 0) {
            // 수정되지 않았다면 자신의 상품이 아닌 것
            throw new ApplicationException(ApplicationError.FORBIDDEN);
        }
    }

    /**
     * 상품 재고 감소(출소 / 구매 등)
     * @param productId 상품 식별자
     * @param quantity 수량 감소분
     */
    @Transactional
    public void decreseInventory(Long productId, Integer quantity) {
        productRepository.decreseInventory(productId, quantity);
    }

    // 상품 등록
    @Transactional
    public void createProduct(Long userId, ReqProductDto reqProductDto) {
        Users user = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));

        // 상품 추가
        Product product = Product.builder()
                .user(user).name(reqProductDto.getName()).alcohol(reqProductDto.getAlcohol()).isOnlineSell(reqProductDto.getIs_online_sell())
                .volume(reqProductDto.getVolume()).originPrice(reqProductDto.getOrigin_price()).inventory(reqProductDto.getInventory())
                .description(reqProductDto.getDescription()).build();
        productRepository.save(product);

        System.out.println(product.getName());

        // 상품 이미지 추가
        if(reqProductDto.getImages() != null) {
            for(AddImageDto cur : reqProductDto.getImages()) {
                String imageKey = storageService.upload(cur.getImage(), ImageType.PRODUCT_IMAGE);
                productImageRepository.save(ProductImage.productImageKeySeqVolumeOf(product, imageKey, cur.getSeq(), cur.getImage().getSize()));
            }
        }
    }

    // 상품 수정
    @Transactional
    public void updateProduct(Long userId, UpdateProductDto updateProductDto) {
        Product product = productRepository.findByIdAndUserId(updateProductDto.getId(), userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.PRODUCT_NOT_FOUND));
        if(!updateProductDto.getAdd_images().isEmpty() || !updateProductDto.getRemove_images().isEmpty() || !updateProductDto.getModify_images().isEmpty()) {
            List<ProductImage> imageList = productImageRepository.findByProduct(product);

            Set<Long> imageListIds = imageList.stream().map(ProductImage::getId).collect(Collectors.toSet());

            // 수정 이후의 이미지 총 개수 계산 및 검증
            if(imageList.size() - updateProductDto.getRemove_images().size() + updateProductDto.getAdd_images().size() > 5) {
                throw new ApplicationException(ApplicationError.IMAGE_COUNT_INVALID);
            }

            // 이미지 삭제
            for(Long removeImageId : updateProductDto.getRemove_images()) {
                if(!imageListIds.contains(removeImageId)) {
                    throw new ApplicationException(ApplicationError.REQUEST_FORBIDDEN);
                }
                ProductImage productImage = productImageRepository.findById(updateProductDto.getId()).orElseThrow(() ->
                        new ApplicationException(ApplicationError.IMAGE_NOT_FOUND));
                storageService.remove(productImage.getImageKey()); // 스토리지에서 이미지 삭제
                productImageRepository.delete(productImage);
                imageListIds.remove(removeImageId); // set에 반영
            }

            productImageRepository.flush(); // 삭제 정보 선반영: 이후의 수정 로직에서 uk 제약조건 위배를 피하기 위함

            // 이미지 순서 정보 수정
            for(ModifySeqImageDto cur : updateProductDto.getModify_images()) {
                if(!imageListIds.contains(cur.getImage_id())) {
                    throw new ApplicationException(ApplicationError.REQUEST_FORBIDDEN);
                }
                if(cur.getSeq() > 5 || cur.getSeq() < 1) {
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
                }
                ProductImage productImage = productImageRepository.findById(cur.getImage_id()).orElseThrow(() ->
                        new ApplicationException(ApplicationError.IMAGE_NOT_FOUND));
                productImage.updateSeq(cur.getSeq());
                try {
                    productImageRepository.save(productImage);
                } catch (DataIntegrityViolationException e) {
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
                }
            }

            // 이미지 업로드
            for(AddImageDto cur : updateProductDto.getAdd_images()) {
                if(cur.getSeq() > 5 || cur.getSeq() < 1) {
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
                }
                String imageKey = storageService.upload(cur.getImage(), ImageType.PRODUCT_IMAGE);
                try {
                    productImageRepository.save(ProductImage.productImageKeySeqVolumeOf(product, imageKey, cur.getSeq(), cur.getImage().getSize()));
                } catch (DataIntegrityViolationException e) {
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
                }
            }
        }
        // Product 테이블 컬럼에 대한 수정사항 반영
        if(updateProductDto.getName() != null) {
            product.updateName(updateProductDto.getName());
        }
        if(updateProductDto.getAlcohol() != null) {
            product.updateAlcohol(updateProductDto.getAlcohol());
        }
        if(updateProductDto.getIs_online_sell() != null) {
            product.updateIsOnlineSell(updateProductDto.getIs_online_sell());
        }
        if(updateProductDto.getVolume() != null) {
            product.updateVolume(updateProductDto.getVolume());
        }
        if(updateProductDto.getOrigin_price() != null) {
            product.updateOriginPrice(updateProductDto.getOrigin_price());
        }
        if(updateProductDto.getDescription() != null) {
            product.updateDescription(updateProductDto.getDescription());
        }
        if(updateProductDto.getIs_soldout() != null) {
            product.updateSoldout(updateProductDto.getIs_soldout());
        }
        if(updateProductDto.getDiscount_rate() != null) {
            product.updateDiscountRate(updateProductDto.getDiscount_rate());
        }
    }

    // 상품 삭제 처리
    public void deleteProduct(Long userId, Long productId) {
        Product product = productRepository.findByIdAndUserId(productId, userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.PRODUCT_NOT_FOUND));
        product.setDeleted();
        productRepository.save(product);
    }

    // 상품 삭제 복구
    public void restoreProduct(Long userId, Long productId) {
        Product product = productRepository.findByIdAndUserId(productId, userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.PRODUCT_NOT_FOUND));
        product.unSetDeleted();
        productRepository.save(product);
    }
}
