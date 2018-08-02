alter table REPORT_REPORT add IS_SYSTEM tinyint default 0^
update REPORT_REPORT set IS_SYSTEM = 0 where IS_SYSTEM is null^