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

  IF NOT TablespaceExists('GATEWAY_CONF') THEN
    v_stmt := 'CREATE TABLESPACE GATEWAY_CONF';
    EXECUTE IMMEDIATE v_stmt;
  END IF;

  IF NOT TablespaceExists('GATEWAY_TEMP_01') THEN
    v_stmt := 'CREATE TEMPORARY TABLESPACE GATEWAY_TEMP_01 TEMPFILE SIZE 256M AUTOEXTEND ON NEXT 256M TABLESPACE GROUP GATEWAY_TEMP';
    EXECUTE IMMEDIATE v_stmt;
  END IF;

  IF NOT TablespaceExists('JUNK') THEN
    v_stmt := 'CREATE TABLESPACE JUNK DATAFILE SIZE 1M AUTOEXTEND OFF';
    EXECUTE IMMEDIATE v_stmt;
  END IF;
  
   IF NOT TablespaceExists('GATEWAY_TRNX') THEN
    v_stmt := 'CREATE TABLESPACE GATEWAY_TRNX';
    EXECUTE IMMEDIATE v_stmt;
  END IF;

END;
/

quit;
