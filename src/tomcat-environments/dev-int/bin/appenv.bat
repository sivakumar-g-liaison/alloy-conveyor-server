@echo off
:: ==================================================================
:: Set application specific system properties in this script
:: ==================================================================

set CATALINA_OPTS="%CATALINA_OPTS% -Darchaius.deployment.applicationId=g2mailboxservice"
set CATALINA_OPTS="%CATALINA_OPTS% -Darchaius.deployment.environment=dev-int"
