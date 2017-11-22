#! /bin/sh
# ==================================================================
# Set application specific system properties in this script
# ==================================================================
#Enabled Remote Debugging
export CATALINA_OPTS="$CATALINA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
echo "loading g2mailboxservice-dev-int-at4-dev-pres.sh"