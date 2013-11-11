
-- executed as SYS/SYSTEM:

set serverout on
set echo on

-- (optionally) create tablespaces

@MB_ts.sql

-- (optionally) create roles
-- revoke system privileges from roles
-- grant system privileges to roles
-- (optionallly) create users

@MB_users.sql

-- follow-on ALTER statements (hand-crafted, not yet supported by ModelRight)

@MB_users2.sql

