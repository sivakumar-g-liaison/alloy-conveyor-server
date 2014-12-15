--
-- Copyright 2014 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--

--
-- Test for the existence of service-related tablespaces
-- and create if absent
--

set serverout on
set echo on

DECLARE
  v_stmt    VARCHAR2(4000);

FUNCTION TablespaceExists(
  p_Name IN DBA_TABLESPACES.Tablespace_Name%Type
) RETURN BOOLEAN
IS
  v_ts  DBA_TABLESPACES.Tablespace_Name%Type;
BEGIN
  SELECT Tablespace_Name
    INTO v_ts
    FROM DBA_TABLESPACES
    WHERE Tablespace_Name = p_Name;
  RETURN TRUE;
EXCEPTION
  WHEN NO_DATA_FOUND
  THEN
    RETURN FALSE;
END TablespaceExists;

BEGIN

  IF NOT TablespaceExists('NDEV_GATEWAY_DTDM_CONF') THEN
    v_stmt := 'CREATE TABLESPACE NDEV_GATEWAY_DTDM_CONF';
    EXECUTE IMMEDIATE v_stmt;
  END IF;

  IF NOT TablespaceExists('NDEV_GATEWAY_DTDM_TEMP_01') THEN
    v_stmt := 'CREATE TEMPORARY TABLESPACE NDEV_GATEWAY_DTDM_TEMP_01 TEMPFILE SIZE 256M AUTOEXTEND ON NEXT 256M TABLESPACE GROUP NDEV_GATEWAY_DTDM_TEMP';
    EXECUTE IMMEDIATE v_stmt;
  END IF;

  IF NOT TablespaceExists('JUNK') THEN
    v_stmt := 'CREATE TABLESPACE JUNK DATAFILE SIZE 2M AUTOEXTEND OFF';
    EXECUTE IMMEDIATE v_stmt;
  END IF;
  
   IF NOT TablespaceExists('NDEV_GATEWAY_DTDM_TRNX') THEN
    v_stmt := 'CREATE TABLESPACE NDEV_GATEWAY_DTDM_TRNX';
    EXECUTE IMMEDIATE v_stmt;
  END IF;

END;
/

quit;
