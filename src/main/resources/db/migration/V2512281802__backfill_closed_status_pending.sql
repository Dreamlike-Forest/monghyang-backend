-- closed_status가 null인 값을 'PENDING'으로 일괄 수정

update brewery_closed_date set closed_status = 'PENDING' where closed_status is null;
update joy_closed_date set closed_status = 'PENDING' where closed_status is null;
update joy_closed_start_time set closed_status = 'PENDING' where closed_status is null;