
create table REPORT_GROUP (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    TITLE varchar2(255) not null,
    CODE varchar2(255),
    LOCALE_NAMES clob,
    primary key(ID)
)^

create unique index IDX_REPORT_GROUP_UNIQ_TITLE on REPORT_GROUP(TITLE, DELETE_TS)^

create table REPORT_REPORT (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    NAME varchar2(255) not null,
    CODE varchar2(255),
    DESCRIPTION varchar2(500),
    LOCALE_NAMES clob,
    GROUP_ID varchar2(32) not null,
    DEFAULT_TEMPLATE_ID varchar2(32),
    XML clob,
    ROLES_IDX varchar2(1000),
    SCREENS_IDX varchar2(1000),
    INPUT_ENTITY_TYPES_IDX varchar2(1000),
    REST_ACCESS char(1),
    IS_SYSTEM char(1),

    REPORT_TYPE integer,
    primary key(ID)
)^

create unique index IDX_REPORT_REPORT_UNIQ_NAME on REPORT_REPORT(NAME, DELETE_TS)^

create table REPORT_TEMPLATE (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    REPORT_ID varchar2(32) not null,
    CODE varchar2(50),
    OUTPUT_TYPE integer not null,
    IS_DEFAULT char(1),
    IS_GROOVY char(1),
    IS_CUSTOM char(1),
    IS_ALTERABLE_OUTPUT char(1),
    CUSTOM_DEFINED_BY integer default 100,
    CUSTOM_CLASS varchar2(4000),
    OUTPUT_NAME_PATTERN varchar2(255),
    NAME varchar2(500),
    CONTENT blob,

    primary key(ID)
)^

alter table REPORT_REPORT add constraint FK_REPORT_REPORT_TO_REPORT_GRO foreign key (GROUP_ID) references REPORT_GROUP(ID)^
alter table REPORT_REPORT add constraint FK_REPORT_TO_DEF_TEMPLATE foreign key (DEFAULT_TEMPLATE_ID) references REPORT_TEMPLATE(ID)^

alter table REPORT_TEMPLATE add constraint FK_REPORT_TEMPLATE_TO_REPORT foreign key (REPORT_ID) references REPORT_REPORT(ID)^

--------------------------------------------------------------------------------------------------------------

insert into REPORT_GROUP (ID, CREATE_TS, CREATED_BY, VERSION, TITLE, CODE, LOCALE_NAMES)
values ('4e0835300b9c11e19b416bdaa41bff94', current_timestamp, 'admin', 0, 'General', 'ReportGroup.default',
'en=General
ru=Общие'
)^
