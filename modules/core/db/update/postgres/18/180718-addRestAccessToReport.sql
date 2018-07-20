alter table REPORT_REPORT add column REST_ACCESS boolean default false^
update REPORT_REPORT set REST_ACCESS = false where REST_ACCESS is null^