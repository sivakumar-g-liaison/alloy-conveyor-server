--
-- Copyright 2016 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--

/**
 * Create Processors table
 */
 
DECLARE table_count NUMBER;
BEGIN
  SELECT count(*) INTO table_count FROM all_tables WHERE table_name = 'PROCESSORS';
  IF table_count = 0 THEN 
    EXECUTE IMMEDIATE 'CREATE TABLE GATEWAY_OWNR.PROCESSORS
        (
          PGUID CHAR(32) NOT NULL,
        PROCESSOR_ID CHAR(32) NOT NULL
        )
        TABLESPACE GATEWAY_TRNX
      STORAGE
      (
        PCTINCREASE 0
      )
      NOPARALLEL'
  ;
  EXECUTE IMMEDIATE 'CREATE OR REPLACE SYNONYM GATEWAY_APPL.PROCESSORS FOR GATEWAY_OWNR.PROCESSORS';
  EXECUTE IMMEDIATE 'GRANT SELECT ON GATEWAY_OWNR.PROCESSORS TO GATEWAY_APPL_ROLE';
  EXECUTE IMMEDIATE 'GRANT INSERT ON GATEWAY_OWNR.PROCESSORS TO GATEWAY_APPL_ROLE';
  EXECUTE IMMEDIATE 'GRANT UPDATE ON GATEWAY_OWNR.PROCESSORS TO GATEWAY_APPL_ROLE';
  EXECUTE IMMEDIATE 'GRANT DELETE ON GATEWAY_OWNR.PROCESSORS TO GATEWAY_APPL_ROLE';

  EXECUTE IMMEDIATE 'ALTER TABLE GATEWAY_OWNR.PROCESSORS ADD
  CONSTRAINT PK_PROCESSORS PRIMARY KEY (PGUID)
    USING INDEX
      TABLESPACE GATEWAY_TRNX
      STORAGE
      (
        PCTINCREASE 0
      )';

  EXECUTE IMMEDIATE 'ALTER TABLE GATEWAY_OWNR.PROCESSORS ADD
  CONSTRAINT AK01_PROCESSORS UNIQUE (PROCESSOR_ID)
    USING INDEX
      TABLESPACE GATEWAY_TRNX
      STORAGE
      (
        PCTINCREASE 0
      )';

  EXECUTE IMMEDIATE 'INSERT INTO GATEWAY_OWNR.PROCESSORS(PGUID, PROCESSOR_ID)
  SELECT PGUID, PROCESSOR_ID from GATEWAY_OWNR.PROCESSOR_EXEC_STATE';
  END IF;
END;
/
