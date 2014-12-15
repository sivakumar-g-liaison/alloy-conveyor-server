--
-- Copyright 2014 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--

--
-- Test for the existence of service-related user
-- profiles and create if absent.
--

set serverout on
set echo on

DECLARE
  v_stmt    VARCHAR2(4000);

FUNCTION ProfileExists(
  p_name IN DBA_PROFILES.profile%Type
) RETURN BOOLEAN
IS
  v_count int;
BEGIN
  SELECT count(*)
    INTO v_count
    FROM DBA_PROFILES
    WHERE profile = p_name;
  RETURN v_count > 0;
EXCEPTION
  WHEN NO_DATA_FOUND
  THEN
    RETURN FALSE;
END ProfileExists;

BEGIN

  IF NOT ProfileExists('NDEV_GATEWAY_RTDM_APPL_PROFILE') THEN
    v_stmt := 'CREATE PROFILE NDEV_GATEWAY_RTDM_APPL_PROFILE LIMIT IDLE_TIME 60 PASSWORD_LIFE_TIME UNLIMITED';
    EXECUTE IMMEDIATE v_stmt;
  END IF;

  IF NOT ProfileExists('NDEV_GATEWAY_RTDM_OWNR_PROFILE') THEN
    v_stmt := 'CREATE PROFILE NDEV_GATEWAY_RTDM_OWNR_PROFILE LIMIT IDLE_TIME 60 PASSWORD_LIFE_TIME UNLIMITED';
    EXECUTE IMMEDIATE v_stmt;
  END IF;

END;
/

quit;
