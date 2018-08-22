5.3.1-SNAPSHOT
---------------
* Added option to use either Queue Service or HornetQ in DirectorySweeper and WorkTicketUtil
* Update lib_message_bus to 14.2.1
* Add default QS topic for topic messages
* GMB-1158 - Please add the loading icon or progress bar in staged file screen
* GSP-179: Update processor DC option is not displayed initially in relay

5.3.3
---------------
* GMB-1155 - Ability to stop relay processors overwritting LENS "Category

5.3.2
---------------
* FS2 lib upgrade 13.0.0 and Crypto lib upgrade 6.2.0

5.3.1
---------------
* GMB-1117 - Return list of the stuck files from sweepers - fix for the path is null

5.3.0
---------------
* GSB-3232 - Need to increase HTTP processor limit to >10MB
* GWUD-157 - Support SSO
* GMB-1130 - Write permissions when using File Writer to create new directories and write a file the first time
* GMB-919 -Implement Scripts User Interface
* GMB-1117 - Return list of the stuck files from sweepers
* GMB-1132 - Min-length validation is not working in relay processor search screen
* GUM-445 - PX1 UAT: User account folder is not replicated
* GMB-1104 - Queue Service integration + guice module

5.2.18
---------------
* Migration Service changes (ACL disabled)

5.2.17
---------------
* Kafka Properties update
* Migration Service to update originating dc and process dc in processors and staged file

5.2.16
---------------
* GMB-1129 - Mailbox - Staged Files:Process DC value was incorrect in staged Files details screen
* PX1U DB Service name updates
* GMB-1116 - Replicate remote uploader files when it is using file system - Delete replication issue fixes

5.2.15
---------------
* GMB-1116 - Replicate remote uploader files when it is using file system
    * Delete replication issue fixed
* GMB-1115 - UI to update the process dc of the processor
    * ACON integration changes on the affinity API and Staged file updates
    * Update the process dc of the staged of the Remote Uploader
* GMB-1119 - HTTP Async Clean up   
* GMB-1129 - Mailbox - Staged Files:Process DC value was incorrect in staged Files details screen
* GGT-1581 - PX1 US UAT and PROD properties update
* GMB-1119 - HTTP Async Clean up
* GMB-658 Mailbox updated with new 'tenancyKey' and invalid 'serviceInstance_pguid'.

5.2.14
---------------
* Added EU properties
* GMB-1115 - UI to update the process dc of the processor
* GMB-1116 - Replicate remote uploader files when it is using file system
* GMB-1119 - Lifetime Brands LENS Status Research
* GMB-1121 - Trigger file is not replicated on successful staging.
* GC-468 - Unable to Edit HTTP/HTTPS Processor which is created using configurator
* GUM-440 - Retry the directory creation until the user account replicated by Active Directory
* GSB-3244: setEngineStateToError exposes Message Content in LENS Logs.
* GC-158 - Mailbox:processor list/read operation returns only a small subset of fields
* GMB-1086 - Add Watchdog cleanup to lens event log
* GMB-1101 - Processors' retryInterval-property should be supported
* GGT-1581 - PX1 US UAT and PROD properties update
* GMB-658 Mailbox updated with new 'tenancyKey' and invalid 'serviceInstance_pguid'.

5.2.13
---------------
* GMB-1114 - Trigger file gets written to wherever the "last" file goes

5.2.12
---------------
* GMB-901 - File Create replication fixes

5.2.11
---------------
* GMB-1111 - File writer throwing "Out of Memory" error when running1000 transactions - More refinements

5.2.10
---------------
* GMB-901 - File delete operation refinements

5.2.9
---------------
* GMB-1111 - File writer throwing "Out of Memory" error when running1000 transactions

5.2.8
---------------
* GMB-1103 - Remote Uploader Changes for Trigger File

5.2.7
---------------
* GMB-1108 - FileWriter to have similar 'File transfer indicator' as RemoteUploader has
* GMB-1103 - Remote Uploader Changes for Trigger File

5.2.6
---------------
* GMB-1106 New Queue for Legacy Relay Replication

5.2.5
---------------
* GMB-912 - File writer to replicate file across the DC - added missed retry count
* GMB-1103 - Added index for parent gpid in Staged File table

