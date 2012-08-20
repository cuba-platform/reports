
create table REPORT_GROUP (
  ID uniqueidentifier not null,
  CREATE_TS datetime,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS datetime,
  UPDATED_BY varchar(50),
  --
  TITLE varchar(255) not null,
  CODE varchar(255),
  LOCALE_NAMES varchar(1000),
  --
  primary key (ID)
)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_REPORT
(
  ID uniqueidentifier not null,
  CREATE_TS datetime,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS datetime,
  UPDATED_BY varchar(50),
  --
  NAME varchar(255),
  CODE varchar(255),
  LOCALE_NAMES varchar(1000),
  GROUP_ID uniqueidentifier not null,
  REPORT_TYPE integer,
  --
  primary key (ID),
  constraint FK_REPORT_REPORT_TO_REPORT_GROUP foreign key (GROUP_ID)
      references REPORT_GROUP (ID)
)^

create unique index UK_REPORT_REPORT_CODE on REPORT_REPORT (CODE) where CODE is not null^

--------------------------------------------------------------------------------------------------------------

create table REPORT_BAND_DEFINITION
(
  ID uniqueidentifier not null,
  CREATE_TS datetime,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS datetime,
  UPDATED_BY varchar(50),
  --
  PARENT_DEFINITION_ID uniqueidentifier,
  REPORT_ID uniqueidentifier,
  NAME varchar(255),
  ORIENTATION integer default 0,
  POSITION_ integer default 0,
  --
  primary key (ID),
  constraint FK_REPORT_BAND_DEFINITION_TO_REPORT_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID) on delete cascade,
  constraint FK_REPORT_BAND_DEFINITION_TO_REPORT_BAND_DEFINITION foreign key (PARENT_DEFINITION_ID)
      references REPORT_BAND_DEFINITION (ID)
)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_TEMPLATE
(
  ID uniqueidentifier not null,
  CREATE_TS datetime,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS datetime,
  UPDATED_BY varchar(50),
  --
  REPORT_ID uniqueidentifier,
  CODE varchar(50),
  TEMPLATE_FILE_ID uniqueidentifier,
  OUTPUT_TYPE integer default 0,
  IS_DEFAULT tinyint default 0,
  IS_CUSTOM tinyint default 0,
  CUSTOM_CLASS varchar(max),
  --
  primary key (ID),
  constraint FK_REPORT_TEMPLATE_TO_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID) on delete cascade
)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_INPUT_PARAMETER
(
  ID uniqueidentifier not null,
  CREATE_TS datetime,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS datetime,
  UPDATED_BY varchar(50),
  --
  REPORT_ID uniqueidentifier,
  TYPE integer,
  NAME varchar(255),
  LOCALE_NAMES varchar(1000),
  ALIAS varchar(100),
  SCREEN varchar(255),
  FROM_BROWSER tinyint,
  REQUIRED tinyint default 0,
  POSITION_ integer default 0,
  META_CLASS varchar(255),
  ENUM_CLASS varchar(500),
  --
  primary key (ID),
  constraint FK_REPORT_INPUT_PARAMETER_TO_REPORT_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID) on delete cascade
)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_DATA_SET
(
  ID uniqueidentifier not null,
  CREATE_TS datetime,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS datetime,
  UPDATED_BY varchar(50),
  --
  NAME varchar(255),
  TEXT varchar(max),
  TYPE integer,
  BAND_DEFINITION uniqueidentifier,
  ENTITY_PARAM_NAME varchar(255),
  LIST_ENTITIES_PARAM_NAME varchar(255),
  --
  primary key (ID),
  constraint FK_REPORT_DATA_SET_TO_REPORT_BAND_DEFINITION foreign key (BAND_DEFINITION)
      references REPORT_BAND_DEFINITION (ID) on delete cascade
)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_REPORTS_ROLES (
  REPORT_ID uniqueidentifier not null,
  ROLE_ID uniqueidentifier not null,
  --
  constraint FK_REPORT_REPORTS_ROLES_TO_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT(ID) on delete cascade,
  constraint FK_REPORT_REPORTS_ROLES_TO_ROLE foreign key (ROLE_ID)
      references SEC_ROLE(ID)
)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_REPORT_SCREEN (
  ID uniqueidentifier not null,
  CREATE_TS datetime,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS datetime,
  UPDATED_BY varchar(50),
  --
  REPORT_ID uniqueidentifier,
  SCREEN_ID varchar(255),
  --
  primary key (ID),
  constraint FK_REPORT_REPORT_SCREEN_TO_REPORT_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID) on delete cascade
)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_VALUE_FORMAT (
  ID uniqueidentifier not null,
  CREATE_TS datetime,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS datetime,
  UPDATED_BY varchar(50),
  --
  REPORT_ID uniqueidentifier,
  NAME varchar(255),
  FORMAT varchar(255),
  --
  primary key (ID),
  constraint FK_REPORT_VALUE_FORMAT_TO_REPORT_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID) on delete cascade
)^

--------------------------------------------------------------------------------------------------------------

insert into REPORT_GROUP (ID, CREATE_TS, CREATED_BY, VERSION, TITLE, CODE, LOCALE_NAMES)
values ('4e083530-0b9c-11e1-9b41-6bdaa41bff94', current_timestamp, 'admin', 0, 'General', 'ReportGroup.default',
'en=General'+char(10)+'ru=Общие')^
