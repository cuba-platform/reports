
create table REPORT_GROUP (
  ID varchar(32),
  CREATE_TS datetime(3),
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS datetime(3),
  UPDATED_BY varchar(50),
  DELETE_TS datetime(3),
  DELETED_BY varchar(50),
  DELETE_TS_NN datetime(3) not null default '1000-01-01 00:00:00.000',
  --
  TITLE varchar(190) not null,
  CODE varchar(255),
  LOCALE_NAMES text,
  --
  primary key (ID)
)^

create unique index IDX_REPORT_GROUP_UNIQ_TITLE on REPORT_GROUP (TITLE, DELETE_TS_NN)^

create trigger REPORT_GROUP_DELETE_TS_NN_TRIGGER before update on REPORT_GROUP
  for each row
    if not(NEW.DELETE_TS <=> OLD.DELETE_TS) then
      set NEW.DELETE_TS_NN = if (NEW.DELETE_TS is null, '1000-01-01 00:00:00.000', NEW.DELETE_TS);
    end if^

/**********************************************************************************************/

create table REPORT_REPORT
(
  ID varchar(32),
  CREATE_TS datetime(3),
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS datetime(3),
  UPDATED_BY varchar(50),
  DELETE_TS datetime(3),
  DELETED_BY varchar(50),
  DELETE_TS_NN datetime(3) not null default '1000-01-01 00:00:00.000',
  --
  NAME varchar(190) not null,
  CODE varchar(255),
  DESCRIPTION varchar(500),
  LOCALE_NAMES text,
  GROUP_ID varchar(32) not null,
  REPORT_TYPE integer,
  DEFAULT_TEMPLATE_ID varchar(32),
  XML text,
  ROLES_IDX varchar(1000),
  SCREENS_IDX varchar(1000),
  INPUT_ENTITY_TYPES_IDX varchar(1000),
  REST_ACCESS boolean default false,
  IS_SYSTEM boolean default false,
  --
  primary key (ID),
  constraint FK_REPORT_REPORT_TO_REPORT_GROUP foreign key (GROUP_ID)
      references REPORT_GROUP (ID)
)^

create unique index IDX_REPORT_REPORT_UNIQ_NAME on REPORT_REPORT (NAME, DELETE_TS_NN)^

create trigger REPORT_REPORT_DELETE_TS_NN_TRIGGER before update on REPORT_REPORT
  for each row
    if not(NEW.DELETE_TS <=> OLD.DELETE_TS) then
      set NEW.DELETE_TS_NN = if (NEW.DELETE_TS is null, '1000-01-01 00:00:00.000', NEW.DELETE_TS);
    end if^

/**********************************************************************************************/

create table REPORT_TEMPLATE
(
  ID varchar(32),
  CREATE_TS datetime(3),
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS datetime(3),
  UPDATED_BY varchar(50),
  DELETE_TS datetime(3),
  DELETED_BY varchar(50),
  DELETE_TS_NN datetime(3) not null default '1000-01-01 00:00:00.000',
  --
  REPORT_ID varchar(32) not null,
  CODE varchar(50),
  OUTPUT_TYPE integer default 0 not null,
  IS_DEFAULT boolean default false,
  IS_GROOVY boolean default false,
  IS_CUSTOM boolean default false,
  IS_ALTERABLE_OUTPUT boolean default false,
  CUSTOM_DEFINED_BY integer default 100,
  CUSTOM_CLASS text,
  OUTPUT_NAME_PATTERN varchar(255),
  NAME varchar(500),
  CONTENT mediumblob,
  --
  primary key (ID),
  constraint FK_REPORT_TEMPLATE_TO_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID) on delete cascade
)^

create trigger REPORT_TEMPLATE_DELETE_TS_NN_TRIGGER before update on REPORT_TEMPLATE
  for each row
    if not(NEW.DELETE_TS <=> OLD.DELETE_TS) then
      set NEW.DELETE_TS_NN = if (NEW.DELETE_TS is null, '1000-01-01 00:00:00.000', NEW.DELETE_TS);
    end if^

/**********************************************************************************************/

alter table REPORT_REPORT add constraint FK_REPORT_REPORT_TO_DEF_TEMPLATE foreign key (DEFAULT_TEMPLATE_ID)
references REPORT_TEMPLATE (ID)^

insert into REPORT_GROUP (ID, CREATE_TS, CREATED_BY, VERSION, TITLE, CODE, LOCALE_NAMES)
values ('4e0835300b9c11e19b416bdaa41bff94', current_timestamp, 'admin', 0, 'General', 'ReportGroup.default',
concat('en=General','\n','ru=Общие'))^