5.2.4
---------------
* GMB-1100 - Conditional Sweeper and Trigger property
* GMB-1102 - Directory Transfer to publish Batch transaction to SB.
* GMB-1103 - Directory and Conditional Sweeper enhancements

5.2.3
---------------
* Reverted the migration script changes(6d963a9ab692c88dde919746572e509ca42f099a)
* MAPR Service Ticket location added in the start up file

5.2.2
---------------
* GMB-1097 - Null pointer exception if the sweeper directory is not available.
* MapR Stream - Kafka implementation
* GMB-1091 - Update the processingDc column of the Remote downloaders during Datacenter switch

5.2.1
---------------
* GMB-1095 - Modify the uploader logic where local directory is not mandatory
* GMB-1096 - Need to set the success status while constructing workresult.

5.2.0
---------------
* GMB-1088 - The remote uploader always has to pick up the current datacenter files based on processing_dc
* GMB-1089 - Watchdog service cleanup based on processingDC
* GMB-1090 - Process downloader based on processing DC
* GMB-901 - MAPR Intergration - REST endpoint to receive notification from system for file delete operation
* Removed newly added Indexes for STAGED_FILE and PROCESSOR_EXEC_STATE to check in STAGING

5.1.8
---------------
* new rpm with fs2 11.1.2
    * There is non backward compatible changes introduced on GSB-3170

5.1.7
---------------
* GMB-1056 - HTTP Remote Uploader Improvements
    * Execution type fix - once or infinite
    * LENS logging for the invalid url case
    * Try all the files and upload successful files

5.1.6
---------------
* GMB-1056 - HTTP Remote Uploader Improvements - Log Changes

5.1.5
---------------
* GMB-1056 - HTTP Remote Uploader Improvements
* GMB-1064, GMB-1065 - JSCH 0.1.54 causing issues for the kex alg diffie-hellman-group-exchange-sha1
* GMB-1076 - FTP Protocol type is set when selecting the cluster type is set as Low secure.
* GLASS-13 : Use GlassMessageUtil to add admin error details and also node information 
    * By using the GlassMessageUtil, we can add more logic to additional information just by modifying the library
* GMB-1075 - HTTP Async Processor: Socket Timeout Value deleted permanently from processor properties screen
* GMB-1078 - URL generated in the Mailbox processor is incomplete
* GMB-1085 - Log the response status code and data in Kibana
* GWUD-159 - Show user info of the file downloader

5.1.4
---------------
* GGT-1532-Update com.liaison.acl.skipList in the properties
* GGT-1528 Upgrade lib-dto to latest version in gateway services Relay
* GMB-1080-px1s-vpmbox: com.liaison.commons.acl.util.ACLPermissionCheckRuntimeException: HTTP 403 Forbidden

5.1.3
---------------
* GMB-1073 - Mailbox created successfully with invalid tenancy key through postman services

5.1.2
---------------
* bootstrap properties typo updated 

5.1.1
---------------
* staging at4 db service_name updated 

5.1.0
---------------
* lib configurationv6 upgrade
* commons-lib upgrade
* tomcat scripts updates
* GMB-1062 - Provision to enter the socket timeout in http sync processor - updated socket timeout validation
* GMB-1064, GMB-1065 - JSCH 0.1.54 causing issues for the kex alg diffie-hellman-group-exchange-sha1 

5.0.1
---------------
* use acl-commons-lib 8.4.0
* use commons-lib 11.4.0

5.0.0
---------------
* GMB-1036 - LENS should show one triggered file move as a single transaction
* GMB-1058 - Failed to stage files but there is no error logged
* GMB-1062 - Provision to enter the socket timeout in http sync processor
* GMB-1059 - Upgrade libraries in Relay
    * commons-lib 11.+
    * acl-commons-lib 8.+
    * lib_crypt 6.+
    * fs2 10.+
    * globalenterprisemanager-client 5.+
    * usermanagement-client 5.+
    * lib_message_bu 13.+
    * jersey 2.x
    * lib_health_monitor 6.+
* GWUD-140 - Proper error message from the dropbox/conveyor server when the passsword expire and account locked
    * Filter level changes  - Removed redundant code on the dropbox side
