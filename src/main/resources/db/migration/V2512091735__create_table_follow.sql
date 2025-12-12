-- follow 테이블 추가

create table follow (
    follow_id bigint(20) auto_increment primary key,
    follower_id bigint(20) not null,
    following_id bigint(20) not null,
    created_at datetime(6) not null,
    foreign key(follower_id) references users(id),
    foreign key(following_id) references users(id),
    constraint uk_follower_id_following_id unique (follower_id, following_id)
);
