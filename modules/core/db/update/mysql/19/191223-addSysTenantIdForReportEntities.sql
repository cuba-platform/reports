alter table REPORT_GROUP add SYS_TENANT_ID varchar(255)^
alter table REPORT_GROUP add SYS_TENANT_ID_NN varchar(255)^
alter table REPORT_REPORT add SYS_TENANT_ID varchar(255)^
alter table REPORT_REPORT add SYS_TENANT_ID_NN varchar(255)^

drop index IDX_REPORT_GROUP_UNIQ_TITLE on REPORT_GROUP^
create unique index IDX_REPORT_GROUP_UNIQ_TITLE on REPORT_GROUP (TITLE, SYS_TENANT_ID_NN, DELETE_TS_NN)^

create trigger REPORT_GROUP_SYS_TENANT_ID_NN_INSERT_TRIGGER before insert on REPORT_GROUP
for each row set NEW.SYS_TENANT_ID_NN = if (NEW.SYS_TENANT_ID is null, 'no_tenant', NEW.SYS_TENANT_ID)^

drop trigger REPORT_GROUP_DELETE_TS_NN_TRIGGER^
create trigger REPORT_GROUP_SYS_TENANT_ID_NN_AND_DELETE_TS_NN_UPDATE_TRIGGER before update on REPORT_GROUP
for each row
begin
    if not(NEW.SYS_TENANT_ID <=> OLD.SYS_TENANT_ID) then
      set NEW.SYS_TENANT_ID_NN = if (NEW.SYS_TENANT_ID is null, 'no_tenant', NEW.SYS_TENANT_ID);
    end if;
    if not(NEW.DELETE_TS <=> OLD.DELETE_TS) then
      set NEW.DELETE_TS_NN = if (NEW.DELETE_TS is null, '1000-01-01 00:00:00.000', NEW.DELETE_TS);
    end if;
end^

drop index IDX_REPORT_REPORT_UNIQ_NAME on REPORT_REPORT^
create unique index IDX_REPORT_REPORT_UNIQ_NAME on REPORT_REPORT (NAME, SYS_TENANT_ID_NN, DELETE_TS_NN)^

create trigger REPORT_REPORT_SYS_TENANT_ID_NN_INSERT_TRIGGER before insert on REPORT_REPORT
for each row set NEW.SYS_TENANT_ID_NN = if (NEW.SYS_TENANT_ID is null, 'no_tenant', NEW.SYS_TENANT_ID)^

drop trigger REPORT_REPORT_DELETE_TS_NN_TRIGGER^
create trigger REPORT_REPORT_SYS_TENANT_ID_NN_AND_DELETE_TS_NN_UPDATE_TRIGGER before update on REPORT_REPORT
for each row
begin
    if not(NEW.SYS_TENANT_ID <=> OLD.SYS_TENANT_ID) then
      set NEW.SYS_TENANT_ID_NN = NEW.SYS_TENANT_ID;
    end if;
    if not(NEW.DELETE_TS <=> OLD.DELETE_TS) then
      set NEW.DELETE_TS_NN = if (NEW.DELETE_TS is null, '1000-01-01 00:00:00.000', NEW.DELETE_TS);
    end if;
end^