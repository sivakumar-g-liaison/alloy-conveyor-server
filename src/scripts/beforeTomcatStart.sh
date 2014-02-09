#!/bin/bash

# install environment specific jre configs
echo "cp -rf ${releaseCurrent}/configs/jre/common/. ${releaseCurrent}/jre"
cp -rf ${releaseCurrent}/configs/jre/common/. ${releaseCurrent}/jre

echo "cp -rf ${releaseCurrent}/configs/jre/${environment}/. ${releaseCurrent}/jre"
cp -rf ${releaseCurrent}/configs/jre/${environment}/. ${releaseCurrent}/jre

# vestigial?
# install environment specific tomcat configs
#echo "cp -rf ${releaseCurrent}/configs/tomcat/common/. ${releaseCurrent}/tomcat"
#cp -rvf ${releaseCurrent}/configs/tomcat/common/. ${releaseCurrent}/tomcat
#echo "cp -rf ${releaseCurrent}/configs/jre/${environment}/. ${releaseCurrent}/jre"
#cp -rvf ${releaseCurrent}/configs/jre/${environment}/. ${releaseCurrent}/jre

# install environment specific application configs
echo "cp -rf ${releaseCurrent}/configs/app/.  ${releaseCurrent}/tomcat/lib"
cp -rf ${releaseCurrent}/configs/app/.  ${releaseCurrent}/tomcat/lib

