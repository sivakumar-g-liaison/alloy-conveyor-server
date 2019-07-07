--
-- Copyright 2019 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--

--Modifying the mailbox unique constraint
DECLARE
    num_rows integer;
BEGIN
    SELECT count(*) INTO num_rows FROM USER_CONSTRAINTS WHERE table_name = 'MAILBOX' AND constraint_name = 'AK01_MAILBOX' AND owner = 'GATEWAY_OWNR';

    IF num_rows = 1 THEN
        EXECUTE IMMEDIATE
        'ALTER TABLE GATEWAY_OWNR.MAILBOX DROP CONSTRAINT AK01_MAILBOX DROP INDEX ';

        EXECUTE IMMEDIATE
            'ALTER TABLE GATEWAY_OWNR.MAILBOX ADD 
                CONSTRAINT AK01_MAILBOX UNIQUE (NAME, TENANCY_KEY, CLUSTER_TYPE)
                    USING INDEX
                        TABLESPACE GATEWAY_CONF
                        STORAGE
                        (
                            PCTINCREASE 0
                        )
            ';
    END IF;
END;
/
