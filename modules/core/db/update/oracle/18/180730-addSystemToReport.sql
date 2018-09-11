alter table REPORT_REPORT add IS_SYSTEM char(1)^
update REPORT_REPORT set IS_SYSTEM = 0 where IS_SYSTEM is null^