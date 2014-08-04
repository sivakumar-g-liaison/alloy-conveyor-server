#THIS IS THE SCRIPT FILE THAT A CRON SCHEDULER SHOULD CALL TO TRIGGER PROFILE PERIODICALLY.
#HOW TO SETUP A CRON JOB
#STEP 1: Launch crontab -e
#STEP 2: EDIT TO INCLUDE */5 * * * * /opt/apps/mailbox/triggerProfile.sh MCCWebProfile. 
#        This sample schedules the cron to trigger profile one in 5 mins.
#STEP3: Save and Quit:wq
#STEP4: crontab -l - verify your configuration listed
#STEP5: Create the folder triggerProfileSchedulerlogs at /opt/apps/mailbox
#STEP6: Once triggered you will find the logs at /opt/apps/mailbox/triggerProfileSchedulerlogs/
#!/bin/bash
#
# Copyright 2014 Liaison Technologies, Inc.
# This software is the confidential and proprietary information of
# Liaison Technologies, Inc. ("Confidential Information").  You shall
# not disclose such Confidential Information and shall use it only in
# accordance with the terms of the license agreement you entered into
# with Liaison Technologies.
#

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
curl -d "" -H "Content-Type:application/json" -o /app/mailbox/triggerProfileSchedulerlogs/logs_$DATE.txt   http://10.146.18.25:8989/g2mailboxservice/rest/v1/mailbox/triggerProfile?name=$profileName&excludeMailbox=$excludeName
exit 0
