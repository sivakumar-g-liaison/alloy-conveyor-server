--
-- Copyright 2014 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--


--Removing old processor constraint
DECLARE
	num_rows integer;
BEGIN
	SELECT count(*) INTO num_rows FROM USER_CONSTRAINTS WHERE table_name = 'PROCESSOR' AND constraint_name = 'NN07_PROCESSOR';

	IF num_rows = 1 THEN
		EXECUTE IMMEDIATE 'ALTER TABLE GATEWAY_OWNR.PROCESSOR DROP CONSTRAINT NN07_PROCESSOR';
 	END IF;
END;
/

--Removing old folder constraint
DECLARE
	num_rows integer;
BEGIN
	SELECT count(*) INTO num_rows FROM USER_CONSTRAINTS WHERE table_name = 'FOLDER' AND constraint_name = 'NN03_FOLDER';

	IF num_rows = 1 THEN
		EXECUTE IMMEDIATE 'ALTER TABLE GATEWAY_OWNR.FOLDER DROP CONSTRAINT NN03_FOLDER';
 	END IF;
END;
/

--Removing old mailbox constraint
DECLARE
	num_rows integer;
BEGIN
	SELECT count(*) INTO num_rows FROM USER_CONSTRAINTS WHERE table_name = 'MAILBOX' AND constraint_name = 'NN05_MAILBOX';

	IF num_rows = 1 THEN
		EXECUTE IMMEDIATE 'ALTER TABLE GATEWAY_OWNR.MAILBOX DROP CONSTRAINT NN05_MAILBOX';
 	END IF;
END;
/

