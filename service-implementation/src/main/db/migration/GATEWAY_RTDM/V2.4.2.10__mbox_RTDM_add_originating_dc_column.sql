--
-- Copyright 2016 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--
/**
 * Adding new column "ORIGINATING_DC" in the table "PROCESSORS"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'ORIGINATING_DC'
      and table_name = 'PROCESSORS'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.PROCESSORS ADD ORIGINATING_DC VARCHAR2(16)';
  end if;
end;
/

/**
 * Adding new column "ORIGINATING_DC" in the table "PROCESSOR_EXEC_STATE"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'ORIGINATING_DC'
      and table_name = 'PROCESSOR_EXEC_STATE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.PROCESSOR_EXEC_STATE ADD ORIGINATING_DC VARCHAR2(16)';
  end if;
end;
/

/**
 * Adding new column "ORIGINATING_DC" in the table "STAGED_FILE"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'ORIGINATING_DC'
      and table_name = 'STAGED_FILE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.STAGED_FILE ADD ORIGINATING_DC VARCHAR2(16)';
  end if;
end;
/