--
-- Copyright 2015 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--

DECLARE
	num_rows integer;
BEGIN
	SELECT count(*) INTO num_rows FROM USER_CONSTRAINTS WHERE table_name = 'FSM_STATE' AND constraint_name = 'AK01_FSM_STATE';

	IF num_rows = 0 THEN
		EXECUTE IMMEDIATE
			'ALTER TABLE GATEWAY_OWNR.FSM_STATE ADD 
				CONSTRAINT AK01_FSM_STATE UNIQUE (EXECUTION_ID)
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
