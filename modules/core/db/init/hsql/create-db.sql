
create table REPORT_GROUP (
  ID varchar(36) not null,
  CREATE_TS timestamp,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS timestamp,
  UPDATED_BY varchar(50),

  TITLE varchar(255) not null,
  CODE varchar(255),
  LOCALE_NAMES varchar(7000),

  primary key (ID)
)^

----------------------------------------------------------------------------------------------------------------

create table REPORT_REPORT
(
  ID varchar(36) not null,
  CREATE_TS timestamp,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS timestamp,
  UPDATED_BY varchar(50),

  NAME varchar(255) not null,
  CODE varchar(255),
  LOCALE_NAMES varchar(7000),
  GROUP_ID varchar(36) not null,
  REPORT_TYPE integer,
  DEFAULT_TEMPLATE_ID varchar(36),
  XML longvarchar,

  primary key (ID),
  constraint FK_REPORT_REPORT_TO_REPORT_GROUP foreign key (GROUP_ID)
      references REPORT_GROUP (ID)
)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_TEMPLATE
(
  ID varchar(36) not null,
  CREATE_TS timestamp,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS timestamp,
  UPDATED_BY varchar(50),

  REPORT_ID varchar(36) not null,
  CODE varchar(50),
  OUTPUT_TYPE integer default 0 not null,
  IS_DEFAULT boolean default false,
  IS_CUSTOM boolean default false,
  CUSTOM_CLASS varchar(4000),
  OUTPUT_NAME_PATTERN varchar(255),
  NAME varchar(500),
  CONTENT longvarbinary,

  primary key (ID),
  constraint FK_REPORT_TEMPLATE_TO_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID) on delete cascade
)^

--------------------------------------------------------------------------------------------------------------

alter table REPORT_REPORT add constraint FK_REPORT_REPORT_TO_DEF_TEMPLATE foreign key (DEFAULT_TEMPLATE_ID)
references REPORT_TEMPLATE (ID);

insert into REPORT_GROUP (ID, CREATE_TS, CREATED_BY, VERSION, TITLE, CODE, LOCALE_NAMES)
values ('4e083530-0b9c-11e1-9b41-6bdaa41bff94', now(), 'admin', 0, 'General', 'ReportGroup.default',
'en=General
ru=Общие')^