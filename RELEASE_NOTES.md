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
