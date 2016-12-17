--
-- Copyright 2014 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--

--Removing old mailbox constraint
DECLARE
	num_rows integer;
BEGIN
	SELECT count(*) INTO num_rows FROM USER_CONSTRAINTS WHERE table_name = 'MAILBOX' AND constraint_name = 'CK01_MAILBOX';

	IF num_rows = 1 THEN
		EXECUTE IMMEDIATE
			'ALTER TABLE GATEWAY_OWNR.MAILBOX DROP CONSTRAINT CK01_MAILBOX';
	END IF;
END;
/

--Adding mailbox constraint

ALTER TABLE GATEWAY_OWNR.MAILBOX
ADD CONSTRAINT CK01_MAILBOX CHECK (STATUS IN ('ACTIVE', 'INACTIVE', 'DELETED'));


--Removing old processor constraint if exist

DECLARE
	num_rows integer;
BEGIN
	SELECT count(*) INTO num_rows FROM USER_CONSTRAINTS WHERE table_name = 'PROCESSOR' AND constraint_name = 'CK02_PROCESSOR';

	IF num_rows = 1 THEN
		EXECUTE IMMEDIATE
			'ALTER TABLE GATEWAY_OWNR.PROCESSOR DROP CONSTRAINT CK02_PROCESSOR';
	END IF;
END;
/

--Adding processor constraint

ALTER TABLE GATEWAY_OWNR.PROCESSOR
ADD CONSTRAINT CK02_PROCESSOR CHECK (STATUS IN ('ACTIVE', 'INACTIVE', 'DELETED'));