-- '별도 휴무' 관련 테이블에 enum 타입 'status' 컬럼 추가
-- 컬럼 추가 대상 테이블: '양조장 별도 휴무일', '체험 별도 휴무일', '체험 별도 휴무 시작 시간대'

alter table brewery_closed_date add column closed_status enum('PENDING', 'CONFIRMED');
alter table joy_closed_date add column closed_status enum('PENDING', 'CONFIRMED');
alter table joy_closed_start_time add column closed_status enum('PENDING', 'CONFIRMED');