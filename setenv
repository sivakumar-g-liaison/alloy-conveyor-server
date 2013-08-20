#!/bin/bash

# 
# CACHE ORIGINAL PATH
#
export _ORIGINAL_PATH=${_ORIGINAL_PATH:=$PATH}

# 
# DEFAULT SETTINGS
#
export _PLATFORM=linux
export _ROOT=$PWD
export _TARGET=$_ROOT/install
export DEPLOY_TARGET=$_ROOT/install
export INSTALL_HOME=$_ROOT/install
export SCRIPTS_HOME=$_ROOT/tools/scripts
export DISTROS_HOME=$INSTALL_HOME/3rdParty
if cat /proc/version | grep -q "Ubuntu\|Debian"; then
    export IS_DEBIAN=true 
else
    export IS_DEBIAN=false 
fi


#
# COMMAND LINE ARGUMENTS
#
QUIET=
if [ "$1" = "-quiet" -o "$1" = "-q" ]; then
    export QUIET=true
fi


#
# PROPERTIES SETTINGS
#
cat setenv.properties | awk -f readproperties.awk > ./temp.sh
source ./temp.sh
rm ./temp.sh

# 
# TOOLS
#
# Only CentOS/RedHat and Debian/Ubuntu are supported for automatic tool install.
# (Assume CentOS/RedHat if not Debian/Ubuntu)
#
# make sure curl is installed
command -v curl >/dev/null 2>&1 || { 
    [ "$UID" -eq 0 ] || sudo bash "$0" "$@";
    if [ "$IS_DEBIAN" = "true" ]; then
        sudo -y apt-get install curl; 
    else
        sudo yum install curl --assumeyes; 
    fi
} 
# make sure unzip is installed
command -v unzip >/dev/null 2>&1 || { 
    [ "$UID" -eq 0 ] || sudo bash "$0" "$@";
    if [ "$IS_DEBIAN" = "true" ]; then
        sudo -y apt-get install unzip; 
    else
        sudo yum install unzip --assumeyes; 
    fi
} 

# TODO - enable when gradle supports UML javadocs
# make sure graphviz dot is installed
#command -V dot >/dev/null 2>&1 || { 
#    [ "$UID" -eq 0 ] || sudo bash "$0" "$@";
#    if [ "$IS_DEBIAN" = "true" ]; then
#        sudo -y apt-get install graphviz; 
#    else
#        sudo yum install graphviz --assumeyes; 
#    fi
#} 


# 
# SETUP JDK
#
export JDK_HOME=$PWD/tools/jdk-$JAVA_FULL_VERSION
export URL_JDK=$ARTIFACT_REPO_URL/releases/com/oracle/java/liaison/jdk-$_PLATFORM-x64/$JAVA_FULL_VERSION/jdk-$_PLATFORM-x64-$JAVA_FULL_VERSION.zip
if [ ! -f $JDK_HOME/bin/java ]; then
    echo '.................................................................................'
    echo ' Downloading '$URL_JDK
    curl -o "$_ROOT/tools/jdk-$_PLATFORM-x64-$JAVA_FULL_VERSION.zip" $URL_JDK
    unzip "$_ROOT/tools/jdk-$_PLATFORM-x64-$JAVA_FULL_VERSION.zip" -d "$_ROOT/tools"
    mv "$_ROOT/tools/jdk-$JAVA_FULL_VERSION-$_PLATFORM-x64" "$_ROOT/tools/jdk-$JAVA_FULL_VERSION"
    rm "$_ROOT/tools/jdk-$_PLATFORM-x64-$JAVA_FULL_VERSION.zip"
fi

export JAVA_HOME=$JDK_HOME

chmod -R +x $JAVA_HOME/bin/
chmod -R +x $JAVA_HOME/jre/bin/

# build with JDK runtime, not JRE runtime
export BUILD_PATH=$JAVA_HOME/bin

# set the build path
export PATH=$BUILD_PATH:$_ORIGINAL_PATH

if [ ! -f $JDK_HOME/bin/java ]; then
    echo 'WARNING!'
    echo '"gradle.properties" was generated from "gradle.properties.template"'
    echo 'Please modify the new properties file to match your environment!'
    cp "./gradle.properties.template" "./gradle.properties"
fi


if [ "$QUIET" != "true" ]; then
    echo '................................................................................'
    echo $PATH
    echo '................................................................................'
    java -version
    echo '................................................................................'
fi

