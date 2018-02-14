--
-- Copyright 2016 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--

/**
 * Adding new column "PROCESSING_DC" in the table "STAGED_FILE"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'PROCESS_DC'
      and table_name = 'STAGED_FILE'
      and owner = 'GATEWAY_OWNR';

        if (v_column_exists = 0) then
             execute immediate 'ALTER TABLE GATEWAY_OWNR.STAGED_FILE ADD PROCESS_DC VARCHAR2(16)';
        end if;
        
        EXECUTE IMMEDIATE 'UPDATE GATEWAY_OWNR.STAGED_FILE 
        SET GATEWAY_OWNR.STAGED_FILE.PROCESS_DC = GATEWAY_OWNR.STAGED_FILE.ORIGINATING_DC';
        commit;
        
        EXECUTE IMMEDIATE 'ALTER TABLE GATEWAY_OWNR.STAGED_FILE MODIFY (PROCESS_DC VARCHAR2(16) NOT NULL)';
end;