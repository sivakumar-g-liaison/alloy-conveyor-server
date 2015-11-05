--
-- Copyright 2015 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--
/**
 * Adding new columns for archive and stale file clean up
 */
DECLARE
  v_column_exists number := 0;  
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'PROCESSOR_GUID'
      and table_name = 'STAGED_FILE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.STAGED_FILE ADD PROCESSOR_GUID CHAR(32)';
  end if;
end;
/

DECLARE
  v_column_exists number := 0;  
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'PROCESSOR_TYPE'
      and table_name = 'STAGED_FILE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.STAGED_FILE ADD PROCESSOR_TYPE VARCHAR2(128)';
  end if;
end;
/

DECLARE
  v_column_exists number := 0;  
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'CREATED_DATE'
      and table_name = 'STAGED_FILE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.STAGED_FILE ADD CREATED_DATE TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP CONSTRAINT NN07_STAGED_FILE NOT NULL';
  end if;
end;
/

DECLARE
  v_column_exists number := 0;  
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'MODIFIED_DATE'
      and table_name = 'STAGED_FILE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.STAGED_FILE ADD MODIFIED_DATE TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP CONSTRAINT NN08_STAGED_FILE NOT NULL';
  end if;
end;
/

