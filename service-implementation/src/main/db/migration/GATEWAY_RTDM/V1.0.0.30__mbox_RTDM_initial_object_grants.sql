--
-- Recreate service-related object roles
--

DECLARE
  v_stmt    VARCHAR2(4000);

FUNCTION RoleGranted(
  p_role IN USER_ROLE_PRIVS.Granted_Role%Type
) RETURN BOOLEAN
IS
  v_role  USER_ROLE_PRIVS.Granted_Role%Type;
BEGIN
  SELECT Granted_Role
    INTO v_role
    FROM USER_ROLE_PRIVS
    WHERE Granted_Role = p_role;
  RETURN TRUE;
EXCEPTION
  WHEN NO_DATA_FOUND
  THEN
    RETURN FALSE;
END RoleGranted;

PROCEDURE ResetRole(
  p_role IN USER_ROLE_PRIVS.Granted_Role%Type
)
IS
BEGIN
  FOR c in (
  SELECT
      UNIQUE table_name, grantee
    FROM
      USER_TAB_PRIVS
    WHERE
      table_name NOT IN ( SELECT object_name FROM recyclebin ) AND
      grantee = p_role
  )
  LOOP
    EXECUTE IMMEDIATE 'REVOKE ALL ON ' || c.table_name || ' FROM ' || c.grantee;
  END LOOP;
END ResetRole;

BEGIN

  IF RoleGranted('GATEWAY_APPL_ROLE') THEN
    ResetRole('GATEWAY_APPL_ROLE');

    v_stmt := 'GRANT SELECT ON GATEWAY_OWNR.FSM_TRANSITION_STATE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT INSERT ON GATEWAY_OWNR.FSM_TRANSITION_STATE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT UPDATE ON GATEWAY_OWNR.FSM_TRANSITION_STATE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT SELECT ON GATEWAY_OWNR.FSM_TRANSITION_LIST TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT INSERT ON GATEWAY_OWNR.FSM_TRANSITION_LIST TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT UPDATE ON GATEWAY_OWNR.FSM_TRANSITION_LIST TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT SELECT ON GATEWAY_OWNR.FSM_STATE_VALUE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT INSERT ON GATEWAY_OWNR.FSM_STATE_VALUE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT UPDATE ON GATEWAY_OWNR.FSM_STATE_VALUE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT SELECT ON GATEWAY_OWNR.FSM_STATE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT INSERT ON GATEWAY_OWNR.FSM_STATE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT UPDATE ON GATEWAY_OWNR.FSM_STATE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT SELECT ON GATEWAY_OWNR.FSM_EVENT TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT INSERT ON GATEWAY_OWNR.FSM_EVENT TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT UPDATE ON GATEWAY_OWNR.FSM_EVENT TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT SELECT ON GATEWAY_OWNR.PROCESSOR_EXEC_STATE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT INSERT ON GATEWAY_OWNR.PROCESSOR_EXEC_STATE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT UPDATE ON GATEWAY_OWNR.PROCESSOR_EXEC_STATE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT DELETE ON GATEWAY_OWNR.PROCESSOR_EXEC_STATE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT SELECT ON GATEWAY_OWNR.STAGED_FILE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT INSERT ON GATEWAY_OWNR.STAGED_FILE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT UPDATE ON GATEWAY_OWNR.STAGED_FILE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
    v_stmt := 'GRANT DELETE ON GATEWAY_OWNR.STAGED_FILE TO GATEWAY_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;

  END IF;

  IF RoleGranted('GATEWAY_OWNR_ROLE') THEN
    ResetRole('GATEWAY_OWNR_ROLE');
  END IF;

END;
/
