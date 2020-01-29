alter table REPORT_GROUP add SYS_TENANT_ID varchar(255)^
alter table REPORT_REPORT add SYS_TENANT_ID varchar(255)^

alter table REPORT_GROUP drop constraint REPORT_GROUP_UNIQ_TITLE^
alter table REPORT_GROUP add constraint REPORT_GROUP_UNIQ_TITLE unique (TITLE, SYS_TENANT_ID, DELETE_TS)^

alter table REPORT_REPORT drop constraint REPORT_REPORT_UNIQ_NAME^
alter table REPORT_REPORT add constraint REPORT_REPORT_UNIQ_NAME unique (NAME, SYS_TENANT_ID, DELETE_TS)^