* GMB-948 Log the application logs to Kibana directly
* GMB-1063 - Unable to receive the response payload in http sync request
* GMB-912 - File writer replication retry if storage replication is not done.
* GMB-1011 - Processor affinity support
* GMB-1068 - Nullpointer exception while updating a processor
* Production properties update
* AT4 and PX1 Staging Properties update
* GMB-1067 - Validate the runtime processor configuration during revise operation

4.17.3
---------------
* GMB-1052 - EOF exception is received when the trigger file name is given as .INP
* GMB-1051 - Conditional sweeper sweeps files marked as inactive

4.17.2
---------------
* GMB-1050 - Page displays empty fields when loading the processor on relay

4.17.1
---------------
* [GMB-988](https://jira.liaison.tech/browse/GMB-988) - REST - Processor Creation/Updation : We able to create a processor using FTPS/SFTP/Sweeper protocol in low secure relay - protocol changes

4.17.0
---------------
* GMB-1030 - Add new processor type ConditionalSweeper - Server
* GMB-1031 - Add new processor type ConditionalSweeper - UI
* GMB-1032 - Conditional Sweeper execution
* GMB-1033 - SFTP Clients keep sub-directories intact
* GMB-1034 - Data retention based on TTL and should not delete a file after pickup
* GMB-1035 - Watchdog service has to monitor the conditional sweeper
* GMB-1036 - LENS should show one triggered file move as a single transaction.
* GMB-1037 - Provide ability to filter files eligible for move
* [GMB-1028](https://jira.liaison.tech/browse/GMB-1028) - FTPSClient needs to be updated
* [GGT-1356](https://jira.liaison.tech/browse/GGT-1356): Remove Spectrum settings from Archaius *.properties files
* [GGT-1357](https://jira.liaison.tech/browse/GGT-1357) and [GSB-452](https://jira.liaison.tech/browse/GSB-452): Inheritance and Minimization of Archaius property files. (Backwards-compatible)
* GMB-1047 - Should not post infinite glass log messages when uploader fails
* [GMB-1046](https://jira.liaison.tech/browse/GMB-1046) - Upgrade fs2 library 9.x
* [GMB-972](https://jira.liaison.tech/browse/GMB-972)  - Need "(recursive) mkdir" in a SFTP Client Wrapper
* [GMB-988](https://jira.liaison.tech/browse/GMB-988) - REST - Processor Creation/Updation : We able to create a processor using FTPS/SFTP/Sweeper protocol in low secure relay

4.16.7
---------------
* GMB-1053 - HTTP Connection Leaks
* GMB-1055 - HTTPS delivery does not report failure to Service Broker for unsuccessful HTTP response

4.16.6
------
* GMB-1053 - HTTP Connection Leaks - NO PROD DEPLOYMENT AND ONLY UAT TESTING

4.16.5
------
* GMB-1053 - HTTP Connection Leaks - NO PROD DEPLOYMENT AND ONLY UAT TESTING

4.16.4
------
* GMB-1053 - HTTP Connection Leaks - NO PROD DEPLOYMENT AND ONLY UAT TESTING

4.16.3
---------------
* GMB-1045 - Increase script sandbox pool size

4.16.2
---------------
* GMB-1044 - Relay sets processor execution id instead of processor id in stuck processor alerts

4.16.1
---------------
* GGT-1356: Remove Spectrum settings from Archaius *.properties files 
* GMB-952  - Upgrade Jsch library
* GMB-1025 - SFTP Client doesn't support newer algorithms
* GMB-1017 - Null pointer exception when fetching organization detail
* GMB-994 - Sweeper is not interrupted while sweeping empty files
* GMB-998 - Mailbox is listed multiple times in data grid while searching mailbox using Profile
* GMB-999 - Unable to update staged files
* GMB-1000 - Add cluster type in the staged files grid

4.15.3
---------------
* GMB-1022 - Property ExcludedFiles and IncludeFiles to support wild carding
* GMB-1024 - Upgrade to FS2 library version 8.+

4.15.2
---------------
* GMB-991 - Add provision to filter entities by cluster type	
* GMB-993 - Log the processor guid during script execution

4.15.1
---------------
* GMB-963 - Create lowsecure archaius properties - updates
* GMB-984 - Processors are not displayed while trying to view the processors, on navigating back from mailbox screen.
* GMB-987 - Relay sets wrong Cluster type in Runtime table when we create legacy processors
* GMB-958 - Errors are not displayed in the admin error details field.
* GMB-987 - Relay sets wrong Cluster type in Runtime table when we create legacy processors

4.15.0
---------------
* GMB-956 - Add a flag column to database tables to identify which cluster this belongs to.
* GMB-959 - Migrate the server side logic to support gateway_type flag on read and write
* GMB-960 - Analyze the impact on conveyor server as there schema is shared with relay
* GMB-961 - Expose a REST endpoint to identify the origin gateway type of a processor or mailbox
* GMB-962 - Define low-secure queues
* GMB-963 - Create lowsecure archaius properties
* GMB-965 - Add dashboard icon for low-secure gateway
* GMB-966 - Auto route message to designated relay cluster
* GMB-990 - Invalid cluster type isn't validated in REST endpoint

4.14.1
---------------
* GMB-980 - Provision to filter by node and processor guid
* GMB-970 - Rest service to update multiple processor guid status

4.14.0
---------------
* GMB-954 - Query back to service broker to determine the sender name when creating a process.
* GGT-1329 removing errant period from provisioning system calls to echo
* GMB-982 - Unable to create folders in remote location.
* GMB-977 - Named parameter make more sense than using number.
* GMB-975 - MailboxConfigurationDAOBase code review
* GMB-973 - Relay/Relay-Conveyor missing version checker for the URL
* GMB-971 - Mailbox processors are not displayed while trying to view the processors, on navigating back to mailbox screen.
* GMB-949 - Processor Execution Screen Improvements
* GMB-907 - Typeahead calls to config/mailbox/typeAhead/getEntiryByNames.Get shouldn�t throw exception

4.13.4
---------------
* GMB-979 - FTPS Remote Uploader - Software caused connection abort: socket write error

4.13.3
---------------
* GMB-978 - Override default Socket timeout in sync sweeper
* Email notification receiver updates for error and stuck processor case

4.13.2
---------------
* GMB-947 - Relay errors to LENS Metadata.
* GC-367 - Relay listing issue with configurator

4.13.1
---------------
* GMB-946 - Analyze why there are more timer threads.

4.13.0
---------------
* GGT-1236, GGT-1209 add appenv and archaius properties layers
* GMB-944 - Dropbox : Files-Staged Files : Pro
* GMB-945 - Files-Staged Files : In Show Details Pop up screen meta information doesn't fit in the Popup details
* GMB-932 - Remote Downloader should download directories based on the property
* GMB-915 - Mailbox list/read operation ignores serviceInstance defined in search criteria
* GMB-902 - Staging a file to Dropbox throws a NumberFormatException.
* GMB-943 - Sort transfer profiles in mailbox processor screen.
* GWUD-128 - Provide a default sender organization.
* GMB-934 - "callback is not a function error" is received in Relay application.
* GMB-949 - Processor Execution Screen Improvements
* GMB-942 - org.hibernate.exception.GenericJDBCException: could not inspect JDBC autocommit mode - Added sql for validation
* GMB-907 - Typeahead calls to config/mailbox/typeAhead/getEntiryByNames.Get shouldn�t throw exception 

4.12.0
---------------
* GWUD-130 - Conveyor errs, when a page that doesn't exist is requested.
* Removed Spectrum FS2 configs from Staging environment

4.11.2
---------------
* test case failure fix

4.11.1
---------------
* Rebuilding to pickup new common-lib

4.11.0
---------------
* GGT-1220 : Enable BOSS in DEV and QA

4.10.2
---------------
* Gitlab Server Update

4.10.1
---------------
* update property

4.10.0
---------------
* 2.4.1 Code review comments
* GMB-881 - Change glass sender to glass shipper in log4j properties. All environments

4.9.3
-------------------------------
* GMB-884 - Failed to execute HTTP request: at4u-vpsbasy.liaison.dev: Name or service not known

4.9.2
-------------------------------
* GMB-880 - connection leak in Mailbox search operation

4.9.1
-------------------------------
* GGT-708 - remove deprecated-* repos
* GMB-869 - We need a single-threaded, synchronous Data Sweeper process
* GMB-870 - HTTP response status code should not be 500 in case of authentication problem
* GMB-864 -	Need an ability to receive notification on successful file upload
* GMB-711 - Processor read returns wrong folder properties
* GMB-803 - Read/list mailbox returns entity with different fields
* GMB-612 - Mailbox:Application is very slow when the user tries to a create/revise processor for all protocols
* GMB-854 - Mailbox should not log Authorization headers
* GMB-866 - Do not use System.out
* GMB-860 - Audit Resource is missing user information
* GMB-862 - FTPS Uploader : 'Unsupported recod version unknown-48.48' exception occurs when the user trying to upload a File
* GMB-863 - Default values for processor static properties are not set while creating using Migrator.
* GMB-867 - Relay runtime data model updates
* GMB-875 - Implementation of synchronous Data Sweeper process

4.9.0
-------------------------------
* GMB-860 - Audit Resource is missing user information
* GGT-870 publish dependency report
* GGT-868 publish WAR
* update Gradle to 3.0

4.8.0
-------------------------------
* GGT-726 - Four corner logging to TransactionVisibilityAPI
* 2.3.2 code review comments
* GMB-844 - Provision to override the script default timeout value
* GMB-841 - Unable to download large file using processor script
* GMB-829 - ProcessorPropertyJsonMapper clean up
* GMB-827 - Pass list as parameter instead of appending to the query
* GMB-828 - Modify convertTTLIntoSeconds
* GMB-849 - The overridden TTL is not property set for HTTP Sync and Async transactions
* GMB-856 - LENS logging updates
* GMB-859 - Disable rolling logs in log4j config file in all env

4.7.1
-------------------------------
* GMB-839, GMB-851 - Operation not permitted error is received inconsistently while staging a file.

4.7.0
-------------------------------
* GMB-837   - HTTP time out too aggressive on PROD - 5 minutes
* GMB-836	- Direct Upload is not working for the processor created by Configurator
* GMB-830	- Provide an option to sort the files in sweeper
* GMB-803	- Read/list mailbox returns entity with different fields
* GMB-802	- Processor - Create : Dynamic property name is not cleared after clicking on save button
* GMB-799	- FirstCorner Timestamp
* GMB-797	- Try to useCriteriaQuery for building querie
* GMB-666	- random exception in MailBox PROD: "Key manager failed to retrieve stored secret"
* GMB-665	- No content to map to Object due to end of input
* GMB-656	- Maintenance
* GMB-649	- DropboxService cleanup on exception handling
* GMB-517	- Setup a workflow for dropbox test in dev and qa environment
* GMB-494	- ConstructMessage Method refinements
* GMB-489	- Renew Session: Renew session in Mail box is not redirecting to SOA Proxy login page.
* GMB-465	- Users cannot reach the Mail Box UI, possibly due to PermGen exhausted.
* GMB-449	- Processor creation : URL is not updated when the user modify the url using ACE Editor while creating an new processor
* GMB-288	- Please augment the documentation concerning scripts used by mailbox processors.
* GMB-211	- Mailbox should be configured with Truststore Group Name, not GUID
* GMB-129	- Warning dialog should be displayed if the user tries to do any new operation without saving the previous operation

4.6.1
-------------------------------
* GMB-805: sweepSubDirectories property is added as dynamic property.

4.6.0
-------------------------------
* GMB-802: Processor - Create : Dynamic property name is not cleared after clicking on save button

4.5.2
-------------------------------
* Rebuild to get new commons-lib to fix GSB-2877, "Unable to run large pgp decryption in uat"

4.5.1
-------------------------------
* GMB-821 Sync Mailbox Transaction Fails

4.5.0
-------------------------------
* GGT-797 BOSS Properties removed and defaulted to SPECTRUM
* GGT-797 Updated UAT TTL value to 90 days 
* GGT-652 update gitlab hostname and protocol
* GMB-817 Provision to add the lens content type for the files swept by Directory Sweeper

4.4.0
-------------------------------
* GMB-788: Mailbox - Processor :'Error while saving the processor' error message occurs when the user trying to add the two storage types as BOSS and SPECTRUM
* GMB-779: use techDescription for more specific error messages
* GGT-745: Sentinel: Mailbox
* GMB-800: Modify the search response when results not found

4.3.4
-------------------------------
* force commons-lib to version without metrics/stats

4.3.3
-------------------------------
* removed boss storage identifiers for prod properties.

4.3.2
-------------------------------
* GMB-794 - Bleeding reports errors

4.3.1
-------------------------------
* GMB-777 Storage Type

4.3.0
-------------------------------
GMB-768 - Able to save processors even without providing password for FTP and SFTP protocols.
GMB-776 - Config Status is displayed as "Incomplete Configuration" for all mailbox.

4.2.0
-------------------------------
* GMB-723 - Declare constants as final
* GGT-453 publishing fixup
* GGT-477 restore building offline
* GMB-752 - While creating processor through REST call, the password guid from KMS must be provided in the credential section.
* GMB-753 - While creating/revising processor through REST, valid ssh keypair and trustore guid must be provided
* GMB-721 - ProfileConfigurationDAOBase clean up
* GMB-722 - Use logger.debug in ProfileConfiguratinDAOBase
* GMB-751 - Display actual TenancyKey from DB in UI, if the manifest header does not contain the tenancy key

4.1.0
-------------------------------
* GQ-131 - properties for jnp
* GQ-118 - LMB Listener 
* GMB-707 - Mailbox filewriter doesn't record outbound bytes
* GGT-194 - sonarqube integration
* GMB-708 - Wrong Status code returns in response when the user configured the bind users to the Mailbox ID's
* GMB-718 - Set upper limit for grouping the files in Directory Sweeper
* GMB-634 - Mailbox Uploaders should check the local payload and try to connect the SFTP server once the files are available to upload
* GMB-646 - Validation is missing for processor properties in server side
* GGT-452 new nexus repo
* GMB-700 - Provision in the sendEmail API to overwrite the configured email address
* ARP-380 - Uses LMB connection factory

4.0.5
-------------------------------
* Need to get lib message bus to 5.4.0

4.0.4
-------------------------------
* GMB-713 Mailbox and dropbox server rollback scripts need to be .rollback

4.0.3
-------------------------------
* GGEM-124 Patch/Acl Signer Public Key Group Guid Update
* DB Rollback script

4.0.2
-------------------------------
* GMB-710 - DROPBOX- Unable to stage a file

4.0.1
-------------------------------
* GMB-706 - Extend QueueProcessor to allow ProcessorAvailability interrupt and Cut into mailbox

4.0.0
-------------------------------

3.1.0
-------------------------------

3.1.0-SNAPSHOT
-------------------------------
* GGT-404 - Modify the log4j appenders to Async
* GMB-703 - Cannot send email. java.lang.NullPointerException in UAT

3.0.7
-------------------------------
* GGT-380

3.0.6
-------------------------------
* GCL-102

3.0.5
-------------------------------
* GMB-699

3.0.4
-------------------------------
* GGT-393

3.0.3
-------------------------------
* GMB-686 - Modified UUIDGen method to static
* GMB-673 - Invalid TVAPI logged to Spectrum

3.0.3
-------------------------------
* GMB-679 - Add indexes for queries with more than one unindexed column

3.0.2
-------------------------------
* Glass Log Shipper and FileBeat configuration changes

3.0.1
-------------------------------
* removed memory/gc settings

3.0.0
-------------------------------
* Set version number for release candidate for G2 1.3.1.

2.5.2
-------------------------------
* GGEM-124 - refinements in dropbox API

2.5.1-SNAPSHOT
-------------------------------
* GGT-330 - INFO loggers - Ghazni

2.5.0
-------------------------------
* After merging with RC branch

2.4.9-SNAPSHOT
-------------------------------
* After merging with RC branch

2.4.6-SNAPSHOT
-------------------------------
* Integrated JMS lib message bus
* GMB-578	- Logging Improvement in MailboxSLA Service
* GMB-632	- Rename the menu from "Executing Processors" to "Running Processors" and Add 'processor name and Processor Id' filter in Search Processor screen.
* GMB-657 	- Mailbox GET service should not expect content-type header

2.4.5-SNAPSHOT
-------------------------------
* GMB-617   - Properties are not saved during mailbox creation
* GCL-57    - Add a provision in the G2SFTP client to use private key string which is retrieved from KMS instead of filepath
* GMB-598   - Mailbox name should be case insensitive and disable filter button should show up while loading mailboxes
* GMB-616   - Mailbox Swagger is not working
* Mailbox refinements for Email Util and JSON marshaling done using JAXBUtility
* GMB-581 - updated lib_message_bus to 4.+
* GMB-620 - Rename watchdog request in the Log to consumed from queue

2.4.9
-------------------------------
* updated queue processor interval from 10 seconds to 1 seconds

2.4.8
-------------------------------
* disable queue ssl

2.4.7
-------------------------------
* Updated queue host names for uat and prod for discovery

2.4.6
-------------------------------
* Fix list of queue servers for discovery.

2.4.5
-------------------------------
* Integrated JMS lib message bus

2.4.4
-------------------------------
* GMB-651 - add graceful shutdown pool bleed configuration
* Modified commons-scripting to use 3.3.3 - scripting pool renamed

2.4.3
-------------------------------
* Removed debug settings from UAT environment for mailbox and dropbox
* Modified commons-scripting to use 3.3.2
* GGT-254 - Invalid HeapDumpPath causing servers to fail to create dump when OOM
* Removed old thread counts from all environment properties file

2.4.2
-------------------------------
* Modified storage utilities initialization because thread model changes

2.4.1
-------------------------------
* ACL Skip list changes
* Updated poller and consumers to use QueueProcessor from lib_message_bus. 

2.4.0
-------------------------------
* GMB-641 - Updated executor service to use LiaisonExecutorServiceBuilder to create thread pool.

2.3.6
-------------------------------
* GMB-633 - Modify the filewriter to support "targetDirectory" from the SB task script

2.3.5
-------------------------------
* GQ-15 - patch for queue reconnect

2.3.4
-------------------------------
* GMB-584 - Set unique guid instead of globalprocessor guid in FSM_STATE of the mailbox filewriter

2.3.3
-------------------------------
* Modified script cache to 5 seconds in uat

2.3.2
-------------------------------
* GMB-569 - Removed Dynamic Key usage from Encryption Util
* GMB-569 - Removed the special char from comment

2.3.1
-------------------------------
* GMB-573 - Payload outbound size
* GMB-572 - Processor-Sweeper: Files are not renamed to the format specified in 'File Rename Format' property.
* GMB-574 - HTTP Sync transaction status always in 'DELIVERY IN PROGRESS'
* GMB-574 - outbound size cast
* seperate glass messages for ingress and egress
* GMB-574 - new TVAPI for the specific case
* GMB-574 - handle the empty payload response
* acl_signer_public_key_guid updates
* updated dbx acl_signer_public_key_guid

2.3.0
-------------------------------
GWUD-69: Fix Dropbox TVAPI & Glass Message logging

2.2.13
-------------------------------
update glass_log4j2_lib version to 2.X - GGT-111

2.2.12
-------------------------------
GMB-483-TransactionVisibilityAPI logging is invalid
GMB-483-TransactionVisibilityAPI logging in the async receving end
GMB-483-TransactionVisibilityAPI changes
GMB-483-TransactionVisibilityAPI set status to success
GMB-483: http async common error msg and removed unused imports
GMB-483 : set outsize in the lens logging
GMB-483 : set out agent

2.2.11
-------------------------------
turn off async logger
made logj2 production xml files the same for dbx and vanilla
removed async appender for glass and root async logger for uat and uatdbx

2.2.10
-------------------------------
added remote debug for uat, qa, dev

2.2.9
-------------------------------
making glass message appender sync

2.2.8
-------------------------------
* bumped up commons script
* TTL updates

2.2.7
-------------------------------
prod properties updates

2.2.6
-------------------------------
lens property changes

2.2.5
-------------------------------
properties updates

2.2.4
-------------------------------
fix for GMB-473

2.2.3
-------------------------------
Patch/2.1.X/Lib Glass Message

2.2.2
-------------------------------
Remove version number accidentally brought in from the "develop" branch.

2.2.1
-------------------------------
Updated the web.xml to apply the tomcat harden changes.

2.2.0
-------------------------------

2.1.9
-------------------------------
* GMB-485 : upgrade commons scripting library

2.1.8
-------------------------------
* Updated the glass_log4j2_lib version to 2.2.+
* Updated UserMgmt Client to release version

2.1.7
-------------------------------
* GMB-376 - Creating an HTTPS processor with self signed key fails
* GMB-420 - Search query optimization
* GMB-453 - Throw RuntimeException with NotImplemented message for not implemented methods
* GMB-445 - Logger and exception message are saying something different
* GMB-452 - Show proper error message when invalid target/payload location is given
* GMB-450 - Unable to create Mailbox processor script url with directory
* GMB-447 - Code review comments changes (I would match the name to the value here if possible.)
* GMB-383 - Do not use cache for Processor Javascript
* GMB-375 - Creating a SFTP processor fails when adding keys
* GMB-438 - Clear text password stored in the processor credentials table in Database
* GMB-441 - Cannot create SFTP directory sweeper: protocol required
* GMB-442 - If Processor revision/creation got failed then protocol is getting disappeared in Processor screen in Mailbox.
* GMB-439 - reverted back as clearing of password on failure from KMS is the expected behavior
* GMB-436 - Processor execution through JS is not working
* GMB-440 - NPE occurs during file staging if the given mailbox corresponding to given mailbox Id in workTicket from serviceBroker is inactive
* HTTP SYNC STATUS LOGGIN DURING SB ERROR CASE - LOG FAILURE STATUS INSTEAD OF SUCCESS RAW PASSWORD GETTING STORED IN CREDENTIALS TABLE
* Fixed the console error when try search mail Box
* Patch/Syslog Production

2.1.6
-------------------------------
* Add missing properties - MSOPS-2325

2.1.5
-------------------------------
* GMB-436
* ACL enabled in UAT
* DB Property Changes

2.1.4
-------------------------------

2.1.3
-------------------------------

2.1.2
-------------------------------
* Added https.protocols parameter to JVM
* Turn on ACL for qa-stage.
* GMB-424(Dropbox: List staged files failure when sortDirection missing)
* dropbox queue configuration
* Legacy processors cannot be revised in latest implementation - Fixed
* Patch/Turn On Acl For Qa Stage
* Fix for backward compatibility in certain processor edits.
* GMB-427 - remive loading props from during revise
* code refinements
* fixed internal issues
* GMB-429 : HTTPsync Processor:HTTPsync Processor is not working as expected using the 'HTTPListener Auth check required' property as true
* GMB-425:HTTP Sync Processor:HTTP Syn URL is not integrated with the latest URL Changes
* GMB-425:HTTP Sync Processor:HTTP Syn URL is not integrated with the latest URL Changes (uat url changed)
* GMB-428:HTTPsync Processor:Response generated with duplicate global process id when the user manually configured the globalprocessid in header
* GMB-430 (Dropbox :Staged file is not deleted based upon the TTL value configured in Mailbox)
* GMB-431 Processor - Revise :List of processors are failing in revision mode
* GMB-432:FTPS Downloader :By Default the passive value should be true while creating an * FTPS Downloader
* production url updates
* enable hornetq ssl in dev-int
* GMB-426 Processor creation:Inconsistently we are facing the 'Key manager failed to add stored secret' error message when the user create an SFTP Downloader/uploader processor
* GMB-430 Fix to set the TTL as seconds in fs2header
* passive value of static properties always display in grid without adding explicitly was fixed

2.1.1
-------------------------------

2.0.1
-------------------------------
MFT Version
=======
1.1.9
-------------------------------
GMB-408:The trigger profile endpoint in Mailbox should consume any content type
PROD syslog property updates

1.1.8
-------------------------------
Fix for High priority issue - https://jira.liaison.tech/browse/GMB-407

1.1.7
-------------------------------
GMB-406 Archaius properties for Spectrum for UAT specify a wrong location, "prod".

1.1.6
-------------------------------
Fix for GMB-405 - UAT logstash property updates

1.1.5
-------------------------------
Spectrum and Gitlab related UAT property updates.

1.1.4
-------------------------------
Updates to spectrum related proprties
glass property updates
LENS/Spectrum prod property updates

1.1.3
-------------------------------
Mailbox bug fixes and properties update

1.1.2
-------------------------------
prod property updates

1.1.1
-------------------------------
hornetq properties changes

1.1.0
-------------------------------
RELEASE VERSION


1.0.0
-------------------------------

Initial version

Add to the top of this file. Don't edit existing notes.
