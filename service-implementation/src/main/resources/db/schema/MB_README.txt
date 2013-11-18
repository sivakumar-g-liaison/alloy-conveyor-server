
executed as SYS/SYSTEM:
	create tablespaces				MB_ts.sql
	create roles					MB_users.sql
	revoke system privileges from roles		"
	grant system privileges to roles		"
	create users					"
	follow-on ALTER statements			MB_users2.sql
executed as OWNR:
	create objects					MB_ddl.sql
	revoke all object privileges from APPL role	MB_object_grants.sql
	grant object privileges to APPL role		"
	(re)create synonyms in APPL account		MB_synonyms.sql

