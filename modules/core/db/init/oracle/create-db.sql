--create table REPORT_BAND_DEFINITION (
--    ID varchar2(32) not null,
--    CREATE_TS timestamp,
--    CREATED_BY varchar2(50),
--    VERSION integer,
--    UPDATE_TS timestamp,
--    UPDATED_BY varchar2(50),
--    PARENT_DEFINITION_ID varchar2(32),
--    REPORT_ID varchar2(32) not null,
--    NAME varchar2(255) not null,
--    ORIENTATION integer not null,
--    POSITION_ integer not null,
--    primary key(ID)
--)^
--
--create table REPORT_DATA_SET (
--    ID varchar2(32) not null,
--    CREATE_TS timestamp,
--    CREATED_BY varchar2(50),
--    VERSION integer,
--    UPDATE_TS timestamp,
--    UPDATED_BY varchar2(50),
--    NAME varchar2(255) not null,
--    TEXT clob,
--    DATA_SET_TYPE integer not null,
--    BAND_DEFINITION varchar2(32) not null,
--    ENTITY_PARAM_NAME varchar2(255),
--    LIST_ENTITIES_PARAM_NAME varchar2(255),
--    primary key(ID)
--)^

create table REPORT_GROUP (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    TITLE varchar2(255) not null,
    CODE varchar2(255),
    LOCALE_NAMES clob,
    primary key(ID)
)^

--create table REPORT_INPUT_PARAMETER (
--    ID varchar2(32) not null,
--    CREATE_TS timestamp,
--    CREATED_BY varchar2(50),
--    VERSION integer,
--    UPDATE_TS timestamp,
--    UPDATED_BY varchar2(50),
--    REPORT_ID varchar2(32) not null,
--    PARAMETER_TYPE integer not null,
--    NAME varchar2(255) not null,
--    LOCALE_NAMES clob,
--    ALIAS varchar2(100),
--    SCREEN varchar2(255),
--    FROM_BROWSER char(1),
--    REQUIRED char(1),
--    POSITION_ integer,
--    META_CLASS varchar2(255),
--    ENUM_CLASS varchar2(500),
--    primary key(ID)
--)^

create table REPORT_REPORT (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    NAME varchar2(255) not null,
    CODE varchar2(255),
    LOCALE_NAMES clob,
    GROUP_ID varchar2(32) not null,
    DEFAULT_TEMPLATE_ID varchar2(32),
    XML clob,

    REPORT_TYPE integer,
    primary key(ID)
)^
--create unique index UK_REPORT_REPORT_CODE on REPORT_REPORT(CODE)^

--create table REPORT_REPORT_SCREEN (
--    ID varchar2(32) not null,
--    CREATE_TS timestamp,
--    CREATED_BY varchar2(50),
--    VERSION integer,
--    UPDATE_TS timestamp,
--    UPDATED_BY varchar2(50),
--    REPORT_ID varchar2(32) not null,
--    SCREEN_ID varchar2(255) not null,
--    primary key(ID)
--)^

--create table REPORT_REPORTS_ROLES (
--    REPORT_ID varchar2(32) not null,
--    ROLE_ID varchar2(32) not null
--    )^

create table REPORT_TEMPLATE (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    REPORT_ID varchar2(32) not null,
    CODE varchar2(50),
    OUTPUT_TYPE integer not null,
    IS_DEFAULT char(1),
    IS_CUSTOM char(1),
    CUSTOM_DEFINED_BY integer default 100,
    CUSTOM_CLASS varchar2(4000),
    OUTPUT_NAME_PATTERN varchar2(255),
    NAME varchar2(500),
    CONTENT blob,

    primary key(ID)
)^

--create table REPORT_VALUE_FORMAT (
--    ID varchar2(32) not null,
--    CREATE_TS timestamp,
--    CREATED_BY varchar2(50),
--    VERSION integer,
--    UPDATE_TS timestamp,
--    UPDATED_BY varchar2(50),
--    REPORT_ID varchar2(32) not null,
--    NAME varchar2(255) not null,
--    FORMAT varchar2(255) not null,
--    primary key(ID)
--)^

--alter table REPORT_BAND_DEFINITION add constraint FK_REP_BAN_DEF_TO_REP_BAN_DEF foreign key (PARENT_DEFINITION_ID) references REPORT_BAND_DEFINITION(ID)^
--alter table REPORT_BAND_DEFINITION add constraint FK_REPORT_BAND_DEF_TO_REP_REP foreign key (REPORT_ID) references REPORT_REPORT(ID)^
--
--alter table REPORT_DATA_SET add constraint FK_REP_DAT_SET_TO_REP_BAN_DEF foreign key (BAND_DEFINITION) references REPORT_BAND_DEFINITION(ID)^
--
--alter table REPORT_INPUT_PARAMETER add constraint FK_REPORT_INPUT_PAR_TO_REP_REP foreign key (REPORT_ID) references REPORT_REPORT(ID)^

alter table REPORT_REPORT add constraint FK_REPORT_REPORT_TO_REPORT_GRO foreign key (GROUP_ID) references REPORT_GROUP(ID)^
alter table REPORT_REPORT add constraint FK_REPORT_TO_DEF_TEMPLATE foreign key (DEFAULT_TEMPLATE_ID) references REPORT_TEMPLATE(ID)^

--alter table REPORT_REPORT_SCREEN add constraint FK_REPORT_REP_SCR_TO_REP_REP foreign key (REPORT_ID) references REPORT_REPORT(ID)^
--
--alter table REPORT_REPORTS_ROLES add constraint FK_REPORT_REPORTS_ROLES_TO_REP foreign key (REPORT_ID) references REPORT_REPORT(ID)^
--alter table REPORT_REPORTS_ROLES add constraint FK_REPORT_REPORTS_ROLES_TO_ROL foreign key (ROLE_ID) references SEC_ROLE(ID)^

alter table REPORT_TEMPLATE add constraint FK_REPORT_TEMPLATE_TO_REPORT foreign key (REPORT_ID) references REPORT_REPORT(ID)^

--alter table REPORT_VALUE_FORMAT add constraint FK_REPORT_VALUE_FOR_TO_REP_REP foreign key (REPORT_ID) references REPORT_REPORT(ID)^

--------------------------------------------------------------------------------------------------------------

insert into REPORT_GROUP (ID, CREATE_TS, CREATED_BY, VERSION, TITLE, CODE, LOCALE_NAMES)
values ('4e0835300b9c11e19b416bdaa41bff94', current_timestamp, 'admin', 0, 'General', 'ReportGroup.default',
'en=General
ru=Общие'
)^
