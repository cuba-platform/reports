
create table REPORT_GROUP (
  ID uniqueidentifier not null,
  CREATE_TS datetime,
  CREATED_BY varchar(50),
  VERSION integer not null default 1,
  UPDATE_TS datetime,
  UPDATED_BY varchar(50),
  DELETE_TS datetime,
  DELETED_BY varchar(50),
  SYS_TENANT_ID varchar(255),
  --
  TITLE varchar(255) not null,
  CODE varchar(255),
  LOCALE_NAMES varchar(1000),
  --
  primary key (ID)
)^

create unique index IDX_REPORT_GROUP_UNIQ_TITLE on REPORT_GROUP (TITLE, SYS_TENANT_ID, DELETE_TS)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_REPORT
(
  ID uniqueidentifier not null,
  CREATE_TS datetime,
  CREATED_BY varchar(50),
  VERSION integer not null default 1,
  UPDATE_TS datetime,
  UPDATED_BY varchar(50),
  DELETE_TS datetime,
  DELETED_BY varchar(50),
  SYS_TENANT_ID varchar(255),
  --
  NAME varchar(255) not null,
  CODE varchar(255),
  DESCRIPTION varchar(500),
  LOCALE_NAMES varchar(1000),
  GROUP_ID uniqueidentifier not null,
  REPORT_TYPE integer,
  DEFAULT_TEMPLATE_ID uniqueidentifier,
  XML varchar(max),
  ROLES_IDX varchar(1000),
  SCREENS_IDX varchar(1000),
  INPUT_ENTITY_TYPES_IDX varchar(1000),
  REST_ACCESS tinyint default 0,
  IS_SYSTEM tinyint default 0,
  --
  primary key (ID),
  constraint FK_REPORT_REPORT_TO_REPORT_GROUP foreign key (GROUP_ID)
      references REPORT_GROUP (ID)
)^

create unique index IDX_REPORT_REPORT_UNIQ_NAME on REPORT_REPORT (NAME, SYS_TENANT_ID, DELETE_TS)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_TEMPLATE
(
  ID uniqueidentifier not null,
  CREATE_TS datetime,
  CREATED_BY varchar(50),
  VERSION integer not null default 1,
  UPDATE_TS datetime,
  UPDATED_BY varchar(50),
  DELETE_TS datetime,
  DELETED_BY varchar(50),
  --
  REPORT_ID uniqueidentifier not null,
  CODE varchar(50),
  OUTPUT_TYPE integer default 0 not null,
  IS_DEFAULT tinyint default 0,
  IS_GROOVY tinyint default 0,
  IS_CUSTOM tinyint default 0,
  IS_ALTERABLE_OUTPUT tinyint default 0,
  CUSTOM_DEFINED_BY integer default 100,
  CUSTOM_CLASS varchar(max),
  OUTPUT_NAME_PATTERN varchar(255),
  NAME varchar(500),
  CONTENT image,

  --
  primary key (ID),
  constraint FK_REPORT_TEMPLATE_TO_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID) on delete cascade
)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_EXECUTION (
  ID uniqueidentifier not null,
  CREATE_TS datetime,
  CREATED_BY varchar(50),
  --
  REPORT_ID uniqueidentifier,
  REPORT_NAME varchar(255) not null,
  REPORT_CODE varchar(255),
  USER_ID uniqueidentifier not null,
  START_TIME datetime not null,
  FINISH_TIME datetime,
  IS_SUCCESS tinyint default 0,
  CANCELLED tinyint default 0,
  PARAMS varchar(max),
  ERROR_MESSAGE varchar(max),
  SERVER_ID varchar(50),
  OUTPUT_DOCUMENT_ID uniqueidentifier,
  --
  primary key (ID),

  constraint FK_REPORT_EXECUTION_TO_REPORT foreign key (REPORT_ID) references REPORT_REPORT(ID) on delete set null,
  constraint FK_REPORT_EXECUTION_TO_USER foreign key (USER_ID) references SEC_USER(ID),
  constraint FK_REPORT_EXECUTION_TO_DOCUMENT foreign key (OUTPUT_DOCUMENT_ID) references SYS_FILE(ID)
)^

create index IDX_REPORT_EXECUTION_REPORT_ID on REPORT_EXECUTION (REPORT_ID);
create index IDX_REPORT_EXECUTION_START_TIME on REPORT_EXECUTION (START_TIME)^

--------------------------------------------------------------------------------------------------------------
alter table REPORT_REPORT add constraint FK_REPORT_REPORT_TO_DEF_TEMPLATE foreign key (DEFAULT_TEMPLATE_ID)
references REPORT_TEMPLATE (ID);

insert into REPORT_GROUP (ID, CREATE_TS, CREATED_BY, VERSION, TITLE, CODE, LOCALE_NAMES)
values ('4e083530-0b9c-11e1-9b41-6bdaa41bff94', current_timestamp, 'admin', 0, 'General', 'ReportGroup.default',
'en=General'+char(10)+'ru=Общие')^
