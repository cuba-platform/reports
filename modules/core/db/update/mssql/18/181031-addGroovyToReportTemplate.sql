alter table REPORT_TEMPLATE add IS_GROOVY tinyint default 0^
update REPORT_TEMPLATE set IS_GROOVY = 0 where IS_GROOVY is null^