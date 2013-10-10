
create table REPORT_GROUP (
  ID uuid not null,
  CREATE_TS timestamp,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS timestamp,
  UPDATED_BY varchar(50),
  --
  TITLE varchar(255) not null,
  CODE varchar(255),
  LOCALE_NAMES text,
  --
  primary key (ID)
)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_REPORT
(
  ID uuid not null,
  CREATE_TS timestamp,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS timestamp,
  UPDATED_BY varchar(50),
  --
  NAME varchar(255) not null,
  CODE varchar(255),
  LOCALE_NAMES text,
  GROUP_ID uuid not null,
  REPORT_TYPE integer,
  DEFAULT_TEMPLATE_ID uuid,
  XML text,
  --
  primary key (ID),
  constraint FK_REPORT_REPORT_TO_REPORT_GROUP foreign key (GROUP_ID)
      references REPORT_GROUP (ID)
)^

--create unique index UK_REPORT_REPORT_CODE on REPORT_REPORT (CODE) where CODE is not null^

--------------------------------------------------------------------------------------------------------------
--
--create table REPORT_BAND_DEFINITION
--(
--  ID uuid not null,
--  CREATE_TS timestamp,
--  CREATED_BY varchar(50),
--  VERSION integer,
--  UPDATE_TS timestamp,
--  UPDATED_BY varchar(50),
--  --
--  PARENT_DEFINITION_ID uuid,
--  REPORT_ID uuid not null,
--  NAME varchar(255) not null,
--  ORIENTATION integer default 0 not null,
--  POSITION_ integer default 0 not null,
--  --
--  primary key (ID),
--  constraint FK_REPORT_BAND_DEFINITION_TO_REPORT_REPORT foreign key (REPORT_ID)
--      references REPORT_REPORT (ID) on delete cascade,
--  constraint FK_REPORT_BAND_DEFINITION_TO_REPORT_BAND_DEFINITION foreign key (PARENT_DEFINITION_ID)
--      references REPORT_BAND_DEFINITION (ID)
--)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_TEMPLATE
(
  ID uuid not null,
  CREATE_TS timestamp,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS timestamp,
  UPDATED_BY varchar(50),
  --
  REPORT_ID uuid not null,
  CODE varchar(50),
  OUTPUT_TYPE integer default 0 not null,
  IS_DEFAULT boolean default false,
  IS_CUSTOM boolean default false,
  CUSTOM_CLASS varchar,
  OUTPUT_NAME_PATTERN varchar(255),
  NAME varchar(500),
  CONTENT bytea,
  --
  primary key (ID),
  constraint FK_REPORT_TEMPLATE_TO_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID) on delete cascade
)^

--------------------------------------------------------------------------------------------------------------

--create table REPORT_INPUT_PARAMETER
--(
--  ID uuid not null,
--  CREATE_TS timestamp,
--  CREATED_BY varchar(50),
--  VERSION integer,
--  UPDATE_TS timestamp,
--  UPDATED_BY varchar(50),
--  --
--  REPORT_ID uuid not null,
--  PARAMETER_TYPE integer not null,
--  NAME varchar(255) not null,
--  LOCALE_NAMES text,
--  ALIAS varchar(100),
--  SCREEN varchar(255),
--  FROM_BROWSER boolean,
--  REQUIRED boolean default false,
--  POSITION_ integer default 0,
--  META_CLASS varchar(255),
--  ENUM_CLASS varchar(500),
--  --
--  primary key (ID),
--  constraint FK_REPORT_INPUT_PARAMETER_TO_REPORT_REPORT foreign key (REPORT_ID)
--      references REPORT_REPORT (ID) on delete cascade
--)^

--------------------------------------------------------------------------------------------------------------

--create table REPORT_DATA_SET
--(
--  ID uuid not null,
--  CREATE_TS timestamp,
--  CREATED_BY varchar(50),
--  VERSION integer,
--  UPDATE_TS timestamp,
--  UPDATED_BY varchar(50),
--  --
--  NAME varchar(255) not null,
--  TEXT text,
--  DATA_SET_TYPE integer not null,
--  BAND_DEFINITION uuid not null,
--  ENTITY_PARAM_NAME varchar(255),
--  LIST_ENTITIES_PARAM_NAME varchar(255),
--  --
--  primary key (ID),
--  constraint FK_REPORT_DATA_SET_TO_REPORT_BAND_DEFINITION foreign key (BAND_DEFINITION)
--      references REPORT_BAND_DEFINITION (ID) on delete cascade
--)^

--------------------------------------------------------------------------------------------------------------

--create table REPORT_REPORTS_ROLES (
--  REPORT_ID uuid not null,
--  ROLE_ID uuid not null,
--  --
--  constraint FK_REPORT_REPORTS_ROLES_TO_REPORT foreign key (REPORT_ID)
--      references REPORT_REPORT(ID) on delete cascade,
--  constraint FK_REPORT_REPORTS_ROLES_TO_ROLE foreign key (ROLE_ID)
--      references SEC_ROLE(ID)
--)^

--------------------------------------------------------------------------------------------------------------

--create table REPORT_REPORT_SCREEN (
--  ID uuid not null,
--  CREATE_TS timestamp,
--  CREATED_BY varchar(50),
--  VERSION integer,
--  UPDATE_TS timestamp,
--  UPDATED_BY varchar(50),
--  --
--  REPORT_ID uuid not null,
--  SCREEN_ID varchar(255) not null,
--  --
--  primary key (ID),
--  constraint FK_REPORT_REPORT_SCREEN_TO_REPORT_REPORT foreign key (REPORT_ID)
--      references REPORT_REPORT (ID) on delete cascade
--)^

--------------------------------------------------------------------------------------------------------------

--create table REPORT_VALUE_FORMAT (
--  ID uuid not null,
--  CREATE_TS timestamp,
--  CREATED_BY varchar(50),
--  VERSION integer,
--  UPDATE_TS timestamp,
--  UPDATED_BY varchar(50),
--  --
--  REPORT_ID uuid not null,
--  NAME varchar(255) not null,
--  FORMAT varchar(255) not null,
--  --
--  primary key (ID),
--  constraint FK_REPORT_VALUE_FORMAT_TO_REPORT_REPORT foreign key (REPORT_ID)
--      references REPORT_REPORT (ID) on delete cascade
--)^

--------------------------------------------------------------------------------------------------------------
alter table REPORT_REPORT add constraint FK_REPORT_REPORT_TO_DEF_TEMPLATE foreign key (DEFAULT_TEMPLATE_ID)
references REPORT_TEMPLATE (ID);

insert into REPORT_GROUP (ID, CREATE_TS, CREATED_BY, VERSION, TITLE, CODE, LOCALE_NAMES)
values ('4e083530-0b9c-11e1-9b41-6bdaa41bff94', now(), 'admin', 0, 'General', 'ReportGroup.default',
E'en=General \nru=Общие')^
