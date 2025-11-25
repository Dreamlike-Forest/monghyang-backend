-- community_like 테이블 추가

create table community_like (
    community_like_id bigint(20) primary key,
    community_id bigint(20) not null,
    user_id bigint(20) not null,
    created_at datetime(6) not null,
    foreign key(community_id) references community(community_id),
    foreign key(user_id) references users(id),
    constraint uk_community_id_user_id unique (community_id, user_id)
);