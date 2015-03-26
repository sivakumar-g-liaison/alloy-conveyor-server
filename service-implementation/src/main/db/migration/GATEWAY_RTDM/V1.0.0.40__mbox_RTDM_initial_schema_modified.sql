DECLARE table_count NUMBER;
BEGIN
  SELECT count(*) INTO table_count FROM all_tables WHERE table_name = 'STAGED_FILE';
  IF table_count = 0 THEN 
    EXECUTE IMMEDIATE 'CREATE TABLE GATEWAY_OWNR.STAGED_FILE
   			(
			   	PGUID CHAR(32 BYTE) NOT NULL, 
				MAILBOX_GUID CHAR(32 BYTE) NOT NULL, 
				SPECTRUM_URI VARCHAR2(512 BYTE) NOT NULL, 
				FILE_NAME VARCHAR2(512 BYTE), 
				FILE_SIZE VARCHAR2(20 BYTE), 
				FILE_PATH VARCHAR2(512 BYTE),
			 	META VARCHAR2(512 BYTE), 
			 	STATUS VARCHAR2(16) DEFAULT ''ACTIVE'' CONSTRAINT CK01_STAGEDFILE CHECK (STATUS IN (''ACTIVE'', ''INACTIVE'')),
				AVAILABLE_UNTIL TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
  		 	)
  		 	TABLESPACE GATEWAY_TRNX
			STORAGE
			(
				PCTINCREASE 0
			)
			NOPARALLEL'
	;
	EXECUTE IMMEDIATE 'CREATE OR REPLACE SYNONYM GATEWAY_APPL.STAGED_FILE FOR GATEWAY_OWNR.STAGED_FILE';
	EXECUTE IMMEDIATE 'GRANT SELECT ON GATEWAY_OWNR.STAGED_FILE TO GATEWAY_APPL_ROLE'; 
	EXECUTE IMMEDIATE 'GRANT INSERT ON GATEWAY_OWNR.STAGED_FILE TO GATEWAY_APPL_ROLE';
	EXECUTE IMMEDIATE 'GRANT UPDATE ON GATEWAY_OWNR.STAGED_FILE TO GATEWAY_APPL_ROLE';
	EXECUTE IMMEDIATE 'GRANT DELETE ON GATEWAY_OWNR.STAGED_FILE TO GATEWAY_APPL_ROLE';
  END IF;
END;
/

DECLARE
	num_rows integer;
BEGIN
	SELECT count(*) INTO num_rows FROM USER_CONSTRAINTS WHERE table_name = 'STAGED_FILE' AND constraint_name = 'CK01_STAGEDFILE';

	IF num_rows = 0 THEN
		EXECUTE IMMEDIATE 'ALTER TABLE GATEWAY_OWNR.STAGED_FILE ADD (STATUS VARCHAR2(16) DEFAULT ''ACTIVE'' CONSTRAINT CK01_STAGEDFILE CHECK (STATUS IN (''ACTIVE'', ''INACTIVE'')))';
		EXECUTE IMMEDIATE 'ALTER TABLE GATEWAY_OWNR.STAGED_FILE ADD AVAILABLE_UNTIL TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP';
 	END IF;
END;
/
