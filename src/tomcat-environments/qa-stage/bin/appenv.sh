#! /bin/sh
# ==================================================================
# Set application specific system properties in this script
# ==================================================================

export APP_ENV="qa-stage"
export CATALINA_OPTS="$CATALINA_OPTS -Darchaius.deployment.environment=$APP_ENV"
export CATALINA_OPTS="$CATALINA_OPTS -Darchaius.deployment.applicationId=g2mailboxservice"

# ==================================================================
# log overrides
# ==================================================================

export CATALINA_OPTS="$CATALINA_OPTS -Dlog4j.configurationFile=log4j2-$APP_ENV.xml"
