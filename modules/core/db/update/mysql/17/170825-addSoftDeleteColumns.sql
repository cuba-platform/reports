alter table REPORT_REPORT add DELETE_TS datetime(3)^
alter table REPORT_REPORT add DELETED_BY varchar(50)^
alter table REPORT_REPORT add DELETE_TS_NN datetime(3) not null default '1000-01-01 00:00:00.000'^

alter table REPORT_GROUP add DELETE_TS datetime(3)^
alter table REPORT_GROUP add DELETED_BY varchar(50)^
alter table REPORT_GROUP add DELETE_TS_NN datetime(3) not null default '1000-01-01 00:00:00.000'^

alter table REPORT_TEMPLATE add DELETE_TS datetime(3)^
alter table REPORT_TEMPLATE add DELETED_BY varchar(50)^
alter table REPORT_TEMPLATE add DELETE_TS_NN datetime(3) not null default '1000-01-01 00:00:00.000'^

drop index IDX_REPORT_REPORT_UNIQ_NAME on REPORT_REPORT^
create unique index IDX_REPORT_REPORT_UNIQ_NAME on REPORT_REPORT (NAME, DELETE_TS_NN)^

drop index IDX_REPORT_GROUP_UNIQ_TITLE on REPORT_GROUP^
create unique index IDX_REPORT_GROUP_UNIQ_TITLE on REPORT_GROUP (TITLE, DELETE_TS_NN)^

create trigger REPORT_REPORT_DELETE_TS_NN_TRIGGER before update on REPORT_REPORT
  for each row
    if not(NEW.DELETE_TS <=> OLD.DELETE_TS) then
    set NEW.DELETE_TS_NN = if (NEW.DELETE_TS is null, '1000-01-01 00:00:00.000', NEW.DELETE_TS);
  end if^

create trigger REPORT_GROUP_DELETE_TS_NN_TRIGGER before update on REPORT_GROUP
  for each row
    if not(NEW.DELETE_TS <=> OLD.DELETE_TS) then
      set NEW.DELETE_TS_NN = if (NEW.DELETE_TS is null, '1000-01-01 00:00:00.000', NEW.DELETE_TS);
    end if^

create trigger REPORT_TEMPLATE_DELETE_TS_NN_TRIGGER before update on REPORT_TEMPLATE
  for each row
    if not(NEW.DELETE_TS <=> OLD.DELETE_TS) then
      set NEW.DELETE_TS_NN = if (NEW.DELETE_TS is null, '1000-01-01 00:00:00.000', NEW.DELETE_TS);
    end if^
