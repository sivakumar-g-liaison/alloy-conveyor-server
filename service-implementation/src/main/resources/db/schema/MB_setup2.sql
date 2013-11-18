
-- executed as OWNR:

set serverout on
set echo on

-- create objects

@MB_ddl.sql

-- revoke all object privileges from APPL role
-- grant object privileges to APPL role

@MB_object_grants.sql

-- (re)create synonyms in APPL account

@MB_synonyms.sql

