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

ALTER USER NDEV_GATEWAY_RTDM_OWNR
    QUOTA 0 ON JUNK
    QUOTA 0 ON SYSTEM
    QUOTA 0 ON SYSAUX
    QUOTA UNLIMITED ON NDEV_GATEWAY_RTDM_CONF
    QUOTA UNLIMITED ON NDEV_GATEWAY_RTDM_TRNX
;

ALTER USER NDEV_GATEWAY_RTDM_APPL
    QUOTA 0 ON JUNK
    QUOTA 0 ON SYSTEM
    QUOTA 0 ON SYSAUX
    QUOTA 0 ON NDEV_GATEWAY_RTDM_CONF
    QUOTA 0 ON NDEV_GATEWAY_RTDM_TRNX
;

quit;
