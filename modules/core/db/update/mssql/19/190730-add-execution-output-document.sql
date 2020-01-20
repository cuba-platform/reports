
alter table REPORT_EXECUTION add OUTPUT_DOCUMENT_ID uniqueidentifier^

alter table REPORT_EXECUTION add constraint FK_REPORT_EXECUTION_TO_DOCUMENT
foreign key (OUTPUT_DOCUMENT_ID) references SYS_FILE(ID)^
 