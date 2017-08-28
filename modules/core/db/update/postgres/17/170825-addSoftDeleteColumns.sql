alter table REPORT_REPORT add column DELETE_TS timestamp^
alter table REPORT_REPORT add column DELETED_BY varchar(50)^

alter table REPORT_GROUP add column DELETE_TS timestamp^
alter table REPORT_GROUP add column DELETED_BY varchar(50)^

alter table REPORT_TEMPLATE add column DELETE_TS timestamp^
alter table REPORT_TEMPLATE add column DELETED_BY varchar(50)^

drop index IDX_REPORT_REPORT_UNIQ_NAME^
create unique index IDX_REPORT_REPORT_UNIQ_NAME on REPORT_REPORT (NAME) where DELETE_TS is null^

drop index IDX_REPORT_GROUP_UNIQ_TITLE^
create unique index IDX_REPORT_GROUP_UNIQ_TITLE on REPORT_GROUP (TITLE) where DELETE_TS is null^
