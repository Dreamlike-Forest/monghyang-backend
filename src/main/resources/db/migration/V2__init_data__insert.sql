-- role, region_type, tag_category, tags 테이블에 기본 데이터 레코드를 삽입
-- 이미 데이터가 존재하는 테이블에는 insert를 수행하지 않는다.

-- ============================================
-- 권한 정보 (role) 기본값 추가 (idempotent)
-- ============================================
INSERT INTO role (id, name)
SELECT 1, 'ROLE_ADMIN'
    WHERE NOT EXISTS (SELECT 1 FROM role WHERE id = 1);

INSERT INTO role (id, name)
SELECT 2, 'ROLE_BREWERY'
    WHERE NOT EXISTS (SELECT 1 FROM role WHERE id = 2);

INSERT INTO role (id, name)
SELECT 3, 'ROLE_SELLER'
    WHERE NOT EXISTS (SELECT 1 FROM role WHERE id = 3);

INSERT INTO role (id, name)
SELECT 4, 'ROLE_USER'
    WHERE NOT EXISTS (SELECT 1 FROM role WHERE id = 4);



-- ============================================
-- 양조장 지역 정보 (region_type) 기본값 추가
-- ============================================
INSERT INTO region_type (id, name)
SELECT 1, '미정'
    WHERE NOT EXISTS (SELECT 1 FROM region_type WHERE id = 1);

INSERT INTO region_type (id, name)
SELECT 2, '서울'
    WHERE NOT EXISTS (SELECT 1 FROM region_type WHERE id = 2);

INSERT INTO region_type (id, name)
SELECT 3, '경기도'
    WHERE NOT EXISTS (SELECT 1 FROM region_type WHERE id = 3);

INSERT INTO region_type (id, name)
SELECT 4, '강원도'
    WHERE NOT EXISTS (SELECT 1 FROM region_type WHERE id = 4);

INSERT INTO region_type (id, name)
SELECT 5, '충청도'
    WHERE NOT EXISTS (SELECT 1 FROM region_type WHERE id = 5);

INSERT INTO region_type (id, name)
SELECT 6, '전라도'
    WHERE NOT EXISTS (SELECT 1 FROM region_type WHERE id = 6);

INSERT INTO region_type (id, name)
SELECT 7, '경상도'
    WHERE NOT EXISTS (SELECT 1 FROM region_type WHERE id = 7);

INSERT INTO region_type (id, name)
SELECT 8, '제주도'
    WHERE NOT EXISTS (SELECT 1 FROM region_type WHERE id = 8);



-- ============================================
-- 태그 카테고리 (tag_category) 기본값 추가
-- ============================================
INSERT INTO tag_category (id, name, is_deleted)
SELECT 1, '주종', 0
    WHERE NOT EXISTS (SELECT 1 FROM tag_category WHERE id = 1);

INSERT INTO tag_category (id, name, is_deleted)
SELECT 2, '배지', 0
    WHERE NOT EXISTS (SELECT 1 FROM tag_category WHERE id = 2);

INSERT INTO tag_category (id, name, is_deleted)
SELECT 3, '기타', 0
    WHERE NOT EXISTS (SELECT 1 FROM tag_category WHERE id = 3);



-- ============================================
-- 태그 (tags) 기본값 추가
-- ============================================
INSERT INTO tags (id, is_deleted, name, tag_category_id)
SELECT 1, 0, '막걸리', 1
    WHERE NOT EXISTS (SELECT 1 FROM tags WHERE id = 1);

INSERT INTO tags (id, is_deleted, name, tag_category_id)
SELECT 2, 0, '청주', 1
    WHERE NOT EXISTS (SELECT 1 FROM tags WHERE id = 2);

INSERT INTO tags (id, is_deleted, name, tag_category_id)
SELECT 3, 0, '소주', 1
    WHERE NOT EXISTS (SELECT 1 FROM tags WHERE id = 3);

INSERT INTO tags (id, is_deleted, name, tag_category_id)
SELECT 4, 0, '명인', 2
    WHERE NOT EXISTS (SELECT 1 FROM tags WHERE id = 4);

INSERT INTO tags (id, is_deleted, name, tag_category_id)
SELECT 5, 0, '과실주', 1
    WHERE NOT EXISTS (SELECT 1 FROM tags WHERE id = 5);

INSERT INTO tags (id, is_deleted, name, tag_category_id)
SELECT 6, 0, '증류주', 1
    WHERE NOT EXISTS (SELECT 1 FROM tags WHERE id = 6);

INSERT INTO tags (id, is_deleted, name, tag_category_id)
SELECT 7, 0, '리큐르', 1
    WHERE NOT EXISTS (SELECT 1 FROM tags WHERE id = 7);

INSERT INTO tags (id, is_deleted, name, tag_category_id)
SELECT 8, 0, '기타', 1
    WHERE NOT EXISTS (SELECT 1 FROM tags WHERE id = 8);

INSERT INTO tags (id, is_deleted, name, tag_category_id)
SELECT 9, 0, '친환경', 2
    WHERE NOT EXISTS (SELECT 1 FROM tags WHERE id = 9);

INSERT INTO tags (id, is_deleted, name, tag_category_id)
SELECT 10, 0, '프리미엄', 3
    WHERE NOT EXISTS (SELECT 1 FROM tags WHERE id = 10);

INSERT INTO tags (id, is_deleted, name, tag_category_id)
SELECT 11, 0, '베스트', 3
    WHERE NOT EXISTS (SELECT 1 FROM tags WHERE id = 11);

INSERT INTO tags (id, is_deleted, name, tag_category_id)
SELECT 12, 0, '유기농', 2
    WHERE NOT EXISTS (SELECT 1 FROM tags WHERE id = 12);

INSERT INTO tags (id, is_deleted, name, tag_category_id)
SELECT 13, 0, '전통기법', 2
    WHERE NOT EXISTS (SELECT 1 FROM tags WHERE id = 13);