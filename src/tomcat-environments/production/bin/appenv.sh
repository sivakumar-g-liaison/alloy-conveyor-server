#! /bin/sh
# ==================================================================
# Set application specific system properties in this script
# ==================================================================
export APP_ENV="production"
export APP_NAME="g2mailboxservice"
export CATALINA_OPTS="$CATALINA_OPTS -Darchaius.deployment.environment=$APP_ENV"
export CATALINA_OPTS="$CATALINA_OPTS -Darchaius.deployment.applicationId=$APP_NAME"

# Path to secure property file. The secure properties file must be manually copied to deployment server.
export CATALINA_OPTS="$CATALINA_OPTS -Darchaius.configurationSource.additionalUrls=file:///secure/tomcat/g2mailboxservice-dev-int-secure.properties"

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
export CATALINA_OPTS="$CATALINA_OPTS -Dlog4j.configurationFile=log4j2-$APP_ENV.xml"
# Shortcode for APP_NAME:
export SYSLOG_APPNAME="g2mailboxservice"
export SYSLOG_HOST=[REQUIRED]
export SYSLOG_PORT=[REQUIRED]
export SYSLOG_PROTO="UDP"
export SYSLOG_ENTERPRISE_NUMBER="99999"
export SYSLOG_ID="App"
export SYSLOG_MESSAGE_ID="AUDIT"
export SYSLOG_FACILITY="Local0"
 
export GLASS_SPECTRUM_PORT=[REQUIRED]
export GLASS_SPECTRUM_IP=[REQUIRED]
export GLASS_SPECTRUM_USER=[REQUIRED]
export GLASS_SPECTRUM_PASSWORD=[REQUIRED]
export GLASS_SPECTRUM_DATASPACE=[REQUIRED]
export GLASS_SPECTRUM_SOURCE_NAME=$APP_ENV
 
 
export GLASS_METRIC_PORT=[REQUIRED]
export GLASS_METRIC_IP=[REQUIRED]
export GLASS_METRIC_USER=[REQUIRED]
export GLASS_METRIC_PASSWORD=[REQUIRED]
export GLASS_METRIC_DATASPACE=[REQUIRED]
export GLASS_METRIC_DATAOBJECTNAME=MetricsGM
export GLASS_METRIC_SOURCE_NAME=$APP_ENV
export GSA_LENS_IP=[REQUIRED]
 
export HOST_IPS=$(ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | sed 'N;s/\n/, /;')
export LOCAL_HOSTNAME=$(hostname)
export DAEMON_USER=$(whoami)
export LOGIN_USER=$(who am i | awk '{print $1}')
 
export LOGSTASHMACHINE=[REQUIRED]
export LOGSTASHPORT=[REQUIRED]
export LOGSTASH_LOG_DIR=/opt/liaison/
# ==================================================================
# log overrides
# ==================================================================