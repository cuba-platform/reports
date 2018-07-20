alter table REPORT_REPORT add REST_ACCESS boolean default false^
update REPORT_REPORT set REST_ACCESS = 0 where REST_ACCESS is null^