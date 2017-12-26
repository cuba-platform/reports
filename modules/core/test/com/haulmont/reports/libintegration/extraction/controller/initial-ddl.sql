-- Description: test initialisation data
drop table if exists TEST_USER cascade;
drop table if exists TEST_MONTH cascade;
drop table if exists TEST_TIME_ENTRY cascade;

create table TEST_MONTH (
  ID   int primary key,
  NAME varchar(50)
);
create table TEST_USER (
  ID    uuid default newid() not null primary key,
  LOGIN varchar(50) unique   not null
);
create table TEST_TIME_ENTRY (
  ID              uuid default newid() not null primary key,
  USER_ID         uuid                 not null references TEST_USER (ID),
  DATE_           date                 not null,
  TIME_IN_MINUTES integer default 0    not null
);

insert into TEST_MONTH (NAME, ID) values
  ('january', 1),
  ('february', 2),
  ('march', 3),
  ('april', 4),
  ('may', 5),
  ('june', 6),
  ('july', 7),
  ('august', 8),
  ('september', 9),
  ('october', 10),
  ('november', 11),
  ('december', 12);
insert into TEST_USER (LOGIN) values
  ('tedd'),
  ('fred'),
  ('dead');
insert into TEST_TIME_ENTRY (USER_ID, DATE_, TIME_IN_MINUTES)
  select distinct
    U.ID                                                                                                        as USER_ID,
    (extract('year' from current_date) || '-' || to_char(MONTH, 'fm00') || '-' || to_char(DAY,
                                                                                          'fm00')) :: date      as DATE_,
    (random() * 8 + 2) :: int                                                                                      TIME_IN_MINUTES
  from TEST_USER U,
        generate_series(1, (random() * 26 + 2) :: int, 2) as DAY,
        generate_series(1, (random() * 11 + 1) :: int) as MONTH;

