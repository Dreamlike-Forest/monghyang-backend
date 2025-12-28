-- closed_status 에 대해 not null 설정

alter table brewery_closed_date modify closed_status enum('PENDING', 'CONFIRMED') not null;
alter table joy_closed_date modify closed_status enum('PENDING', 'CONFIRMED') not null;
alter table joy_closed_start_time modify closed_status enum('PENDING', 'CONFIRMED') not null;