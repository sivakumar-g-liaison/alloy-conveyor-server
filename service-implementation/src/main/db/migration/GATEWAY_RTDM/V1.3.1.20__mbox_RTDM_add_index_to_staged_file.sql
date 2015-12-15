--
-- Copyright 2015 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--
/**
 *  The following indexes are only for the MAILBOX. The MAILBOX use case is different compared to DROPBOX.
 */

/**
 * Query to remove the duplicate rows from the STAGED_FILE table, this is required to apply the primary key constraint to the pguid column
 */
DELETE FROM GATEWAY_OWNR.STAGED_FILE A
WHERE  ROWID > (SELECT MIN(ROWID) FROM GATEWAY_OWNR.STAGED_FILE B WHERE B.PGUID = A.PGUID);

/**
 * PRIMARY_KEY is missed out in the MAILBOX RTDM migration script(V1.0.0.40__mbox_RTDM_initial_schema_modified.sql).
 * Enabling this would restrict the use of multiple files for a global process id
 */
DECLARE
    i INTEGER;
BEGIN
    SELECT COUNT(*) INTO i FROM user_indexes WHERE index_name = 'PK_STAGED_FILE' and table_owner='GATEWAY_OWNR' and table_name='STAGED_FILE';
    IF i = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE GATEWAY_OWNR.STAGED_FILE ADD 
							CONSTRAINT PK_STAGED_FILE PRIMARY KEY (PGUID)
								USING INDEX
									TABLESPACE GATEWAY_TRNX
									STORAGE
									(
										PCTINCREASE 0
									)
							';
    END IF;
END;
/

/**
 * INDEX for mostly used non unique columns in the where clause used in the runtime query
 */
DECLARE
    i INTEGER;
BEGIN
    SELECT COUNT(*) INTO i FROM user_indexes WHERE index_name = 'IX01_STAGED_FILE' and table_owner='GATEWAY_OWNR' and table_name='STAGED_FILE';
    IF i = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX GATEWAY_OWNR.IX01_STAGED_FILE ON GATEWAY_OWNR.STAGED_FILE 
							(
								PROCESSOR_GUID,
								STATUS
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

/**
 * INDEX for the columns frequently used for LENS status update
 */
DECLARE
    i INTEGER;
BEGIN
    SELECT COUNT(*) INTO i FROM user_indexes WHERE index_name = 'IX02_STAGED_FILE' and table_owner='GATEWAY_OWNR' and table_name='STAGED_FILE';
    IF i = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX GATEWAY_OWNR.IX02_STAGED_FILE ON GATEWAY_OWNR.STAGED_FILE 
							(
								PROCESSOR_TYPE,
								STATUS
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

/**
 * Add INDEX for columns frequently used to read data in dropbox
 */
DECLARE
    i INTEGER;
BEGIN
    SELECT COUNT(*) INTO i FROM user_indexes WHERE index_name = 'IX03_STAGED_FILE' and table_owner='GATEWAY_OWNR' and table_name='STAGED_FILE';
    IF i = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX GATEWAY_OWNR.IX03_STAGED_FILE ON GATEWAY_OWNR.STAGED_FILE 
							(
								MAILBOX_GUID,
								STATUS
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