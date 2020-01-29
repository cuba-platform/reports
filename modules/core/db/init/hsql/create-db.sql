
create table REPORT_GROUP (
  ID varchar(36) not null,
  CREATE_TS timestamp,
  CREATED_BY varchar(50),
  VERSION integer default 1 not null,
  UPDATE_TS timestamp,
  UPDATED_BY varchar(50),
  DELETE_TS timestamp,
  DELETED_BY varchar(50),
  SYS_TENANT_ID varchar(255),

  TITLE varchar(255) not null,
  CODE varchar(255),
  LOCALE_NAMES varchar(7000),

  primary key (ID)
)^

alter table REPORT_GROUP add constraint REPORT_GROUP_UNIQ_TITLE unique (TITLE, SYS_TENANT_ID, DELETE_TS)^

----------------------------------------------------------------------------------------------------------------

create table REPORT_REPORT
(
  ID varchar(36) not null,
  CREATE_TS timestamp,
  CREATED_BY varchar(50),
  VERSION integer default 1 not null,
  UPDATE_TS timestamp,
  UPDATED_BY varchar(50),
  DELETE_TS timestamp,
  DELETED_BY varchar(50),

  NAME varchar(255) not null,
  CODE varchar(255),
  DESCRIPTION varchar(500),
  LOCALE_NAMES varchar(7000),
  GROUP_ID varchar(36) not null,
  REPORT_TYPE integer,
  DEFAULT_TEMPLATE_ID varchar(36),
  XML longvarchar,
  ROLES_IDX varchar(1000),
  SCREENS_IDX varchar(1000),
  INPUT_ENTITY_TYPES_IDX varchar(1000),
  REST_ACCESS boolean default false,
  IS_SYSTEM boolean default false,
  SYS_TENANT_ID varchar(255),

  primary key (ID),
  constraint FK_REPORT_REPORT_TO_REPORT_GROUP foreign key (GROUP_ID)
      references REPORT_GROUP (ID)
)^

alter table REPORT_REPORT add constraint REPORT_REPORT_UNIQ_NAME unique (NAME, SYS_TENANT_ID, DELETE_TS)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_TEMPLATE
(
  ID varchar(36) not null,
  CREATE_TS timestamp,
  CREATED_BY varchar(50),
  VERSION integer default 1 not null,
  UPDATE_TS timestamp,
  UPDATED_BY varchar(50),
  DELETE_TS timestamp,
  DELETED_BY varchar(50),

  REPORT_ID varchar(36) not null,
  CODE varchar(50),
  OUTPUT_TYPE integer default 0 not null,
  IS_DEFAULT boolean default false,
  IS_GROOVY boolean default false,
  IS_CUSTOM boolean default false,
  IS_ALTERABLE_OUTPUT boolean default false,
  CUSTOM_DEFINED_BY integer default 100,
  CUSTOM_CLASS varchar(4000),
  OUTPUT_NAME_PATTERN varchar(255),
  NAME varchar(500),
  CONTENT longvarbinary,

  primary key (ID),
  constraint FK_REPORT_TEMPLATE_TO_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID) on delete cascade
)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_EXECUTION (
  ID varchar(36) not null,
  CREATE_TS timestamp,
  CREATED_BY varchar(50),
  --
  REPORT_ID varchar(36),
  REPORT_NAME varchar(255) not null,
  REPORT_CODE varchar(255),
  USER_ID varchar(36) not null,
  START_TIME timestamp not null,
  FINISH_TIME timestamp,
  IS_SUCCESS boolean default false,
  CANCELLED boolean default false,
  PARAMS longvarchar,
  ERROR_MESSAGE longvarchar,
  SERVER_ID varchar(50),
  OUTPUT_DOCUMENT_ID varchar(36),
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
values ('4e083530-0b9c-11e1-9b41-6bdaa41bff94', now(), 'admin', 0, 'General', 'ReportGroup.default',
'en=General
ru=Общие')^