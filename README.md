
## Introduction
g2-mailbox code base is common for three different micro services called Relay, Legacy Relay and Conveyor Server. The deployment needs to done on all the three nodes to maintain the common version.

## Prerequisites

### Service Users

The application user should be g2_env_app_svc not the regular tomcat user and the user should have permisison to access the NFS locations and execute shell scripts for machine account directory creations.

Conveyor Server would run with tomcat user and there is not need of special user

### Network Shares

NFS Mounts for Relay
* /data/http
* /data/ftp (secured one and customers won't use it due to complicance)
* /data/ftps
* /data/sftp

NFS Mounts for Legacy Relay
* /data/ftp (unsecured one and customers would use it)

LUKS Mounted Share
* /secure/tomcat

### Server Parameters

#### VM Sizing

This service requires __Large VM__

[VM Sizing Legend: DEV/QA](http://confluence.liaison.tech/display/ARCH/G2+DEV+Integration+Lab+Platform+Configuration+-+Lithia+Springs#G2DEVIntegrationLabPlatformConfiguration-LithiaSprings-VMSizingLegend "VM Sizing Legend: DEV/QA")  
[VM Sizing Legend: UAT/PROD](http://confluence.liaison.tech/display/INFRA/G2+PROD+Platform+Configuration+-+AT4+-+LithiaSprings "VM Sizing Legend: UAT/PROD")

#### Ports

Standard 8989 and 9443

#### JVM Requirements

Bouncy Castle Provider needs to be set in the position #5. This is handled in the application start up.

### Location
* Tier - Web Tier
* F5 VIP - Required
* External Public URL - Required

### Bootstrapping

[Bootstrapping guide](http://confluence.liaison.tech/display/ARCH/Step+by+Step+walkthrough+of+Bootstrap+Process+in+UAT "Bootstrapping guide")

### Database Provisioning

Provisioning and migration scripts can be found in _g2-mailbox/service-implementation/src/main/resources/db_.

Two database is required for Relay and one is for Design Time(DTDM) and another one for Runtime(RTDM).

Provisioning with python script:

```
python provision.py -u <user prefix> -m <moniker> -a <application password> -o <owner password> -s <sys password  -t <tns name>
```

Migration with flyway:
```
gradlew flywayMigrate -PdbServerName=<db server host> -PdbServerService=<service name> -PdbSchemaNamePrefix=<user prefix> -PdbSchemaNameMoniker=<moniker> -PdbaPassword=<owner password>
```

#### Special Instructions
The DB Migration is not required for Legacy Relay since it is using the same DB of the Relay.

### Service Dependencies

* GEM
* GUM
* Service Broker
* Key Management
* Database (Oracle)
* HornetQ
* MapR Streams in Active Active Environment
* Bootstrap server

### Special Requirements

* Need the shellscripts to create and delete the machine account directories
* The shell scripts should be placed under /secure/tomcat/env directory

### Miscellaneous Settings

No special requirements.

## Deployment

### Prerequisites

1. Keystore and password file for Tomcat SSL
    * Keystore: /secure/tomcat/tomcatssl.jks
    * Password file: /secure/tomcat/ssl-password.txt
2. SSH key and password for bootstrap in /secure/tomcat/.ssh
3. deployment-context.sh __must__ be in /opt/liaison/components
    * APP_ID must be callback-service
    * Other values depends on the environment
4. g2-java, g2-cacerts-<env> and g2-tomcat RPM packages installed

Example content of deployment-context.sh from UAT environment
Relay:
```
APP_ID=g2mailboxservice
ENVIRONMENT=uat
STACK=default
REGION=us
DATACENTER=at4-uat-pres
SERVER_ID=at4u-lvmbox01
TOMCAT_USER=g2_uat_app_svc
TOMCAT_GROUP=g2_uat_app_svc
```

Legacy Relay:
```
APP_ID=g2mailboxservice
ENVIRONMENT=uat
STACK=lowsecure
REGION=us
DATACENTER=at4-uat-pres
SERVER_ID=at4u-lvmbox01
TOMCAT_USER=g2_uat_app_svc
TOMCAT_GROUP=g2_uat_app_svc
```

Conveyor Server:
```
APP_ID=g2mailboxservice
ENVIRONMENT=uat
STACK=default
REGION=us
DATACENTER=at4-uat-pres
SERVER_ID=at4u-lvmbox01
TOMCAT_USER=tomcat
TOMCAT_GROUP=tomcat
```

### Service installation

1. Download RPM packages with _yumdownloader_
    * yumdownloader g2-g2mailboxservice g2-g2mailboxservice-conf
2. Install downloaded packages with _rpm_
    * sudo rpm -i <downloaded g2-g2mailboxservice package>
    * sudo rpm -i <downloaded g2-g2mailboxservice-conf package>

Example
> $ yumdownloader g2-g2mailboxservice g2-g2mailboxservice-conf
Run Healthcheck to establish baseline
  
sudo su -
  
#### Look for all thread pools that are in thread-shutdown.conf
cat /opt/liaison/components/g2-service-conf/properties/thread-shutdown.conf
 
#### Compare the pool query to the above, we only care about the pools in the conf.
#### The first section of the report should show the thread-pools as NOT SHUTDOWN.
service tomcat query
 
#### Bleed the service and make sure there are no errors
service tomcat bleed
 
#### Look for all thread pools to be reported as Terminated/Shutdown
service tomcat query
 
#### Stop the service.
service tomcat stop
  
#### check it stopped
ps aux | grep tomcat
  
#### check there's space
df -h
 
#### install, but report errors listed in the output to dev
```html
rpm -ivv --force http://mirror.liaison.prod/g2repo/release2/x86_64/g2-g2mailboxservice-5.1.9-0.372.RC372.x86_64.rpm
rpm -ivv --force http://mirror.liaison.prod/g2repo/release2/x86_64/g2-g2mailboxservice-conf-5.1.9-0.372.RC372.x86_64.rpm
```
 
#### Verify RPMs installed correctly
```
alternatives --display g2-service
alternatives --display g2-service-conf
```


_NOTE:
Using **yum install g2-g2mailboxservice g2-g2mailboxservice-conf** will install the latest packages but it will remove previously installed versions_

### Thread Shutdown Configuration
```
"g2-listener-pool-container-processedpayload" 
"g2-listener-pool-container-dropboxqueue" 
"g2-listener-pool-container-processor"
"g2-pool-async-processing" --block
"g2-pool-javascript-sandbox" --block
"g2-piped-apache-client-output-stream-pool" --block
"g2-pool-spectrum-writer" --block
"g2-pool-healthcheck-spectrum"
```

Note:
We do see the common errors during bleed operation and this very common. This means that pool is not initialized and we can ignore it.
```
g2-listener-pool-container-dropboxqueue not registered with com.liaison.threadmanagement.LiaisonExecutorServiceManager
g2-listener-pool-container-processedpayload not registered with com.liaison.threadmanagement.LiaisonExecutorServiceManager
```
Since it is common code base for three different micro service, we will not initialize Conveyor Server/Dropbox related pools in Relay and Legacy. Same thing applicable for Conveyor Server where we don't initialize javascript or processor pools.
