create table a(
    ab int primary key
);

create table b(
    c int references a(ab),
    d int,
    g int,
    primary key (c,d,g)
);

insert into a values (1);

insert into b values(1,23,2);
insert into b values (1,2,2);


select * from users where email = 'bb';

INSERT INTO lobby (player_A, player_B, grid, opening_rules, variant)
VALUES
    (2, 2, 15, 'Standard', 'Standard');

INSERT INTO match (match_id, start_time, end_time)
VALUES
    (2, 1634800000, null);

update match set end_time = (select start_time) + 3000 where match_id = 2;

select * from (select id from lobby where player_a = 2 or player_b = 2) as li right join match m on li.id=m.match_id where end_time is null;

SELECT * FROM Lobby
WHERE player_B is null and grid = 15 and opening_rules = 'BoardRun' and variant = 'BoardRun';
select * from (select id from lobby where id = 3)
as lobby right join match m on lobby.id=m.match_id;

select * from  match where match_id = 3;

select * from
    (select id from lobby where player_a = 24 or player_b = 24)
        as lobby left join match m on lobby.id=m.match_id
where end_time is null