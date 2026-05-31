-- joy 테이블에 'min_count' 컬럼 추가

alter table joy add column min_count int(11);
update joy set min_count = 1 where min_count is null;
alter table joy modify column min_count int(11) not null;