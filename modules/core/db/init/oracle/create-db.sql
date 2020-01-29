
create table REPORT_GROUP (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50 char),
    VERSION integer default 1 not null,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50 char),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50 char),
    TITLE varchar2(255 char) not null,
    CODE varchar2(255 char),
    LOCALE_NAMES clob,
    SYS_TENANT_ID varchar2(255 char),
    primary key(ID)
)^

create unique index IDX_REPORT_GROUP_UNIQ_TITLE on REPORT_GROUP(TITLE, SYS_TENANT_ID, DELETE_TS)^

create table REPORT_REPORT (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50 char),
    VERSION integer default 1 not null,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50 char),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50 char),
    NAME varchar2(255 char) not null,
    CODE varchar2(255 char),
    DESCRIPTION varchar2(500 char),
    LOCALE_NAMES clob,
    GROUP_ID varchar2(32) not null,
    DEFAULT_TEMPLATE_ID varchar2(32),
    XML clob,
    ROLES_IDX varchar2(1000 char),
    SCREENS_IDX varchar2(1000 char),
    INPUT_ENTITY_TYPES_IDX varchar2(1000 char),
    REST_ACCESS char(1),
    IS_SYSTEM char(1),
    SYS_TENANT_ID varchar2(255 char),

    REPORT_TYPE integer,
    primary key(ID)
)^

create unique index IDX_REPORT_REPORT_UNIQ_NAME on REPORT_REPORT(NAME, SYS_TENANT_ID, DELETE_TS)^

create table REPORT_TEMPLATE (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50 char),
    VERSION integer default 1 not null,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50 char),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50 char),
    REPORT_ID varchar2(32) not null,
    CODE varchar2(50 char),
    OUTPUT_TYPE integer not null,
    IS_DEFAULT char(1),
    IS_GROOVY char(1),
    IS_CUSTOM char(1),
    IS_ALTERABLE_OUTPUT char(1),
    CUSTOM_DEFINED_BY integer default 100,
    CUSTOM_CLASS varchar2(4000),
    OUTPUT_NAME_PATTERN varchar2(255 char),
    NAME varchar2(500 char),
    CONTENT blob,

    primary key(ID)
)^

alter table REPORT_REPORT add constraint FK_REPORT_REPORT_TO_REPORT_GRO foreign key (GROUP_ID) references REPORT_GROUP(ID)^
alter table REPORT_REPORT add constraint FK_REPORT_TO_DEF_TEMPLATE foreign key (DEFAULT_TEMPLATE_ID) references REPORT_TEMPLATE(ID)^

alter table REPORT_TEMPLATE add constraint FK_REPORT_TEMPLATE_TO_REPORT foreign key (REPORT_ID) references REPORT_REPORT(ID)^

--------------------------------------------------------------------------------------------------------------

create table REPORT_EXECUTION (
  ID varchar2(32) not null,
  CREATE_TS timestamp,
  CREATED_BY varchar2(50 char),
  --
  REPORT_ID varchar2(32),
  REPORT_NAME varchar2(255 char) not null,
  REPORT_CODE varchar2(255 char),
  USER_ID varchar2(32) not null,
  START_TIME timestamp not null,
  FINISH_TIME timestamp,
  IS_SUCCESS char(1),
  CANCELLED char(1),
  PARAMS clob,
  ERROR_MESSAGE clob,
  SERVER_ID varchar2(50 char),
  OUTPUT_DOCUMENT_ID varchar2(32),
  --
  primary key (ID),

  constraint FK_REPORT_EXECUTION_TO_REPORT foreign key (REPORT_ID) references REPORT_REPORT(ID) on delete set null,
  constraint FK_REPORT_EXECUTION_TO_USER foreign key (USER_ID) references SEC_USER(ID),
  constraint FK_REPORT_EXECUTION_TO_DOCUMEN foreign key (OUTPUT_DOCUMENT_ID) references SYS_FILE(ID)
)^

create index IDX_REPORT_EXECUTION_REPORT_ID on REPORT_EXECUTION (REPORT_ID)^
create index IDX_REPORT_EXECUTION_START_TIM on REPORT_EXECUTION (START_TIME)^

--------------------------------------------------------------------------------------------------------------

insert into REPORT_GROUP (ID, CREATE_TS, CREATED_BY, VERSION, TITLE, CODE, LOCALE_NAMES)
values ('4e0835300b9c11e19b416bdaa41bff94', current_timestamp, 'admin', 0, 'General', 'ReportGroup.default',
'en=General
ru=Общие'
)^
