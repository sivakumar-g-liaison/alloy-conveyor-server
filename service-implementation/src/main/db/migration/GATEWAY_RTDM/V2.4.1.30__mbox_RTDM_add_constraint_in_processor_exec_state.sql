--
-- Copyright 2016 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--


--Add FOREIGN KEY constraint

DECLARE
  num_rows integer;
BEGIN
  SELECT count(*) INTO num_rows FROM USER_CONSTRAINTS WHERE table_name = 'PROCESSOR_EXEC_STATE' AND constraint_name = 'FK01_PROCESSOR_EXEC_STATE';

  IF num_rows = 0 THEN
    EXECUTE IMMEDIATE
      'ALTER TABLE GATEWAY_OWNR.PROCESSOR_EXEC_STATE ADD
	CONSTRAINT FK01_PROCESSOR_EXEC_STATE FOREIGN KEY (PGUID)
		REFERENCES GATEWAY_OWNR.PROCESSORS (PGUID)';
  END IF;
END;
/


