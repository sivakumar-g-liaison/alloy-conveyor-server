--
-- Copyright 2014 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--

--
-- Apply quotas to users.
--

set serverout on
set echo on

ALTER USER GATEWAY_OWNR
    QUOTA 0 ON JUNK
    QUOTA 0 ON SYSTEM
    QUOTA 0 ON SYSAUX
    QUOTA UNLIMITED ON GATEWAY_CONF
    QUOTA UNLIMITED ON GATEWAY_TRNX
;

ALTER USER GATEWAY_APPL
    QUOTA 0 ON JUNK
    QUOTA 0 ON SYSTEM
    QUOTA 0 ON SYSAUX
    QUOTA 0 ON GATEWAY_CONF
    QUOTA 0 ON GATEWAY_TRNX
;

quit;
