Liaison Configurable Data Source Factory
========================================

Optional configuration settings
-------------------------------


The following settings are optionally provided via the LiaisonConfigurationFactory/DecryptableConfiguration classes (available in the Commons lib) to configure the LiaisonConfigurableDataSourceFactory.

1. com.liaison.DB_INACTIVECONNECTIONTIMEOUT (Integer)
2. com.liaison.DB_VALIDATECONNECTIONONBORROW (Boolean, see below)
3. com.liaison.DB_DESCRIPTION (String)
4. com.liaison.DB_ABANDONEDCONNECTIONTIMEOUT (Integer)
5. com.liaison.DB_CONNECTIONHARVESTMAXCOUNT (Integer)
6. com.liaison.DB_CONNECTIONHARVESTTRIGGERCOUNT (Integer)
7. com.liaison.DB_CONNECTIONWAITTIMEOUT (Integer)
8. com.liaison.DB_FASTCONNECTIONFAILOVERENABLED (Boolean)
9. com.liaison.DB_INITIALPOOLSIZE (Integer)
10. com.liaison.DB_MAXCONNECTIONREUSECOUNT (Integer)
11. com.liaison.DB_MAXCONNECTIONREUSETIME (Integer)
12. com.liaison.DB_MAXIDLETIME (Integer)
13. com.liaison.DB_TIMEOUTCHECKINTERVAL (Integer)
14. com.liaison.DB_TIMETOLIVECONNECTIONTIMEOUT (Integer)

Settings that are of type "Boolean" must have a value of "True" or "False" (case insensitive) to be valid.
