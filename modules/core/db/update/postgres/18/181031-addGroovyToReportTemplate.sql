alter table REPORT_TEMPLATE add column IS_GROOVY boolean default false^
update REPORT_TEMPLATE set IS_GROOVY = false where IS_GROOVY is null^