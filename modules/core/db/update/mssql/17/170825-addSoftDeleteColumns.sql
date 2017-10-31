alter table REPORT_REPORT add DELETE_TS datetime^
alter table REPORT_REPORT add DELETED_BY varchar(50)^

alter table REPORT_GROUP add DELETE_TS datetime^
alter table REPORT_GROUP add DELETED_BY varchar(50)^

alter table REPORT_TEMPLATE add DELETE_TS datetime^
alter table REPORT_TEMPLATE add DELETED_BY varchar(50)^

if exists (select * from sys.indexes where name='IDX_REPORT_REPORT_UNIQ_NAME' AND object_id = OBJECT_ID('REPORT_REPORT'))
begin
  drop index IDX_REPORT_REPORT_UNIQ_NAME on REPORT_REPORT;
  create unique index IDX_REPORT_REPORT_UNIQ_NAME on REPORT_REPORT (NAME, DELETE_TS);
end^

if exists (select * from sys.indexes where name='IDX_REPORT_GROUP_UNIQ_TITLE' AND object_id = OBJECT_ID('REPORT_GROUP'))
begin
  drop index IDX_REPORT_GROUP_UNIQ_TITLE on REPORT_GROUP;
  create unique index IDX_REPORT_GROUP_UNIQ_TITLE on REPORT_GROUP (TITLE, DELETE_TS);
end^