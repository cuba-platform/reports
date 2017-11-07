-- Description: Add not null for fields in reporting tables

-- REPORT_REPORT
alter table REPORT_REPORT alter column NAME set not null;

update REPORT_REPORT set REPORT_TYPE = 10 where REPORT_TYPE is null;
alter table REPORT_REPORT alter column REPORT_TYPE set not null;

-- REPORT_BAND_DEFINITION
delete from REPORT_BAND_DEFINITION where REPORT_ID is null;
alter table REPORT_BAND_DEFINITION alter column REPORT_ID set not null;

alter table REPORT_BAND_DEFINITION alter column NAME set not null;
alter table REPORT_BAND_DEFINITION alter column ORIENTATION set not null;
alter table REPORT_BAND_DEFINITION alter column POSITION_ set not null;

-- REPORT_TEMPLATE
delete from REPORT_TEMPLATE where REPORT_ID is null;
alter table REPORT_TEMPLATE alter column REPORT_ID set not null;

delete from REPORT_TEMPLATE where TEMPLATE_FILE_ID is null;
alter table REPORT_TEMPLATE alter column TEMPLATE_FILE_ID set not null;

alter table REPORT_TEMPLATE alter column OUTPUT_TYPE set not null;

-- REPORT_INPUT_PARAMETER
delete from REPORT_INPUT_PARAMETER where REPORT_ID is null;
alter table REPORT_INPUT_PARAMETER alter column REPORT_ID set not null;
alter table REPORT_INPUT_PARAMETER alter column TYPE set not null;
alter table REPORT_INPUT_PARAMETER alter column NAME set not null;

-- REPORT_DATA_SET
alter table REPORT_DATA_SET alter column NAME set not null;
alter table REPORT_DATA_SET alter column TYPE set not null;
alter table REPORT_DATA_SET alter column BAND_DEFINITION set not null;

-- REPORT_REPORT_SCREEN
alter table REPORT_REPORT_SCREEN alter column REPORT_ID set not null;
alter table REPORT_REPORT_SCREEN alter column SCREEN_ID set not null;

-- REPORT_VALUE_FORMAT
alter table REPORT_VALUE_FORMAT alter column REPORT_ID set not null;
alter table REPORT_VALUE_FORMAT alter column NAME set not null;
alter table REPORT_VALUE_FORMAT alter column FORMAT set not null^
