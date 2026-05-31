-- 양조장 테이블의 'start_time', 'end_time' 컬럼 제거

alter table brewery drop column start_time;
alter table brewery drop column end_time;