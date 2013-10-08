#!/bin/bash
profileName=$1"x"
DATE=$(date +"%Y-%m-%d-%H%M%s")
#if [ $profileName == "x" ];
#then
#echo "It is mandatory to give profile name"
#exit 0
#fi
profileName=$1
excludeName=$2
echo "Input profile name is:"$profileName
echo "Exclusion List:"$excludeName
curl -d "" -H "Content-Type:application/json" -o /opt/apps/mailbox/curllogs/logs_$DATE.txt   http://10.0.24.76:8080/g2mailboxservice/rest/mailbox/triggerProfile?name=$profileName&excludeMailbox=$excludeName
exit 0
