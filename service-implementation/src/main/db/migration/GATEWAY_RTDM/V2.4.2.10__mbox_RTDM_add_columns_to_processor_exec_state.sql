--
-- Copyright 2016 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--

/**
 * Adding new column "LAST_EXEC_STATE" in the table "PROCESSOR_EXEC_STATE" 
 */
DECLARE
  v_column_exists number := 0;  
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'LAST_EXEC_STATE'
      and table_name = 'PROCESSOR_EXEC_STATE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.PROCESSOR_EXEC_STATE ADD LAST_EXEC_STATE VARCHAR2(32)';
  end if;
end;
/

/**
 * Adding new column "LAST_EXEC_DATE" in the table "PROCESSOR_EXEC_STATE" 
 */
DECLARE
  v_column_exists number := 0;  
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'LAST_EXEC_DATE'
      and table_name = 'PROCESSOR_EXEC_STATE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.PROCESSOR_EXEC_STATE ADD LAST_EXEC_DATE TIMESTAMP WITH TIME ZONE';
  end if;
end;
/

/**
 * Adding new column "NODE_IN_USE" in the table "PROCESSOR_EXEC_STATE" 
 */
DECLARE
  v_column_exists number := 0;  
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'NODE_IN_USE'
      and table_name = 'PROCESSOR_EXEC_STATE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.PROCESSOR_EXEC_STATE ADD NODE_IN_USE VARCHAR2(128)';
  end if;
end;
/

/**
 * Adding new column "MODIFIED_BY" in the table "PROCESSOR_EXEC_STATE" 
 */
DECLARE
  v_column_exists number := 0;  
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'MODIFIED_BY'
      and table_name = 'PROCESSOR_EXEC_STATE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.PROCESSOR_EXEC_STATE ADD MODIFIED_BY VARCHAR2(128)';
  end if;
end;
/

/**
 * Adding new column "MODIFIED_DATE" in the table "PROCESSOR_EXEC_STATE" 
 */
DECLARE
  v_column_exists number := 0;  
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'MODIFIED_DATE'
      and table_name = 'PROCESSOR_EXEC_STATE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.PROCESSOR_EXEC_STATE ADD MODIFIED_DATE TIMESTAMP WITH TIME ZONE';
  end if;
end;
/

/**
 * Adding new column "EXEC_TYPE" in the table "PROCESSOR_EXEC_STATE" 
 */
DECLARE
  v_column_exists number := 0;  
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'EXEC_TYPE'
      and table_name = 'PROCESSOR_EXEC_STATE'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.PROCESSOR_EXEC_STATE ADD EXEC_TYPE VARCHAR2(32)';
  end if;
end;
/

--Removing PROCESSOR_EXEC_STATE constraint
DECLARE
	num_rows integer;
BEGIN
	SELECT count(*) INTO num_rows FROM USER_CONSTRAINTS WHERE table_name = 'PROCESSOR_EXEC_STATE' AND constraint_name = 'CK02_PROCESSOR_EXEC_STATE';

	IF num_rows = 1 THEN
		EXECUTE IMMEDIATE
			'ALTER TABLE GATEWAY_OWNR.PROCESSOR_EXEC_STATE DROP CONSTRAINT CK02_PROCESSOR_EXEC_STATE';
	END IF;
END;

/

--Adding PROCESSOR_EXEC_STATE constraint

ALTER TABLE GATEWAY_OWNR.PROCESSOR_EXEC_STATE
ADD CONSTRAINT CK02_PROCESSOR_EXEC_STATE CHECK (EXEC_TYPE IN ('DEFAULT', 'SCRIPT'));
