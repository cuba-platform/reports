-- Description: Remove ROOT_DEFINITION_ID field from REPORT_REPORT, Add on delete cascade for REPORT objects

alter table REPORT_REPORT drop constraint FK_REPORT_REPORT_TO_REPORT_BAND_DEFINITION^

alter table REPORT_REPORT drop column ROOT_DEFINITION_ID^

--create unique index UK_REPORT_REPORT_CODE on REPORT_REPORT (CODE) where CODE is not null^

alter table REPORT_BAND_DEFINITION drop column QUERY^

-- REPORT_BAND_DEFINITION to REPORT_REPORT constraint
alter table REPORT_BAND_DEFINITION drop constraint FK_REPORT_BAND_DEFINITION_TO_REPORT_REPORT^
alter table REPORT_BAND_DEFINITION add constraint FK_REPORT_BAND_DEFINITION_TO_REPORT_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID) on delete cascade^

-- REPORT_TEMPLATE to REPORT constraint
alter table REPORT_TEMPLATE drop constraint FK_REPORT_TEMPLATE_TO_REPORT^
alter table REPORT_TEMPLATE add constraint FK_REPORT_TEMPLATE_TO_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID) on delete cascade^

-- REPORT_INPUT_PARAMETER to REPORT constraint, + fix mistake in REPORT word
alter table REPORT_INPUT_PARAMETER drop constraint FK_REPOR_INPUT_PARAMETER_TO_REPORT_REPORT^
alter table REPORT_INPUT_PARAMETER add constraint FK_REPORT_INPUT_PARAMETER_TO_REPORT_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID) on delete cascade^

-- REPORT_DATA_SET to REPORT_BAND_DEFINITION constraint
alter table REPORT_DATA_SET drop constraint FK_REPORT_DATA_SET_TO_REPORT_BAND_DEFINITION^
alter table REPORT_DATA_SET add constraint FK_REPORT_DATA_SET_TO_REPORT_BAND_DEFINITION foreign key (BAND_DEFINITION)
      references REPORT_BAND_DEFINITION (ID) on delete cascade^

-- REPORT_REPORTS_ROLES to REPORT constraint
alter table REPORT_REPORTS_ROLES drop constraint FK_REPORT_REPORTS_ROLES_TO_REPORT^
alter table REPORT_REPORTS_ROLES add constraint FK_REPORT_REPORTS_ROLES_TO_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT(ID) on delete cascade^

-- REPORT_REPORT_SCREEN to REPORT constraint
alter table REPORT_REPORT_SCREEN drop constraint FK_REPORT_REPORT_SCREEN_TO_REPORT_REPORT^
alter table REPORT_REPORT_SCREEN add constraint FK_REPORT_REPORT_SCREEN_TO_REPORT_REPORT foreign key (REPORT_ID)
      references REPORT_REPORT (ID) on delete cascade^

-- REPORT_VALUE_FORMAT to REPORT constraint
alter table REPORT_VALUE_FORMAT drop constraint FK_REPORT_VALUE_FORMAT_TO_REPORT_REPORT^
alter table REPORT_VALUE_FORMAT add constraint FK_REPORT_VALUE_FORMAT_TO_REPORT_REPORT foreign key (REPORT_ID)
      references report_report (ID) on delete cascade^