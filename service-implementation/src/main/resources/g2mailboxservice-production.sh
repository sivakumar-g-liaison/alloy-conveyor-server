#! /bin/sh
# ==================================================================
# Set application specific system properties in this script
# ==================================================================
export CATALINA_OPTS="$CATALINA_OPTS -Darchaius.deployment.environment=$ENVIRONMENT"
export CATALINA_OPTS="$CATALINA_OPTS -Darchaius.deployment.applicationId=$APP_ID"

# Path to secure property file. The secure properties file must be manually copied to deployment server.
export CATALINA_OPTS="$CATALINA_OPTS -Darchaius.configurationSource.additionalUrls=file:///secure/tomcat/secure.properties"

# Make all loggers ASYNC
export CATALINA_OPTS="$CATALINA_OPTS -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector"

# Include Log Location (line number and file of origin)
# This might be too slow for production, but is really great while in beta.
# Note, this is included as property in case we need to build an interface
# to override while live
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.liaison.log4j2.includeLocation=true"
# log4j will look for updates to log4j xml every 5 minutes
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.liaison.log4j2.configuration.refresh.interval.sec=300"

# ==================================================================
# log overrides
# ==================================================================
export CATALINA_OPTS="$CATALINA_OPTS -Dlog4j.configurationFile=log4j2-$ENVIRONMENT.xml"
# Shortcode for APP_NAME:
export SYSLOG_APPNAME="GMB"
export SYSLOG_HOST="10.152.20.9"
export SYSLOG_PORT="514"
export SYSLOG_PROTO="UDP"
export SYSLOG_ENTERPRISE_NUMBER="99999"
export SYSLOG_ID="App"
export SYSLOG_MESSAGE_ID="AUDIT"
export SYSLOG_FACILITY="Local0"
export SYSLOG_ROLLING_SIZE="250 MB"
export SYSLOG_LOG_DIR="/var/log/tomcat/syslog_failover"
export SYSLOG_RING_BUFFER_SIZE=128
 
export GLASS_LOG_DIR="/var/log/tomcat/glass_failover"
export LENS_LOG_DIR="/var/log/tomcat/lens_failover"

export HOST_IPS=$(ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | sed 'N;s/\n/, /;')
export LOCAL_HOSTNAME=$(hostname)
export DAEMON_USER=$(whoami)
export LOGIN_USER=$(who am i | awk '{print $1}')
 
export LOGSTASHMACHINE=at4p-vplstash.liaison.prod
export LOGSTASHPORT=4560
export LOGSTASH_ROLLING_SIZE="250 MB"

export LOGSTASH_LOG_DIR=/var/log/tomcat/logstash_failover
export LOGSTASH_RING_BUFFER_SIZE=262144
# ==================================================================
# log overrides
# ==================================================================

# Https.protocols parameter for JVM
export CATALINA_OPTS="$CATALINA_OPTS -Dhttps.protocols=TLSv1.2"

# MAPR Service Ticket
export MAPR_TICKETFILE_LOCATION=/var/mapr/g2app-mapr-service-ticket