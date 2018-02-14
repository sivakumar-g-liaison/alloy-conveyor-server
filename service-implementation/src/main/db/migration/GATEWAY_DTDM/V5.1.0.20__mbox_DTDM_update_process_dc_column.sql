--
-- Copyright 2017 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--

UPDATE
    GATEWAY_OWNR.PROCESSOR
SET
    GATEWAY_OWNR.PROCESSOR.PROCESS_DC = GATEWAY_OWNR.PROCESSOR.ORIGINATING_DC
WHERE
    GATEWAY_OWNR.PROCESSOR.TYPE = 'REMOTEDOWNLOADER';
commit;
	
UPDATE
    GATEWAY_OWNR.PROCESSOR
SET
    GATEWAY_OWNR.PROCESSOR.PROCESS_DC = 'ALL'
WHERE
    GATEWAY_OWNR.PROCESSOR.TYPE <> 'REMOTEDOWNLOADER';
commit;	

ALTER TABLE GATEWAY_OWNR.PROCESSOR MODIFY (PROCESS_DC VARCHAR2(16) NOT NULL);

