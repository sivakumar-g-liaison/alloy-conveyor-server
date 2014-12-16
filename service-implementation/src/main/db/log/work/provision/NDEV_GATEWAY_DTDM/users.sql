--
-- Copyright 2014 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--

--
-- Test for the existence of service-related users
-- and roles and create if absent. Grant necessary
-- system privileges to roles.
--

set serverout on
set echo on

DECLARE
  v_stmt    VARCHAR2(4000);

FUNCTION UserExists(
  p_UserName IN DBA_USERS.UserName%Type
) RETURN BOOLEAN
IS
  v_user  DBA_USERS.UserName%Type;
BEGIN
  SELECT UserName
    INTO v_user
    FROM DBA_USERS
    WHERE UserName = p_UserName;
  RETURN TRUE;
EXCEPTION
  WHEN NO_DATA_FOUND
  THEN
    RETURN FALSE;
END UserExists;

FUNCTION RoleExists(
  p_role IN DBA_ROLES.Role%Type
) RETURN BOOLEAN
IS
  v_role  DBA_ROLES.Role%Type;
BEGIN
  SELECT Role
    INTO v_role
    FROM DBA_ROLES
    WHERE Role = p_role;
  RETURN TRUE;
EXCEPTION
  WHEN NO_DATA_FOUND
  THEN
    RETURN FALSE;
END RoleExists;

PROCEDURE ResetRole(
  p_role IN DBA_SYS_PRIVS.Grantee%Type
)
IS
BEGIN
  FOR c in (
  SELECT
      UNIQUE privilege, grantee
    FROM
      DBA_SYS_PRIVS
    WHERE
      grantee = p_role
  )
  LOOP
    EXECUTE IMMEDIATE 'REVOKE ' || c.privilege || ' FROM ' || c.grantee;
  END LOOP;
END ResetRole;

BEGIN

  IF NOT RoleExists('NDEV_GATEWAY_DTDM_APPL_ROLE') THEN
    v_stmt := 'CREATE ROLE NDEV_GATEWAY_DTDM_APPL_ROLE';
    EXECUTE IMMEDIATE v_stmt;
  END IF;

  ResetRole('NDEV_GATEWAY_DTDM_APPL_ROLE');

  v_stmt := 'GRANT CREATE SESSION TO NDEV_GATEWAY_DTDM_APPL_ROLE';
  EXECUTE IMMEDIATE v_stmt;

  IF NOT RoleExists('NDEV_GATEWAY_DTDM_OWNR_ROLE') THEN
    v_stmt := 'CREATE ROLE NDEV_GATEWAY_DTDM_OWNR_ROLE';
    EXECUTE IMMEDIATE v_stmt;
  END IF;

  ResetRole('NDEV_GATEWAY_DTDM_OWNR_ROLE');

  v_stmt := 'GRANT CREATE SESSION TO NDEV_GATEWAY_DTDM_OWNR_ROLE';
  EXECUTE IMMEDIATE v_stmt;
  v_stmt := 'GRANT CREATE ANY SYNONYM TO NDEV_GATEWAY_DTDM_OWNR_ROLE';
  EXECUTE IMMEDIATE v_stmt;
  v_stmt := 'GRANT CREATE TABLE TO NDEV_GATEWAY_DTDM_OWNR_ROLE';
  EXECUTE IMMEDIATE v_stmt;

  IF NOT UserExists('NDEV_GATEWAY_DTDM_APPL') THEN
    v_stmt := 'CREATE USER NDEV_GATEWAY_DTDM_APPL IDENTIFIED BY 12345678 DEFAULT TABLESPACE JUNK TEMPORARY TABLESPACE NDEV_GATEWAY_DTDM_TEMP PROFILE NDEV_GATEWAY_DTDM_APPL_PROFILE';
    EXECUTE IMMEDIATE v_stmt;
  END IF;

  v_stmt := 'GRANT NDEV_GATEWAY_DTDM_APPL_ROLE TO NDEV_GATEWAY_DTDM_APPL';
  EXECUTE IMMEDIATE v_stmt;
  v_stmt := 'ALTER USER NDEV_GATEWAY_DTDM_APPL DEFAULT ROLE NDEV_GATEWAY_DTDM_APPL_ROLE';
  EXECUTE IMMEDIATE v_stmt;

  IF NOT UserExists('NDEV_GATEWAY_DTDM_OWNR') THEN
    v_stmt := 'CREATE USER NDEV_GATEWAY_DTDM_OWNR IDENTIFIED BY 12345678 DEFAULT TABLESPACE JUNK TEMPORARY TABLESPACE NDEV_GATEWAY_DTDM_TEMP PROFILE NDEV_GATEWAY_DTDM_OWNR_PROFILE';
    EXECUTE IMMEDIATE v_stmt;
  END IF;

  v_stmt := 'GRANT NDEV_GATEWAY_DTDM_OWNR_ROLE TO NDEV_GATEWAY_DTDM_OWNR';
  EXECUTE IMMEDIATE v_stmt;
  v_stmt := 'GRANT NDEV_GATEWAY_DTDM_APPL_ROLE TO NDEV_GATEWAY_DTDM_OWNR';
  EXECUTE IMMEDIATE v_stmt;
  v_stmt := 'ALTER USER NDEV_GATEWAY_DTDM_OWNR DEFAULT ROLE NDEV_GATEWAY_DTDM_OWNR_ROLE';
  EXECUTE IMMEDIATE v_stmt;

END;
/

quit;
