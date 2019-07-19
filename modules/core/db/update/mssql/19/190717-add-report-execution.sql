
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
  --
  primary key (ID),

  constraint FK_REPORT_EXECUTION_TO_REPORT foreign key (REPORT_ID) references REPORT_REPORT(ID) on delete set null,
  constraint FK_REPORT_EXECUTION_TO_USER foreign key (USER_ID) references SEC_USER(ID)
)^

create index IDX_REPORT_EXECUTION_REPORT_ID on REPORT_EXECUTION (REPORT_ID);
create index IDX_REPORT_EXECUTION_START_TIME on REPORT_EXECUTION (START_TIME)^
