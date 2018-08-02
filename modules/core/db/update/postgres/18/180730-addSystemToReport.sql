alter table REPORT_REPORT add column IS_SYSTEM boolean default false^
update REPORT_REPORT set IS_SYSTEM = false where IS_SYSTEM is null^