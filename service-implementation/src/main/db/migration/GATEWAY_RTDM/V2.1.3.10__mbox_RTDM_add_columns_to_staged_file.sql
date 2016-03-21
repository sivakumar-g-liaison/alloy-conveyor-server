--
-- Copyright 2016 Liaison Technologies, Inc.
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
    where column_name = 'NOTIFICATION_COUNT'
      and table_name = 'STAGED_FILE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.STAGED_FILE ADD NOTIFICATION_COUNT NUMBER(19, 0)';
  end if;
end;
/