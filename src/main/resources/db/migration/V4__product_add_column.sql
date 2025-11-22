-- 상품 테이블에 '재고 수량' 컬럼을 추가
alter table product add column inventory int(11) not null;

-- 재고 수량에 대한 제약 조건 추가 전, 제약조건에 위배되는 데이터를 미리 처리하여 오류 방지
update product set inventory = 0 where inventory < 0;

-- 제약조건 설정: 재고 수량은 반드시 0 이상이어야 한다.
alter table product add constraint ck_inventory_non_negative check (inventory >= 0);