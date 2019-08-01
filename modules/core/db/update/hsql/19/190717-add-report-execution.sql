
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
  --
  primary key (ID),

  constraint FK_REPORT_EXECUTION_TO_REPORT foreign key (REPORT_ID) references REPORT_REPORT(ID) on delete set null,
  constraint FK_REPORT_EXECUTION_TO_USER foreign key (USER_ID) references SEC_USER(ID)
)^

create index IDX_REPORT_EXECUTION_REPORT_ID on REPORT_EXECUTION (REPORT_ID);
create index IDX_REPORT_EXECUTION_START_TIME on REPORT_EXECUTION (START_TIME)^
