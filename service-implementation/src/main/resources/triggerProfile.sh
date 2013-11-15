#THIS IS THE SCRIPT FILE THAT A CRON SCHEDULER SHOULD CALL TO TRIGGER PROFILE PERIODICALLY.
#HOW TO SETUP A CRON JOB
#STEP 1: Launch crontab -e
#STEP 2: EDIT TO INCLUDE */5 * * * * /opt/apps/mailbox/triggerProfile.sh MCCWebProfile. 
#        This sample schedules the cron to trigger profile one in 5 mins.
#STEP3: Save and Quit:wq
#STEP4: crontab -l - verify your configuration listed
#STEP5: Create the folder curllogs at /opt/apps/mailbox
#STEP6: Once triggered you will find the logs at /opt/apps/mailbox/curllogs/
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
