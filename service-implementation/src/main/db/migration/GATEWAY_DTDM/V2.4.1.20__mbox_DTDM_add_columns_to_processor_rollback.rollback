--
-- Copyright 2016 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--
/**
 * Rollback script for V2.3.3.10__mbox_DTDM_add_columns_to_processor.sql
 */
 
 /**
 * Drop column "MODIFIED_BY" from the table "PROCESSOR" 
 */
DECLARE
  v_column_exists number := 0;  
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'MODIFIED_BY'
      and table_name = 'PROCESSOR'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.PROCESSOR DROP COLUMN MODIFIED_BY';
  end if;
end;
/

/**
 * Drop new column "MODIFIED_DATE" from the table "PROCESSOR" 
 */
DECLARE
  v_column_exists number := 0;  
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'MODIFIED_DATE'
      and table_name = 'PROCESSOR'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.PROCESSOR DROP COLUMN MODIFIED_DATE';
  end if;
end;
/
