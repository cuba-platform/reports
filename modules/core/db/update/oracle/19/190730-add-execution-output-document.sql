
alter table REPORT_EXECUTION add OUTPUT_DOCUMENT_ID varchar2(32)^

alter table REPORT_EXECUTION add constraint FK_REPORT_EXECUTION_TO_DOCUMEN
foreign key (OUTPUT_DOCUMENT_ID) references SYS_FILE(ID)^
 