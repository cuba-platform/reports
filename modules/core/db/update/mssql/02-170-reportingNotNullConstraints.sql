-- Description: Add not null for fields in reporting tables

-- REPORT_REPORT
alter table REPORT_REPORT alter column NAME varchar(255) not null;

update REPORT_REPORT set REPORT_TYPE = 10 where REPORT_TYPE is null;
alter table REPORT_REPORT alter column REPORT_TYPE integer not null;

-- REPORT_BAND_DEFINITION
delete from REPORT_BAND_DEFINITION where REPORT_ID is null;
alter table REPORT_BAND_DEFINITION alter column REPORT_ID uniqueidentifier not null;

alter table REPORT_BAND_DEFINITION alter column NAME varchar(255) not null;
alter table REPORT_BAND_DEFINITION alter column ORIENTATION integer not null;
alter table REPORT_BAND_DEFINITION alter column POSITION_ integer not null;

-- REPORT_TEMPLATE
delete from REPORT_TEMPLATE where REPORT_ID is null;
alter table REPORT_TEMPLATE alter column REPORT_ID uniqueidentifier not null;

alter table REPORT_TEMPLATE alter column TEMPLATE_FILE_ID uniqueidentifier not null;
alter table REPORT_TEMPLATE alter column OUTPUT_TYPE integer not null;

-- REPORT_INPUT_PARAMETER
delete from REPORT_INPUT_PARAMETER where REPORT_ID is null;
alter table REPORT_INPUT_PARAMETER alter column REPORT_ID uniqueidentifier not null;

alter table REPORT_INPUT_PARAMETER alter column TYPE integer not null;
alter table REPORT_INPUT_PARAMETER alter column NAME varchar(255) not null;

-- REPORT_DATA_SET
alter table REPORT_DATA_SET alter column NAME varchar(255) not null;
alter table REPORT_DATA_SET alter column TYPE integer not null;
alter table REPORT_DATA_SET alter column BAND_DEFINITION uniqueidentifier not null;

-- REPORT_REPORT_SCREEN
alter table REPORT_REPORT_SCREEN alter column REPORT_ID uniqueidentifier not null;
alter table REPORT_REPORT_SCREEN alter column SCREEN_ID varchar(255) not null;

-- REPORT_VALUE_FORMAT
alter table REPORT_VALUE_FORMAT alter column REPORT_ID uniqueidentifier not null;
alter table REPORT_VALUE_FORMAT alter column NAME varchar(255) not null;
alter table REPORT_VALUE_FORMAT alter column FORMAT varchar(255) not null^
