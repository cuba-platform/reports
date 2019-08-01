
create table REPORT_EXECUTION (
    ID uuid not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    --
    REPORT_ID uuid,
    REPORT_NAME varchar(255) not null,
    REPORT_CODE varchar(255),
    USER_ID uuid not null,
    START_TIME timestamp not null,
    FINISH_TIME timestamp,
    IS_SUCCESS boolean default false,
    CANCELLED boolean default false,
    PARAMS text,
    ERROR_MESSAGE text,
    SERVER_ID varchar(50),
    --
    primary key (ID),

    constraint FK_REPORT_EXECUTION_TO_REPORT foreign key (REPORT_ID) references REPORT_REPORT(ID) on delete set null,
    constraint FK_REPORT_EXECUTION_TO_USER foreign key (USER_ID) references SEC_USER(ID)
);

create index IDX_REPORT_EXECUTION_REPORT_ID on REPORT_EXECUTION (REPORT_ID);
create index IDX_REPORT_EXECUTION_START_TIME on REPORT_EXECUTION (START_TIME);
