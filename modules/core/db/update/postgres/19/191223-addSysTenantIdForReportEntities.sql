alter table REPORT_GROUP add SYS_TENANT_ID varchar(255)^
alter table REPORT_REPORT add SYS_TENANT_ID varchar(255)^

create unique index IDX_REPORT_GROUP_UNIQ_TITLE on REPORT_GROUP (TITLE) where DELETE_TS is null and SYS_TENANT_ID is null^
create unique index IDX_REPORT_GROUP_UNIQ_TITLE_SYS_TENANT_ID_NN on REPORT_GROUP (TITLE, SYS_TENANT_ID)
    where DELETE_TS is null and SYS_TENANT_ID is not null^

create unique index IDX_REPORT_REPORT_UNIQ_NAME on REPORT_REPORT (NAME) where DELETE_TS is null and SYS_TENANT_ID is null^
create unique index IDX_REPORT_REPORT_UNIQ_NAME_SYS_TENANT_ID_NN on REPORT_REPORT (NAME, SYS_TENANT_ID)
    where DELETE_TS is null and SYS_TENANT_ID is not null^