--
-- Copyright 2017 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--
/**
 * Adding new column "CLUSTER_TYPE" in the table "MAILBOX"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'CLUSTER_TYPE'
      and table_name = 'MAILBOX'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.MAILBOX ADD CLUSTER_TYPE VARCHAR2(32) DEFAULT ''SECURE'' NOT NULL';
  end if;
end;
/

/**
 * Adding new column "CLUSTER_TYPE" in the table "PROCESSOR"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'CLUSTER_TYPE'
      and table_name = 'PROCESSOR'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.PROCESSOR ADD CLUSTER_TYPE VARCHAR2(32) DEFAULT ''SECURE'' NOT NULL';
  end if;
end;
/