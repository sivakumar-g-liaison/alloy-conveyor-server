@echo off
rem ==================================================================
rem Set application specific system properties in this script
rem ==================================================================
set APP_ENV=dev-int-dbx
set APP_NAME=g2mailboxservice
set CATALINA_OPTS=%CATALINA_OPTS% -Darchaius.deployment.environment=%APP_ENV%
set CATALINA_OPTS=%CATALINA_OPTS% -Darchaius.deployment.applicationId=%APP_NAME%
rem Make all loggers ASYNC
set CATALINA_OPTS=%CATALINA_OPTS% -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
 
rem Include Log Location (line number and file of origin)
rem This might be too slow for production, but is really great while in beta.
rem Note, this is included as property in case we need to build an interface
rem to override while live
set CATALINA_OPTS=%CATALINA_OPTS% -Dcom.liaison.log4j2.includeLocation=true
rem log4j will look for updates to log4j xml every 5 minutes
set CATALINA_OPTS=%CATALINA_OPTS% -Dcom.liaison.log4j2.configuration.refresh.interval.sec=300
rem ==================================================================
rem log overrides
rem ==================================================================
set CATALINA_OPTS=%CATALINA_OPTS% -Dlog4j.configurationFile=log4j2-%APP_ENV%.xml
rem Shortcode for APP_NAME:
set SYSLOG_APPNAME=g2mailboxservice
set SYSLOG_HOST=audit-syslog-server
set SYSLOG_PORT=514
set SYSLOG_PROTO=UDP
set SYSLOG_ENTERPRISE_NUMBER=99999
set SYSLOG_ID=App
set SYSLOG_MESSAGE_ID=AUDIT
set SYSLOG_FACILITY=Local0
 
set GLASS_SPECTRUM_PORT=10010
set GLASS_SPECTRUM_IP=192.168.0.67
set GLASS_SPECTRUM_USER=g2seattle
set GLASS_SPECTRUM_PASSWORD=g2seattle
set GLASS_SPECTRUM_DATASPACE=g2-seattle
set GLASS_SPECTRUM_SOURCE_NAME=%APP_ENV%
 
 
set GLASS_METRIC_PORT=10010
set GLASS_METRIC_IP=192.168.0.67
set GLASS_METRIC_USER=g2tampere
set GLASS_METRIC_PASSWORD=g2tampere
set GLASS_METRIC_DATASPACE=g2-tampere
set GLASS_METRIC_DATAOBJECTNAME=MetricsGM
set GLASS_METRIC_SOURCE_NAME=%APP_ENV%
set GSA_LENS_IP=192.168.0.10
 
set HOST_IPS=GANESHRAM_WINDOWS
set LOCAL_HOSTNAME=GANESHRAM_WINDOWS
set DAEMON_USER=GANESHRAM_WINDOWS
set LOGIN_USER=GANESHRAM_WINDOWS
 
set LOGSTASHMACHINE=lsvllogst01d.liaison.dev
set LOGSTASHPORT=4560
set LOGSTASH_LOG_DIR=/tmp
rem ==================================================================
rem log overrides
rem ==================================================================
