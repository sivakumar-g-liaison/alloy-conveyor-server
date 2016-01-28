--
-- Copyright 2016 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--
/**
 * Inactivate the old records
 */
UPDATE GATEWAY_OWNR.STAGED_FILE SET STATUS = 'INACTIVE' WHERE FILE_PATH IS NULL;
COMMIT;

/**
 * Adding new columns to stage multiple files for a process
 */
DECLARE
  v_column_exists number := 0;  
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'GLOBAL_PROCESS_ID'
      and table_name = 'STAGED_FILE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.STAGED_FILE ADD GLOBAL_PROCESS_ID CHAR(32) DEFAULT ''NOT AVAILABLE'' CONSTRAINT NN01_STAGED_FILE NOT NULL';
  end if;
end;
/

