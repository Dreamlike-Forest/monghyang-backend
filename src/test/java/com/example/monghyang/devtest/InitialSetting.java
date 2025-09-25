package com.example.monghyang.devtest;

import com.example.monghyang.domain.brewery.entity.RegionType;
import com.example.monghyang.domain.brewery.repository.RegionTypeRepository;
import com.example.monghyang.domain.tag.entity.TagCategory;
import com.example.monghyang.domain.tag.entity.Tags;
import com.example.monghyang.domain.tag.repository.TagCategoryRepository;
import com.example.monghyang.domain.tag.repository.TagsRepository;
import com.example.monghyang.domain.users.entity.Role;
import com.example.monghyang.domain.users.entity.RoleType;
import com.example.monghyang.domain.users.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // application.yml에 명시된 데이터베이스 설정을 따라감(기본값은 H2기 때문에 이러한 별도의 설정 필요)
@Rollback(false)
public class InitialSetting {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RegionTypeRepository regionTypeRepository;
    @Autowired
    private TagCategoryRepository tagCategoryRepository;
    @Autowired
    private TagsRepository tagsRepository;
    // 데이터베이스에 기본값(role type 등) 삽입하는 스크립트

    @Test
    @Transactional
    void contextLoads() {
        RoleType[] roles = {RoleType.ROLE_ADMIN, RoleType.ROLE_BREWERY, RoleType.ROLE_SELLER, RoleType.ROLE_USER};
        for(int i = 0; i < roles.length; i++) {
            Role role = new Role();
            role.setId(i+1);
            role.setName(roles[i]);
            roleRepository.save(role);
        }

        regionTypeRepository.save(RegionType.nameFrom("미정"));
        regionTypeRepository.save(RegionType.nameFrom("서울"));
        regionTypeRepository.save(RegionType.nameFrom("경기도"));
        regionTypeRepository.save(RegionType.nameFrom("강원도"));
        regionTypeRepository.save(RegionType.nameFrom("충청도"));
        regionTypeRepository.save(RegionType.nameFrom("전라도"));
        regionTypeRepository.save(RegionType.nameFrom("경상도"));
        regionTypeRepository.save(RegionType.nameFrom("제주도"));

        TagCategory tagCategory1 = TagCategory.categoryNameFrom("주종");
        TagCategory tagCategory2 = TagCategory.categoryNameFrom("배지");
        TagCategory tagCategory3 = TagCategory.categoryNameFrom("기타");
        tagCategoryRepository.save(tagCategory1);
        tagCategoryRepository.save(tagCategory2);
        tagCategoryRepository.save(tagCategory3);

        Tags tags1 = Tags.categoryNameOf(tagCategory1, "막걸리");
        Tags tags2 = Tags.categoryNameOf(tagCategory1, "청주");
        Tags tags3 = Tags.categoryNameOf(tagCategory1, "소주");
        Tags tags4 = Tags.categoryNameOf(tagCategory2, "명인");
        Tags tags5 = Tags.categoryNameOf(tagCategory1, "과실주");
        Tags tags6 = Tags.categoryNameOf(tagCategory1, "증류주");
        Tags tags7 = Tags.categoryNameOf(tagCategory1, "리큐르");
        Tags tags8 = Tags.categoryNameOf(tagCategory1, "기타");
        Tags tags9 = Tags.categoryNameOf(tagCategory2, "친환경");
        Tags tags10 = Tags.categoryNameOf(tagCategory3, "프리미엄");
        Tags tags11 = Tags.categoryNameOf(tagCategory3, "베스트");
        Tags tags12 = Tags.categoryNameOf(tagCategory2, "유기농");
        Tags tags13 = Tags.categoryNameOf(tagCategory2, "전통기법");
        tagsRepository.save(tags1);
        tagsRepository.save(tags2);
        tagsRepository.save(tags3);
        tagsRepository.save(tags4);
        tagsRepository.save(tags5);
        tagsRepository.save(tags6);
        tagsRepository.save(tags7);
        tagsRepository.save(tags8);
        tagsRepository.save(tags9);
        tagsRepository.save(tags10);
        tagsRepository.save(tags11);
        tagsRepository.save(tags12);
        tagsRepository.save(tags13);



    }
}
