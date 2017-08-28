alter table REPORT_REPORT add DELETE_TS timestamp^
alter table REPORT_REPORT add DELETED_BY varchar(50)^

alter table REPORT_GROUP add DELETE_TS timestamp^
alter table REPORT_GROUP add DELETED_BY varchar(50)^

alter table REPORT_TEMPLATE add DELETE_TS timestamp^
alter table REPORT_TEMPLATE add DELETED_BY varchar(50)^

alter table REPORT_GROUP drop constraint REPORT_GROUP_UNIQ_TITLE^
alter table REPORT_GROUP add constraint REPORT_GROUP_UNIQ_TITLE unique (TITLE, DELETE_TS)^

alter table REPORT_REPORT drop constraint REPORT_REPORT_UNIQ_NAME^
alter table REPORT_REPORT add constraint REPORT_REPORT_UNIQ_NAME unique (NAME, DELETE_TS)^
