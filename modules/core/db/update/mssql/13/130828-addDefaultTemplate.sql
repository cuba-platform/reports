alter table REPORT_REPORT add DEFAULT_TEMPLATE_ID uniqueidentifier;

alter table REPORT_REPORT add constraint FK_REPORT_REPORT_TO_DEF_TEMPLATE foreign key (DEFAULT_TEMPLATE_ID) references REPORT_TEMPLATE (ID);


