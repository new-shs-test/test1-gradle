
drop table  if exists outcome;
drop table  if exists play;
drop table  if exists token_info;
drop table  if exists match;
drop table  if exists lobby;
drop table  if exists users;





create table users
(
    id       serial primary key,
    password varchar(256)        not null,
    nickname varchar(100) unique not null unique,
    email    varchar(319) unique not null unique

);
create table token_info
(
    token        varchar(256) primary key,
    player_id    int references users (id),
    created_at   bigint not null,
    last_used_at bigint not null
);
create table lobby
(
    id            serial primary key        not null,
    player_A      int references users (id) not null,
    player_B      int references users (id),
    grid          int                       not null,
    opening_rules varchar(200)              not null,
    variant       varchar(20)               not null--variacao das regras
);
create table match
(
    match_id   int primary key references lobby (id),
    black_id   int ,
    start_time bigint not null,
    end_time    bigint
);

create table outcome
(
    match_id int references match (match_id) not null,
    winner   int references users (id),
    a_points int                             not null,
    b_points int                             not null,
    duration bigint                          not null
);

create table play
(
    match_id  int references match (match_id),
    line      int,
    col       int,
    primary key (match_id, line, col)
);

