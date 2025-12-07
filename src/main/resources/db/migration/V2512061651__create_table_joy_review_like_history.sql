-- joy_review_like_history 테이블 추가
-- 회원 식별자와 체험 리뷰 식별자의 조합을 UK 설정: 중복 좋아요 방지

create table joy_review_like_history (
    id bigint(20) not null auto_increment,
    user_id bigint(20) not null,
    joy_review_id bigint(20) not null,
    created_at datetime(6) not null,
    primary key (id),
    constraint fk_user_id foreign key (user_id) references users(id),
    constraint fk_joy_review_id foreign key (joy_review_id) references joy_review(id),
    constraint uk_user_id_joy_review_id unique (user_id, joy_review_id)
)