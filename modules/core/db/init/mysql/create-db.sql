
create table REPORT_GROUP (
  ID varchar(32),
  CREATE_TS datetime,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS datetime,
  UPDATED_BY varchar(50),
  --
  TITLE varchar(255) not null,
  CODE varchar(255),
  LOCALE_NAMES text,
  --
  primary key (ID)
)^

create unique index IDX_REPORT_GROUP_UNIQ_TITLE on REPORT_GROUP (TITLE)^

/**********************************************************************************************/

create table REPORT_REPORT
(
  ID varchar(32),
  CREATE_TS datetime,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS datetime,
  UPDATED_BY varchar(50),
  --
  NAME varchar(255) not null,
  CODE varchar(255),
  LOCALE_NAMES text,
  GROUP_ID varchar(32) not null,
  REPORT_TYPE integer,
  DEFAULT_TEMPLATE_ID varchar(32),
  XML text,
  --
  primary key (ID),
  constraint FK_REPORT_REPORT_TO_REPORT_GROUP foreign key (GROUP_ID)
      references REPORT_GROUP (ID)
)^

create unique index IDX_REPORT_REPORT_UNIQ_NAME on REPORT_REPORT (NAME)^

/**********************************************************************************************/

create table REPORT_TEMPLATE
(
  ID varchar(32),
  CREATE_TS datetime,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS datetime,
  UPDATED_BY varchar(50),
  --
  REPORT_ID varchar(32) not null,
  CODE varchar(50),
  OUTPUT_TYPE integer default 0 not null,
  IS_DEFAULT boolean default false,
  IS_CUSTOM boolean default false,
  CUSTOM_DEFINED_BY integer default 100,
  CUSTOM_CLASS text,
  OUTPUT_NAME_PATTERN varchar(255),
  NAME varchar(500),
  CONTENT blob,
  --
  primary key (ID),
  constraint FK_REPORT_TEMPLATE_TO_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID) on delete cascade
)^

/**********************************************************************************************/

alter table REPORT_REPORT add constraint FK_REPORT_REPORT_TO_DEF_TEMPLATE foreign key (DEFAULT_TEMPLATE_ID)
references REPORT_TEMPLATE (ID)^

insert into REPORT_GROUP (ID, CREATE_TS, CREATED_BY, VERSION, TITLE, CODE, LOCALE_NAMES)
values ('4e0835300b9c11e19b416bdaa41bff94', current_timestamp, 'admin', 0, 'General', 'ReportGroup.default',
concat('en=General','\n','ru=Общие'))^
