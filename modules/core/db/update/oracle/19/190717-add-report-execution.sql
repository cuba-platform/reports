
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
  --
  primary key (ID),

  constraint FK_REPORT_EXECUTION_TO_REPORT foreign key (REPORT_ID) references REPORT_REPORT(ID) on delete set null,
  constraint FK_REPORT_EXECUTION_TO_USER foreign key (USER_ID) references SEC_USER(ID)
)^

create index IDX_REPORT_EXECUTION_REPORT_ID on REPORT_EXECUTION (REPORT_ID)^
create index IDX_REPORT_EXECUTION_START_TIM on REPORT_EXECUTION (START_TIME)^
