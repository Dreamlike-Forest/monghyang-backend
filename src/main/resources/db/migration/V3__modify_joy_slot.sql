-- JoySlot의 Joy 테이블에 대한 외래키를 위한 단일인덱스를 별도로 생성
alter table joy_slot add index fk_joy_id(joy_id);

-- 기존 인덱스 제거
alter table joy_slot drop index joy_reservation_uk;

-- '예약 일시' 컬럼을 '예약 일자', '예약 시간대' 컬럼으로 분할
alter table joy_slot change reservation reservation_date date not null;
alter table joy_slot add column reservation_time time(6) not null;

-- 특정 시간대의 '현재 예약 인원 수' 컬럼 생성
alter table joy_slot add column count int(11) not null;
-- 예약 인원 수는 0 이상의 정수만 허용
alter table joy_slot add constraint ck_count_non_negative check (count >= 0);

-- UK 제약조건 설정: (체험 식별자, 예약 일자, 예약 일시)
alter table joy_slot add constraint uk_joy_id_reservation_date_time unique (joy_id, reservation_date, reservation_time);

-- joy 테이블에 '동 시간대 최대 예약 가능 인원수' 컬럼 추가
alter table joy add column max_count int(11) not null;
-- 최대 예약 가능 인원수는 1 이상의 정수만 허용
alter table joy add constraint ck_max_count_non_negative check (max_count >= 1);