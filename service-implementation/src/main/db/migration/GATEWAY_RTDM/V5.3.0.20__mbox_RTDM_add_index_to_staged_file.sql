--
-- Copyright 2018 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--

/**
 * INDEX for mostly used non unique columns in the where clause used in the runtime query
 */
DECLARE
    i INTEGER;
BEGIN
    SELECT COUNT(*) INTO i FROM user_indexes WHERE index_name = 'IX08_STAGED_FILE' and table_owner='GATEWAY_OWNR' and table_name='STAGED_FILE';
    IF i = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX GATEWAY_OWNR.IX08_STAGED_FILE ON GATEWAY_OWNR.STAGED_FILE 
                            (
                                PARENT_GLOBAL_PROCESS_ID
                            )
                                LOGGING
                                TABLESPACE GATEWAY_TRNX
                                PCTFREE    10
                                INITRANS   2
                                MAXTRANS   255
                                STORAGE    (
                                        INITIAL          64K
                                        NEXT             1M
                                        MAXSIZE          UNLIMITED
                                        MINEXTENTS       1
                                        MAXEXTENTS       UNLIMITED
                                        PCTINCREASE      0
                                        BUFFER_POOL      DEFAULT
                                        FLASH_CACHE      DEFAULT
                                        CELL_FLASH_CACHE DEFAULT
                                       )
                            NOPARALLEL';
    END IF;
END;
/