#! /bin/sh
# ==================================================================
# Set application specific system properties in this script
# ==================================================================

export CATALINA_OPTS="$CATALINA_OPTS -Darchaius.deployment.applicationId=g2mailboxservice"
export CATALINA_OPTS="$CATALINA_OPTS -Darchaius.deployment.environment=dev-int"

### LOGGING
export CATALINA_OPTS="$CATALINA_OPTS -Dlog4j.configurationFile=log4j2-dev-int.xml"