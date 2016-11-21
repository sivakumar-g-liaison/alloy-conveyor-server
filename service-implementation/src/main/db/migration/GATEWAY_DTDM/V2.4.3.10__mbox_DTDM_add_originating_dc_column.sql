--
-- Copyright 2016 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--
/**
 * Adding new column "ORIGINATING_DC" in the table "CREDENTIAL"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'ORIGINATING_DC'
      and table_name = 'CREDENTIAL'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.CREDENTIAL ADD ORIGINATING_DC VARCHAR2(16)';
  end if;
end;
/

/**
 * Adding new column "ORIGINATING_DC" in the table "FOLDER"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'ORIGINATING_DC'
      and table_name = 'FOLDER'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.FOLDER ADD ORIGINATING_DC VARCHAR2(16)';
  end if;
end;
/

/**
 * Adding new column "ORIGINATING_DC" in the table "MAILBOX"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'ORIGINATING_DC'
      and table_name = 'MAILBOX'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.MAILBOX ADD ORIGINATING_DC VARCHAR2(16)';
  end if;
end;
/

/**
 * Adding new column "ORIGINATING_DC" in the table "MAILBOX_PROPERTY"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'ORIGINATING_DC'
      and table_name = 'MAILBOX_PROPERTY'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.MAILBOX_PROPERTY ADD ORIGINATING_DC VARCHAR2(16)';
  end if;
end;
/

/**
 * Adding new column "ORIGINATING_DC" in the table "MAILBOX_SVC_INSTANCE"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'ORIGINATING_DC'
      and table_name = 'MAILBOX_SVC_INSTANCE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.MAILBOX_SVC_INSTANCE ADD ORIGINATING_DC VARCHAR2(16)';
  end if;
end;
/

/**
 * Adding new column "ORIGINATING_DC" in the table "PROCESSOR"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'ORIGINATING_DC'
      and table_name = 'PROCESSOR'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.PROCESSOR ADD ORIGINATING_DC VARCHAR2(16)';
  end if;
end;
/

/**
 * Adding new column "ORIGINATING_DC" in the table "PROCESSOR_PROPERTY"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'ORIGINATING_DC'
      and table_name = 'PROCESSOR_PROPERTY'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.PROCESSOR_PROPERTY ADD ORIGINATING_DC VARCHAR2(16)';
  end if;
end;
/

/**
 * Adding new column "ORIGINATING_DC" in the table "SCHED_PROCESSOR"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'ORIGINATING_DC'
      and table_name = 'SCHED_PROCESSOR'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.SCHED_PROCESSOR ADD ORIGINATING_DC VARCHAR2(16)';
  end if;
end;
/

/**
 * Adding new column "ORIGINATING_DC" in the table "SCHED_PROFILE"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'ORIGINATING_DC'
      and table_name = 'SCHED_PROFILE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.SCHED_PROFILE ADD ORIGINATING_DC VARCHAR2(16)';
  end if;
end;
/

/**
 * Adding new column "ORIGINATING_DC" in the table "SERVICE_INSTANCE"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'ORIGINATING_DC'
      and table_name = 'SERVICE_INSTANCE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.SERVICE_INSTANCE ADD ORIGINATING_DC VARCHAR2(16)';
  end if;
end;
/