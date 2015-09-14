--
-- Copyright 2015 Liaison Technologies, Inc.
-- This software is the confidential and proprietary information of
-- Liaison Technologies, Inc. ("Confidential Information").  You shall
-- not disclose such Confidential Information and shall use it only in
-- accordance with the terms of the license agreement you entered into
-- with Liaison Technologies.
--
/**
 * Replacing data nuclues special characters with null
 * 
 * datanucleus.rdbms.persistEmptyStringAsNull
 *	Description	When persisting en empty string, should it be persisted as null in the datastore. This is to allow for datastores (Oracle) that dont differentiate between null and empty string. If it is set to false and the datastore doesnt differentiate then a special character will be saved when storing an empty string.
 *	Range of Values	true | false
 */
UPDATE GATEWAY_OWNR.MAILBOX SET DESCRIPTION = NULL WHERE DESCRIPTION LIKE CHR(01);
UPDATE GATEWAY_OWNR.MAILBOX SET SHARD_KEY = NULL WHERE SHARD_KEY LIKE CHR(01);
commit;

UPDATE GATEWAY_OWNR.CREDENTIAL SET URI_GUID = NULL WHERE URI_GUID LIKE CHR(01);
UPDATE GATEWAY_OWNR.CREDENTIAL SET USERNAME = NULL WHERE USERNAME LIKE CHR(01);
UPDATE GATEWAY_OWNR.CREDENTIAL SET IDP_TYPE = NULL WHERE IDP_TYPE LIKE CHR(01);
UPDATE GATEWAY_OWNR.CREDENTIAL SET IDP_URI = NULL WHERE IDP_URI LIKE CHR(01);
commit;

UPDATE GATEWAY_OWNR.FOLDER SET DESCRIPTION = NULL WHERE DESCRIPTION LIKE CHR(01);
commit;

UPDATE GATEWAY_OWNR.PROCESSOR SET DESCRIPTION = NULL WHERE DESCRIPTION LIKE CHR(01);
UPDATE GATEWAY_OWNR.PROCESSOR SET JAVASCRIPT_URI = NULL WHERE JAVASCRIPT_URI LIKE CHR(01);
commit;

