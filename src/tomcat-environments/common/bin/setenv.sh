#! /bin/sh
# ==================================================================
#  ______                           __     _____
# /_  __/___  ____ ___  _________ _/ /_   /__  /
#  / / / __ \/ __ `__ \/ ___/ __ `/ __/     / /
# / / / /_/ / / / / / / /__/ /_/ / /_      / /
#/_/  \____/_/ /_/ /_/\___/\__,_/\__/     /_/

# Multi-instance Apache Tomcat installation with a focus
# on best-practices as defined by Apache, SpringSource, and MuleSoft
# and enterprise use with large-scale deployments.

# Credits:
#       Google -> Couldn't survive without it
#       Stackoverflow.com -> Community support
#       SpringSource -> Specifically best-practices and seminars (Expert Series)

# Based On:
#       http://www.springsource.com/files/uploads/tomcat/tomcatx-performance-tuning.pdf
#       http://www.springsource.com/files/u1/PerformanceTuningApacheTomcat-Part2.pdf
#       http://www.springsource.com/files/uploads/tomcat/tomcatx-large-scale-deployments.pdf

# Created By: Terrance A. Snyder
# URL: http://www.terranceasnyder.com, http://shutupandcode.net

# Best Practice Documentation:
# http://terranceasnyder.com/2011/05/tomcat-best-practices/

# Looking for the latest version?
# github @ https://github.com/terrancesnyder

# ==================================================================

export JRE_HOME=/opt/liaison/releases/current/jre/


# Check for application specific parameters at startup
if [ -r "$CATALINA_BASE/bin/appenv.sh" ]; then
  . "$CATALINA_BASE/bin/appenv.sh"
fi

echo "Using CATALINA_OPTS:"
for arg in $CATALINA_OPTS
do
    echo ">> " $arg
done
echo ""

echo "Using JAVA_OPTS:"
for arg in $JAVA_OPTS
do
    echo ">> " $arg
done
echo "_______________________________________________"
echo ""