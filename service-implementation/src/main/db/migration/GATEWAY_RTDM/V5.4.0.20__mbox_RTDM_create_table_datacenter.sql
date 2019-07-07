--
-- Copyright 2018 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--

----------------------------------------------------------------
-- DATACENTER
----------------------------------------------------------------

DECLARE table_count NUMBER;
BEGIN
  SELECT count(*) INTO table_count FROM all_tables WHERE table_name = 'DATACENTER' AND owner = 'GATEWAY_OWNR';
  IF table_count = 0 THEN 
    EXECUTE IMMEDIATE 'CREATE TABLE GATEWAY_OWNR.DATACENTER
        (
          NAME VARCHAR2(5) CONSTRAINT NN01_DATACENTER NOT NULL,
          PROCESSING_DC VARCHAR2(5) CONSTRAINT NN02_DATACENTER NOT NULL
        )
        TABLESPACE GATEWAY_TRNX
      STORAGE
      (
        PCTINCREASE 0
      )
      NOPARALLEL'
  ;

    EXECUTE IMMEDIATE 'CREATE OR REPLACE SYNONYM GATEWAY_APPL.DATACENTER FOR GATEWAY_OWNR.DATACENTER';
    EXECUTE IMMEDIATE 'GRANT SELECT ON GATEWAY_OWNR.DATACENTER TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE 'GRANT INSERT ON GATEWAY_OWNR.DATACENTER TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE 'GRANT UPDATE ON GATEWAY_OWNR.DATACENTER TO GATEWAY_APPL_ROLE';

    EXECUTE IMMEDIATE 'ALTER TABLE GATEWAY_OWNR.DATACENTER ADD
    CONSTRAINT PK_DATACENTER PRIMARY KEY (NAME)
    USING INDEX
      TABLESPACE GATEWAY_TRNX
      STORAGE
      (
        PCTINCREASE 0
      )';

  END IF;
END;
/