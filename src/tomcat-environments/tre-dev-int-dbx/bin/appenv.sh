#! /bin/sh
# ==================================================================
# Set application specific system properties in this script
# ==================================================================
export APP_ENV="tre-dev-int-dbx"
export APP_NAME="g2mailboxservice"
export CATALINA_OPTS="$CATALINA_OPTS -Darchaius.deployment.environment=$APP_ENV"
export CATALINA_OPTS="$CATALINA_OPTS -Darchaius.deployment.applicationId=$APP_NAME"

# Path to secure property file. The secure properties file must be manually copied to deployment server.
#export CATALINA_OPTS="$CATALINA_OPTS -Darchaius.configurationSource.additionalUrls=file:///secure/tomcat/g2mailboxservice-dev-int-secure.properties"

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
export SYSLOG_HOST="audit-syslog-server"
export SYSLOG_PORT="514"
export SYSLOG_PROTO="UDP"
export SYSLOG_ENTERPRISE_NUMBER="99999"
export SYSLOG_ID="App"
export SYSLOG_MESSAGE_ID="AUDIT"
export SYSLOG_FACILITY="Local0"
export SYSLOG_ROLLING_SIZE="250 MB"
export SYSLOG_LOG_DIR="/var/log/tomcat/syslog_failover"
export SYSLOG_RING_BUFFER_SIZE=262144

export GLASS_SPECTRUM_PORT=10010
export GLASS_SPECTRUM_IP=192.168.0.67
export GLASS_SPECTRUM_USER=g2tampere
export GLASS_SPECTRUM_PASSWORD=g2tampere
export GLASS_SPECTRUM_DATASPACE=g2-tampere
export GLASS_SPECTRUM_SOURCE_NAME=G2_Lab
export GLASS_SPECTRUM_MESSAGE_TTL=630720000
export GLASS_LOG_DIR="/var/log/tomcat/glass_failover"
export GLASS_ROLLING_SIZE="250 MB"
export GLASS_RING_BUFFER_SIZE=262144

export LENS_LOG_DIR="/var/log/tomcat/lens_failover"

export HOST_IPS=$(ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | sed 'N;s/\n/, /;')
export LOCAL_HOSTNAME=$(hostname)
export DAEMON_USER=$(whoami)
export LOGIN_USER=$(who am i | awk '{print $1}')

export LOGSTASHMACHINE=lsvllogst01d.liaison.dev
export LOGSTASHPORT=4560
export LOGSTASH_ROLLING_SIZE="250 MB"

export LOGSTASH_LOG_DIR="/var/log/tomcat/logstash_failover"
export LOGSTASH_RING_BUFFER_SIZE=262144
# ==================================================================
# log overrides
# ==================================================================

export JAVA_OPTS="$JAVA_OPTS -XX:-UseSplitVerifier"

# Dump heap when out of memory to /var/log/tomcat
export JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/tomcat"

# discourage address map swapping by setting Xms and Xmx to the same value
# http://confluence.atlassian.com/display/DOC/Garbage+Collector+Performance+Issues
export CATALINA_OPTS="$CATALINA_OPTS -Xms6g"
export CATALINA_OPTS="$CATALINA_OPTS -Xmx6g"

# Increase maximum perm size for web base applications to 4x the default amount
# http://wiki.apache.org/tomcat/FAQ/Memoryhttp://wiki.apache.org/tomcat/FAQ/Memory
export CATALINA_OPTS="$CATALINA_OPTS -XX:MaxPermSize=512m"

# Reset the default stack size for threads to a lower value (by 1/10th original)
# By default this can be anywhere between 512k -> 1024k depending on x32 or x64
# bit Java version.
# http://www.springsource.com/files/uploads/tomcat/tomcatx-large-scale-deployments.pdf
# http://www.oracle.com/technetwork/java/hotspotfaq-138619.html
export CATALINA_OPTS="$CATALINA_OPTS -Xss228k"

# Oracle Java as default, uses the serial garbage collector on the
# Full Tenured heap. The Young space is collected in parallel, but the
# Tenured is not. This means that at a time of load if a full collection
# event occurs, since the event is a 'stop-the-world' serial event then
# all application threads other than the garbage collector thread are
# taken off the CPU. This can have severe consequences if requests continue
# to accrue during these 'outage' periods. (specifically webservices, webapps)
# [Also enables adaptive sizing automatically]
export CATALINA_OPTS="$CATALINA_OPTS -XX:+UseParallelGC"

# This is interpreted as a hint to the garbage collector that pause times
# of <nnn> milliseconds or less are desired. The garbage collector will
# adjust the  Java heap size and other garbage collection related parameters
# in an attempt to keep garbage collection pauses shorter than <nnn> milliseconds.
# http://java.sun.com/docs/hotspot/gc5.0/ergo5.html
export CATALINA_OPTS="$CATALINA_OPTS -XX:MaxGCPauseMillis=1500"

# A hint to the virtual machine that it.s desirable that not more than:
# 1 / (1 + GCTimeRation) of the application execution time be spent in
# the garbage collector.
# http://themindstorms.wordpress.com/2009/01/21/advanced-jvm-tuning-for-low-pause/
export CATALINA_OPTS="$CATALINA_OPTS -XX:GCTimeRatio=9"

# Disable remote (distributed) garbage collection by Java clients
# and remove ability for applications to call explicit GC collection
export CATALINA_OPTS="$CATALINA_OPTS -XX:+DisableExplicitGC"

# Https.protocols parameter for JVM
export CATALINA_OPTS="$CATALINA_OPTS -Dhttps.protocols=TLSv1.2"