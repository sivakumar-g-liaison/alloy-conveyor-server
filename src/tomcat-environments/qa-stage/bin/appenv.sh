#! /bin/sh
# ==================================================================
# Set application specific system properties in this script
# ==================================================================

export CATALINA_OPTS="$CATALINA_OPTS -Darchaius.deployment.applicationId=g2mailboxservice"
export CATALINA_OPTS="$CATALINA_OPTS -Darchaius.deployment.environment=qa"