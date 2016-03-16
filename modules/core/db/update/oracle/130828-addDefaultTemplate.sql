alter table REPORT_REPORT add column DEFAULT_TEMPLATE_ID varchar2(32);

alter table REPORT_REPORT add constraint FK_REPORT_TO_DEF_TEMPLATE foreign key (DEFAULT_TEMPLATE_ID) references REPORT_TEMPLATE(ID)^


