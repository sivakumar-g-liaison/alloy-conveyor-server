--
-- Copyright 2017 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--

/**
 * Adding new column "PROCESS_DC" in the table "PROCESSOR"
 */
DECLARE
  v_column_exists number := 0;
BEGIN
  Select count(*) into v_column_exists
    from all_tab_cols
    where column_name = 'PROCESS_DC'
      and table_name = 'PROCESSOR'
      and owner = 'GATEWAY_OWNR';

  if (v_column_exists = 0) then
      execute immediate 'ALTER TABLE GATEWAY_OWNR.PROCESSOR ADD PROCESS_DC VARCHAR2(16)';
  end if;
end;
/


-- adding index
DECLARE
    i INTEGER;
BEGIN
    SELECT COUNT(*) INTO i FROM user_indexes WHERE index_name = 'IX01_PROCESSOR' and table_owner='GATEWAY_OWNR' and table_name='PROCESSOR';
    IF i = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX GATEWAY_OWNR.IX01_PROCESSOR ON GATEWAY_OWNR.PROCESSOR
                            (
                                PROCESS_DC,
                                STATUS,
                                CLUSTER_TYPE
                            )
                                TABLESPACE GATEWAY_CONF
                                STORAGE
                                (
                                    INITIAL 1M
                                    NEXT 1M
                                    PCTINCREASE 0
                                )';
    END IF;
END;

/