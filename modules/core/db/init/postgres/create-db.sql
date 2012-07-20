
create table REPORT_BAND_DEFINITION
(
  ID uuid not null,
  CREATE_TS timestamp,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS timestamp,
  UPDATED_BY varchar(50),
  --
  QUERY varchar(255),
  PARENT_DEFINITION_ID uuid,
  REPORT_ID uuid,
  NAME varchar(255),
  ORIENTATION integer default 0,
  POSITION_ integer default 0,
  --
  primary key (ID),
  constraint FK_REPORT_BAND_DEFINITION_TO_REPORT_BAND_DEFINITION foreign key (PARENT_DEFINITION_ID)
      references REPORT_BAND_DEFINITION (ID)
)^

--------------------------------------------------------------------------------------------------------------

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
  NAME varchar(255),
  CODE varchar(255),
  LOCALE_NAMES text,
  GROUP_ID uuid not null,
  ROOT_DEFINITION_ID uuid,
  REPORT_TYPE integer,
  --
  primary key (ID),
  constraint FK_REPORT_REPORT_TO_REPORT_BAND_DEFINITION foreign key (ROOT_DEFINITION_ID)
      references REPORT_BAND_DEFINITION (ID),
  constraint FK_REPORT_REPORT_TO_REPORT_GROUP foreign key (GROUP_ID)
      references REPORT_GROUP (ID)
)^

alter table REPORT_BAND_DEFINITION add constraint FK_REPORT_BAND_DEFINITION_TO_REPORT_REPORT
foreign key (REPORT_ID) references REPORT_REPORT (ID)^

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
  REPORT_ID uuid,
  CODE varchar(50),
  TEMPLATE_FILE_ID uuid,
  OUTPUT_TYPE integer default 0,
  IS_DEFAULT boolean default false,
  IS_CUSTOM boolean default false,
  CUSTOM_CLASS varchar,
  --
  primary key (ID),
  constraint FK_REPORT_TEMPLATE_TO_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID)
)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_INPUT_PARAMETER
(
  ID uuid not null,
  CREATE_TS timestamp,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS timestamp,
  UPDATED_BY varchar(50),
  --
  REPORT_ID uuid,
  TYPE integer,
  NAME varchar(255),
  LOCALE_NAMES text,
  ALIAS varchar(100),
  SCREEN varchar(255),
  FROM_BROWSER boolean,
  REQUIRED boolean default false,
  POSITION_ integer default 0,
  META_CLASS varchar(255),
  ENUM_CLASS varchar(500),
  --
  primary key (ID),
  constraint FK_REPOR_INPUT_PARAMETER_TO_REPORT_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID)
)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_DATA_SET
(
  ID uuid not null,
  CREATE_TS timestamp,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS timestamp,
  UPDATED_BY varchar(50),
  --
  NAME varchar(255),
  TEXT text,
  TYPE integer,
  BAND_DEFINITION uuid,
  ENTITY_PARAM_NAME varchar(255),
  LIST_ENTITIES_PARAM_NAME varchar(255),
  --
  primary key (ID),
  constraint FK_REPORT_DATA_SET_TO_REPORT_BAND_DEFINITION foreign key (BAND_DEFINITION)
      references REPORT_BAND_DEFINITION (ID)
)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_REPORTS_ROLES (
  REPORT_ID uuid not null,
  ROLE_ID uuid not null,
  --
  constraint FK_REPORT_REPORTS_ROLES_TO_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT(ID),
  constraint FK_REPORT_REPORTS_ROLES_TO_ROLE foreign key (ROLE_ID)
      references SEC_ROLE(ID)
)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_REPORT_SCREEN (
  ID uuid not null,
  CREATE_TS timestamp,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS timestamp,
  UPDATED_BY varchar(50),
  --
  REPORT_ID uuid,
  SCREEN_ID varchar(255),
  --
  primary key (ID),
  constraint FK_REPORT_REPORT_SCREEN_TO_REPORT_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID)
)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_VALUE_FORMAT (
  ID uuid not null,
  CREATE_TS timestamp,
  CREATED_BY varchar(50),
  VERSION integer,
  UPDATE_TS timestamp,
  UPDATED_BY varchar(50),
  --
  REPORT_ID uuid,
  NAME varchar(255),
  FORMAT varchar(255),
  --
  primary key (ID),
  constraint FK_REPORT_VALUE_FORMAT_TO_REPORT_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID)
)^

--------------------------------------------------------------------------------------------------------------

insert into REPORT_GROUP (ID, CREATE_TS, CREATED_BY, VERSION, TITLE, CODE, LOCALE_NAMES)
values ('4e083530-0b9c-11e1-9b41-6bdaa41bff94', now(), 'admin', 0, 'General', 'ReportGroup.default',
E'en=General \nru=Общие')^
