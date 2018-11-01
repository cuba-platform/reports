alter table REPORT_TEMPLATE add IS_GROOVY char(1)^
update REPORT_TEMPLATE set IS_GROOVY = 0 where IS_GROOVY is null^