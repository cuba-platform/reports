
alter table REPORT_EXECUTION add column OUTPUT_DOCUMENT_ID varchar(36)^

alter table REPORT_EXECUTION add constraint FK_REPORT_EXECUTION_TO_DOCUMENT
foreign key (OUTPUT_DOCUMENT_ID) references SYS_FILE(ID);
 