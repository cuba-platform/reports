alter table REPORT_REPORT add column DELETE_TS timestamp^
alter table REPORT_REPORT add column DELETED_BY varchar(50)^

alter table REPORT_GROUP add column DELETE_TS timestamp^
alter table REPORT_GROUP add column DELETED_BY varchar(50)^

alter table REPORT_TEMPLATE add column DELETE_TS timestamp^
alter table REPORT_TEMPLATE add column DELETED_BY varchar(50)^

create function updateIndexReportUniqueName() returns void as $$
begin
  if exists (
    select 1
    from   pg_class c
    join   pg_namespace n ON n.oid = c.relnamespace
    where  c.relname = 'idx_report_report_uniq_name'
    ) then
      drop index IDX_REPORT_REPORT_UNIQ_NAME;
      create unique index IDX_REPORT_REPORT_UNIQ_NAME on REPORT_REPORT (NAME) where DELETE_TS is null;
  end if;
end;
$$ language plpgsql^

select updateIndexReportUniqueName()^
drop function updateIndexReportUniqueName()^

create function updateIndexReportGroupUniqueTitle() returns void as $$
begin
  if exists (
    select 1
    from   pg_class c
    join   pg_namespace n ON n.oid = c.relnamespace
    where  c.relname = 'idx_report_group_uniq_title'
    ) then
      drop index IDX_REPORT_GROUP_UNIQ_TITLE;
      create unique index IDX_REPORT_GROUP_UNIQ_TITLE on REPORT_GROUP (TITLE) where DELETE_TS is null;
  end if;
end;
$$ language plpgsql^

select updateIndexReportGroupUniqueTitle()^
drop function updateIndexReportGroupUniqueTitle()^