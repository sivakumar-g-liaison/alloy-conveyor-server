/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox;

import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;

/**
 * Contains the static variables which we can use globally.
 *
 * @author OFS
 */
public interface MailBoxConstants {

    int RETRY_ATTEMPTS_MIN_VALUE = 0;
    int RETRY_ATTEMPTS_MAX_VALUE = 5;

    int NUMBER_OF_FILES_THRESHOLD_MIN_VALUE = 0;
    int NUMBER_OF_FILES_THRESHOLD_MAX_VALUE = 25;

    int TOTAL_PERCENT = 100;

    // Key descriminator values
    /** File datetime format. */
    String FILETIME_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    /** MailBox Sweeper DateTime Format. */
    String MAILBOXSWEEPER_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    /** Resolved folder or hidden folder name. */
    String SWEEPED_FOLDER_NAME = "sweeped";

    /** The processed file extension */
    String SWEEPED_FILE_EXTN = ".queued";

    /** Property which is used to get the configured receivers from mailbox. */
    String MBX_RCVR_PROPERTY = "emailnotificationids";

    String MBX_STATUS = "EntityStatus";

    String PROCESSOR_TYPE = "ProcessorType";

    String PROCESSOR_PROTOCOL = "Protocol";

    String FOLDER_TYPE = "FolderType";

    String CREDENTIAL_TYPE = "CredentialType";

    String INCOMPLETE_CONFIGURATION = "INCOMPLETE_CONFIGURATION";

    String COMPLETE_STATUS = "COMPLETED";

    String GROUPING_JS_PROP_NAME = "filegroupingjspath";

    String LAST_MODIFIED_TOLERANCE = "last.modified.tolerance";

    String FOLDER_HEADER = "folderlocation";
    String FILE_NAME_HEADER = "filename";
    String COMMENT_HEADER = "comment";
    String FROM_HEADER = "from";

    String URL = "resturl";

    // Added for list files in get payload
    String META_FILE_NAME = ".meta";
    String PROCESSED_FOLDER = "PROCESSED";

    // used for write response for http remote downloader. This is the default
    // processor name.
    String PROCESSOR = "PROCESSOR";

    String MAILBOX_PROCESSOR = "Processor";

    // Added for processing Mount Location from Folder Path given by User
    String MOUNT_LOCATION = "MOUNT_POINT";
    String MOUNT_LOCATION_PATTERN = "(?i:MOUNT_POINT)";

    // Added for Error File Location
    String ERROR_FOLDER = "ERROR";

    // Type of Credentials
    String TRUSTSTORE_CERT = "truststore_cert";
    String SSH_KEYPAIR = "ssh_keypair";
    String LOGIN_CREDENTIAL = "login_credential";

    // Type of Truststore
    String GLOBAL = "GLOBAL";
    String SELFSIGNED = "SELFSIGNED";

    // Added to acl Manifest Request construction
    String DOMAIN_NAME = "SERVICE_BROKER";
    String DOMAIN_TYPE = "ORGANIZATION";
    String PLATFORM_NAME = "SERVICE_BROKER";
    String SERVICE_NAME = "KEYMANAGER";
    String ROLE_NAME = "MailboxAdmin";

    String MAILBOX_ID = "mailboxId";
    String GLOBAL_PROCESS_ID = "globalProcessId";

    // properties used to configure sla rules of an mailbox
    String TIME_TO_PICK_UP_FILE_POSTED_TO_MAILBOX = "timetopickupfilepostedtomailbox";
    String TIME_TO_PICK_UP_FILE_POSTED_BY_MAILBOX = "timetopickupfilepostedbymailbox";
    String EMAIL_NOTIFICATION_FOR_SLA_VIOLATION = "emailnotificationforslaviolation";
    String MAX_NUM_OF_NOTIFICATION_FOR_SLA_VIOLATION = "maxnumberofnotificationforslaviolation";
    String LENS_NOTIFICATION_FOR_UPLOADER_FAILURE = "maxnumberoflensnotificationforuploaderfailure";

    // sla validation
    String PROCESSOR_NOT_AVAILABLE = "-NA-";
    String PROFILE_NOT_AVAILABLE = "-NA-";
    String DUMMY_MAILBOX_ID_FOR_FSM_STATE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ789101";
    String DUMMY_PROCESSOR_ID_FOR_FSM_STATE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456";

    // spectrum payload prefix
    String SPECTRUM_PAYLOAD_PREFIX = "/mailboxsweeper/payload/1.0/";

    // FS2 Header Keys
    String KEY_PIPELINE_ID = "PIPELINE_ID";
    String KEY_GLOBAL_PROCESS_ID = "GLOBAL_PROCESS_ID";
    String KEY_RAW_PAYLOAD_SIZE = "RAW_PAYLOAD_SIZE";
    String KEY_SERVICE_INSTANCE_ID = "SERVICE_INSTANCE_ID";
    String KEY_PAYLOAD_DESCRIPTION = "PAYLOAD_DESCRIPTION";
    String KEY_TENANCY_KEY = "TENANCY_KEY";
    String KEY_LENS_VISIBILITY = "LENS_VISIBILITY";
    String KEY_MESSAGE_NAME = "MESSAGE_NAME";

    String PIPELINE_FULLY_QUALIFIED_PACKAGE = "com.liaison.service.core.edm.model.Pipeline";
    String PAYLOAD_DESCRIPTION_VALUE = "Sweeped Payload from payload location %s";

    // additional context keys Worker ticket
    String KEY_FOLDER_NAME = "folderName";
    String KEY_FILE_NAME = "fileName";
    String KEY_FILE_CREATED_NAME = "createdTime";
    String KEY_FILE_MODIFIED_NAME = "modifiedTime";
    String KEY_MAILBOX_ID = "mailboxId";
    String KEY_PROCESSOR_ID = "processorId";
    String KEY_OVERWRITE = "overwrite";
    String KEY_FILE_PATH = "path";
    String DIRECT_UPLOAD = "directUpload";
    String USE_FILE_SYSTEM = "useFileSystem";
    String KEY_WORKTICKET_PROCESSOR_ID = "processorId";
    String KEY_WORKTICKET_TENANCYKEY = "tenancyKey";
    String KEY_TARGET_DIRECTORY = "targetDirectory";
    String KEY_TARGET_DIRECTORY_MODE = "targetDirectoryMode";
    String KEY_MAILBOX_NAME = "mailboxName";
    String STORAGE_IDENTIFIER_TYPE = "Storage Type";
    String CONNECTION_TIMEOUT = "connectionTimeout";
    String SOCKET_TIMEOUT = "socketTimeout";
    String PROCSR_STATUS = "procsrStatus";
    String MAILBOX_STATUS = "mbxStatus";
    String KEY_FILE_GROUP = "fileGroup";
    String KEY_FILE_COUNT = "fileCount";
    String KEY_TRIGGER_FILE_NAME = "triggerFileName";
    String KEY_TRIGGER_FILE_PARENT_GPID = "triggerFileParentGpid";
    String KEY_TRIGGER_FILE_URI = "triggerFileUri";
    String KEY_IS_BATCH_TRANSACTION = "isBatchTransaction";
    String SCRIPT = "Script";

    String TARGET_DIRECTORY_MODE_OVERWRITE = "overwrite";

    // Overwrite
    String OVERWRITE_TRUE = "true";
    String OVERWRITE_FALSE = "false";
    String OVERWRITE_ERROR = "error";

    // GITURI
    String PROPERTY_GITLAB_ACTIVITY_PROJECT_ID = "com.liaison.gitlab.mailbox.script.project.id";
    String PROPERTY_GITLAB_ACTIVITY_PRIVATE_TOKEN = "com.liaison.gitlab.mailbox.script.private_token";
    String PROPERTY_GITLAB_ACTIVITY_SERVER_HOST = "com.liaison.gitlab.mailbox.script.server.host";
    String PROPERTY_GITLAB_ACTIVITY_SERVER_FOLDER = "com.liaison.gitlab.mailbox.script.folder";

    // Empty script file
    String DEFAULT_SCRIPT_TMPLATE_CONTENT = "// Scripts File is empty.";

    // DEFAULTVALUE_FOR_REQUIRED_PROPS

    String DEFAULT_SCRIPT_TEMPLATE_NAME = "mailbox.script.default.template";
    String DEFAULT_JOB_SEARCH_PERIOD_IN_HOURS = "default.job.search.period.in.hours";
    String DEFAULT_INTERRUPT_SIGNAL_FREQUENCY_IN_SEC = "check.for.interrupt.signal.frequency.in.sec";

    String PROCESSOR_SECURE_SYNC_URL_DISPLAY_PREFIX = "processor.sync.url.display.prefix.default";
    String PROCESSOR_SECURE_ASYNC_URL_DISPLAY_PREFIX = "processor.async.url.display.prefix.default";
    String PROCESSOR_LOWSECURE_SYNC_URL_DISPLAY_PREFIX = "processor.sync.url.display.prefix.lowsecure";
    String PROCESSOR_LOWSECURE_ASYNC_URL_DISPLAY_PREFIX = "processor.async.url.display.prefix.lowsecure";

    // Property to decide whether the application is deployed "RELAY",
    // "LOW_SECURE_RELAY" or "CONVEYOR"
    String DEPLOYMENT_TYPE = "com.liaison.deployment.type";

    String MAILBOX = "MailBox";
    String PROCESSOR_STATUS = "Processor Status";
    String INTERRUPT_SIGNAL = "Interrupt Signal";
    String EXECUTING_PROCESSORS = "Running Processors";
    String PROFILE = "Profile";

    // added for generic error message if any location is missing
    String PAYLOAD_LOCATION = "payload";
    String RESPONSE_LOCATION = "response";
    String FILEWRITE_LOCATION = "file write";
    String COMMON_LOCATION = "payload or file write";

    // added for Dropbox file transfer
    String UM_AUTH_TOKEN = "um-auth-token";
    String LOGIN_ID = "loginId";
    String ACL_MANIFEST_HEADER = "acl-manifest";
    String DBX_WORK_TICKET_PROFILE_NAME = "profileName";
    String UPLOAD_FILE_NAME = "filename";
    String UPLOAD_META = "meta-data";

    String DROPBOX_AUTH_TOKEN = "token";
    String DROPBOX_LOGIN_ID = "loginId";
    String ACL_SIGNED_MANIFEST_HEADER = "acl-signature";
    String ACL_MANIFEST_FAILURE_MESSAGE = "Get manifest failed.";

    int AUTH_SUCCESS_CODE = 200;
    int AUTH_FAILURE_CODE = 401;
    int ACL_RETRIVAL_FAILURE_CODE = 400;
    int CLOSING_DATA_CONNECTION = 226;
    int FTP_FILE_TRANSFER_ACTION_OK = 250;
    int SFTP_FILE_TRANSFER_ACTION_OK = 0;
    String TTL = "ttl";
    String TTL_UNIT = "ttlunit";
    String TTL_IN_SECONDS = "ttlinseconds";
    String DROPBOX_PAYLOAD_TTL_DAYS = "com.liaison.dropbox.payload.ttl.days";
    String MAILBOX_PAYLOAD_TTL_DAYS = "com.liaison.mailbox.payload.ttl.days";
    String MAILBOX_PAYLOAD_TTL_DAYS_DEFAULT = "7";
    String FS2_STORAGE_DEFAULT_USE = "fs2.storage.file.default.use";
    String VALUE_FOR_DEFAULT_TTL = "30";
    String TTL_UNIT_SECONDS = "Seconds";
    String TTL_UNIT_MINUTES = "Minutes";
    String CUSTOM_TTL_UNIT = "CustomTTL";
    String TTL_NUMBER = "TTLnumber";
    String TTL_UNIT_HOURS = "Hours";
    String TTL_UNIT_DAYS = "Days";
    String TTL_UNIT_WEEKS = "Weeks";
    String TTL_UNIT_MONTHS = "Months";
    String TTL_UNIT_YEARS = "Years";
    String FS2_OPTIONS_TTL = "fs2.options.ttl";

    // charsetname
    String CHARSETNAME = "UTF-8";
    String TOKEN_SEPARATOR = "::";

    String DUMMY_MANIFEST_USAGE_PROPERTY = "use.dummy.manifest.as.backup";
    String DUMMY_MANIFEST_PROPERTY = "dummy.acl.manifest.json";
    String REQUEST_HEADER = "Request Header";
    String PROPERTIES_FILE = "Properties file";

    // added for self signed truststore
    String SELF_SIGNED_TRUSTORE_PASSPHRASE = "mailbox.self.signed.trustore.passphrase";

    String DEFAULT_FIRST_CORNER_NAME = "SECOND CORNER";
    String PROPERTY_FIRST_CORNER_NAME = "com.liaison.secondcorner.name";
    String DEFAULT_SECOND_CORNER_NAME = "SECOND CORNER";
    String PROPERTY_SECOND_CORNER_NAME = "com.liaison.secondcorner.name";
    String DEFAULT_THIRD_CORNER_NAME = "THIRD CORNER";
    String PROPERTY_THIRD_CORNER_NAME = "com.liaison.thirdcorner.name";
    String DEFAULT_FOURTH_CORNER_NAME = "FOURTH CORNER";
    String PROPERTY_FOURTH_CORNER_NAME = "com.liaison.fourthcorner.name";

    // static properties
    String PROPERTY_URL = "url";
    String PROPERTY_PORT = "port";
    String PROPERTY_HTTP_VERSION = "httpVersion";
    String PROPERTY_HTTP_VERB = "httpVerb";
    String PROPERTY_CONTENT_TYPE = "contentType";
    String PROPERTY_CHUNKED_ENCODING = "chunkedEncoding";
    String PROPERTY_CONNECTION_TIMEOUT = "connectionTimeout";
    String PROPERTY_SOCKET_TIMEOUT = "socketTimeout";
    String PROPERTY_RETRY_ATTEMPTS = "retryAttempts";
    String PROPERTY_OTHER_REQUEST_HEADERS = "otherRequestHeader";
    String PROPERTY_PASSIVE = "passive";
    String PROPERTY_BINARY = "binary";
    String PROPERTY_ERROR_FILE_LOCATION = "errorFileLocation";
    String PROPERTY_PROCESSED_FILE_LOCATION = "processedFileLocation";
    String PROPERTY_PIPELINEID = "pipelineID";
    String PROPERTY_DELETE_FILE_AFTER_SWEEP = "deleteFileAfterSweep";
    String PROPERTY_FILE_RENAME_FORMAT = "fileRenameFormat";
    String PROPERTY_NO_OF_FILES_THRESHOLD = "numOfFilesThreshold";
    String PROPERTY_PAYLOAD_SIZE_THRESHOLD = "payloadSizeThreshold";
    String PROPERTY_SWEEPED_FILE_LOCATION = "sweepedFileLocation";
    String PROPERTY_HTTPLISTENER_AUTH_CHECK = "httpListenerAuthCheckRequired";
    String PROPERTY_HTTPLISTENER_PIPELINEID = "httpListenerPipeLineId";
    String PROPERTY_HTTPLISTENER_PAYLOAD_LOCATION = "httpListenerPayload";
    String PROPERTY_HTTPLISTENER_SECUREDPAYLOAD = "securedPayload";
    String PROPERTY_LENS_VISIBILITY = "lensVisibility";
    String PROPERTY_TENANCY_KEY = "MBX_TENANCY_KEY";
    String PROPERTY_SCRIPT_EXECUTION_TIMEOUT = "scriptExecutionTimeout";
    String PROPERTY_STALE_FILE_TTL = "staleFileTTL";
    String PROPERTY_PROCESS_MODE = "processMode";
    String PROPERTY_RETRY_INTERVAL = "retryInterval";

    // Properties for sweeper grouping boundary condition
    String PAYLOAD_SIZE_THRESHOLD = "payloadsizethreshold";
    String NUMBER_OF_FILES_THRESHOLD = "numoffilesthreshold";
    String FILE_RENAME_FORMAT_PROP_NAME = "filerenameformat";
    String SWEEPED_FILE_LOCATION = "sweepedfilelocation";
    String PROCESSED_FILE_LOCATION = "processedfilelocation";
    String ERROR_FILE_LOCATION = "errorfilelocation";
    String HTTPLISTENER_AUTH_CHECK = "httplistenerauthcheckrequired";

    String ADD_NEW_PROPERTY = "add new -->";

    String PAGING_OFFSET = "pagingoffset";
    String PAGING_COUNT = "pagingcount";
    String PAGE_VALUE = "pageValue";

    String PORT_PROPERTY = "port";

    // HTTPLISTENER CONSTANTS
    String GATEWAY_HEADER_PREFIX = "x-gate-";
    String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
    String HTTP_HEADER_TRANSFER_ENCODING = "Transfer-Encoding";
    String CONTENT_TYPE = "Content-Type";
    String GLOBAL_PROCESS_ID_HEADER = GATEWAY_HEADER_PREFIX + "globalprocessid";

    // WORKTICKET CONSTANTS
    String HTTP_METHOD = "httpMethod";
    String HTTP_QUERY_STRING = "httpQueryString";
    String HTTP_REMOTE_PORT = "httpRemotePort";
    String HTTP_CHARACTER_ENCODING = "httpCharacterEncoding";
    String HTTP_REMOTE_USER = "httpRemoteUser";
    String HTTP_REMOTE_ADDRESS = "httpRemoteAddress";
    String HTTP_REQUEST_PATH = "httpRequestPath";
    String HTTP_CONTENT_TYPE = "httpContentType";

    // FOLDER PROPERTIES
    String PROPERTY_FILEWRITE_DISPLAYTYPE = "File Write Location";
    String PROPERTY_SWEEPER_DISPLAYTYPE = "Payload Location";
    String PROPERTY_LOCAL_TARGET_LOCATION_DISPLAYTYPE = "Local Target Location";
    String PROPERTY_LOCAL_PAYLOAD_LOCATION_DISPLAYTYPE = "Local Payload Location";
    String PROPERTY_REMOTE_TARGET_LOCATION_DISPLAYTYPE = "Remote Target Location";
    String PROPERTY_REMOTE_PAYLOAD_LOCATION_DISPLAYTYPE = "Remote Payload Location";

    // Credential Properties
    String PROPERTY_LOGIN_CREDENTIAL_DISPLAY_TYPE = "Login Credential";
    String PROPERTY_TRUSTORE_DISPLAY_TYPE = "Trustore Certificate";
    String PROPERTY_SSH_KEYPAIR_DISPLAY_TYPE = "SSH KeyPair";

    String PROPERTY_LOGIN_CREDENTIAL = "LOGIN_CREDENTIAL";
    String PROPERTY_TRUSTORE = "TRUSTSTORE_CERT";
    String PROPERTY_SSH_KEYPAIR = "SSH_KEYPAIR";

    // glass log messages
    String DROPBOX_FILE_TRANSFER = "dropbox_file_transfer";
    String DROPBOX_SERVICE_NAME = "ALLOY Conveyor";
    String DROPBOX_PROCESSOR = "dropboxprocessor";
    String DROPBOX_WORKTICKET_CONSUMED = "Workticket consumed from queue";
    String FILE_QUEUED_SUCCESSFULLY = DROPBOX_SERVICE_NAME + ": File queued for transfer successfully";
    String FILE_STAGED_SUCCESSFULLY = DROPBOX_SERVICE_NAME + ": File staged for delivery";
    String FILE_DOWNLOADED_SUCCESSFULLY = "File downloaded successfully";
    String FILE_QUEUEING_FAILED = "File content queued for transfer got failed";
    String FILE_STAGING_FAILED = "File staging got failed after consuming from queue";
    String FILE_DOWNLOADING_FAILED = "File downloading got failed";

    // internal dl for error notification
    String ERROR_RECEIVER = "com.liaison.mailbox.error.receiver";
    String ERROR_RECEIVER_KEYWORDS = "com.liaison.mailbox.error.receiver.keywords";

    // added for lens visibility property
    String LENS_VISIBLE = "Visible";
    String LENS_INVISIBLE = "Invisible";

    // constants for staged file entry
    String FILE_EXISTS = "fileExists";

    // Constants for LENS Logging
    String REMOTEDOWNLOADER = "REMOTEDOWNLOADER";
    String REMOTEUPLOADER = "REMOTEUPLOADER";
    String SWEEPER = "SWEEPER";
    String CONDITIONALSWEEPER = "CONDITIONALSWEEPER";
    String HTTPASYNCPROCESSOR = "HTTPASYNCPROCESSOR";
    String HTTPSYNCPROCESSOR = "HTTPSYNCPROCESSOR";
    String DROPBOXPROCESSOR = "DROPBOXPROCESSOR";
    String FILEWRITER = "FILEWRITER";

    // Added for read resource for migrator services
    String TYPE_MAILBOX = "mailbox";
    String TYPE_PROCESSOR = "processor";
    String TYPE_PROFILE = "profile";

    // configurations for sla validation
    String DEFAULT_CUSTOMER_SLA = "com.liaison.mailbox.default.customer.sla";
    String DEFAULT_MAILBOX_SLA = "com.liaison.mailbox.default.mailbox.sla";
    String MBX_SLA_CONFIG_UNIT = "com.liaison.mailbox.default.sla.time.unit";
    String DEFAULT_SLA_EMAIL_NOTIFICATION = "com.liaison.mailbox.default.sla.notification";
    String DEFAULT_SLA_MAX_NOTIFICATION_COUNT = "com.liaison.mailbox.default.sla.max.notification.count";
    String DEFAULT_LENS_FAILURE_NOTIFICATION_COUNT = "com.liaison.mailbox.default.lens.failure.notification.count";

    // Default Ports
    int HTTP_PORT = 80;
    int HTTPS_PORT = 443;
    int FTPS_PORT = 21;
    int SFTP_PORT = 22;

    // TTL in days for stale file cleanup in sweeper location
    int STALE_FILE_CLEAN_UP_TTL = 30;
    String PROPERTY_STALE_FILE_CLEAN_UP = "com.liaison.mailbox.sweeper.stalefile.ttl";

    // Timeout range min and max
    int TIMEOUT_RANGE_MIN = 0;
    int TIMEOUT_RANGE_MAX = 60000;

    int SOCKET_TIMEOUT_MIN = 0;
    int SOCKET_TIMEOUT_MAX = 300000;

    String POST = "POST";
    String PUT = "PUT";

    int BYTE_ARRAY_INITIAL_SIZE = 4096;
   
    // Script Execution Timeout range min and max
    int SCRIPT_EXC_TIMEOUT_RANGE_MIN = 30;
    int SCRIPT_EXC_TIMEOUT_RANGE_MAX = 120;
    
    // Http connection timeout range min and max
    int HTTP_CONNECITON_TIMEOUT_RANGE_MIN = 60000;
    int HTTP_CONNECITON_TIMEOUT_RANGE_MAX = 300000;

    //constant for callback
    String KEY_MESSAGE_CONTEXT_URI = "MESSAGE_CONTEXT_URI";
    String CALLBACK = "CALLBACK";
    
    //Stale file TTL range min and max
    int STALE_FILE_TTL_RANGE_MAX = 90;
    int STALE_FILE_TTL_RANGE_MIN = 15;
    String RESUME = "resume";

    String CONFIGURATION_SERVICE_BROKER_URI = "com.liaison.servicebroker.sync.uri";
    String CONFIGURATION_SERVICE_BROKER_ASYNC_URI = "com.liaison.servicebroker.async.uri";
    String CONFIGURATION_CONNECTION_TIMEOUT = "com.liaison.mailbox.sync.processor.connection.timeout";
    String CONFIGURATION_SOCKET_TIMEOUT = "com.liaison.mailbox.sync.processor.socket.timeout";
    String PROPERTY_KEY_MANAGEMENT_BASE_URL = "client.key-management.baseUrl";
    String PROPERTY_GEM_TENANCY_KEY_VALIDATION_URL = "com.liaison.mailbox.tenancy.validation.url";

    String LOCALHOST = "localhost";
    
    //PERMISSION PATH CONSTANT
    String PERMISSION_SCRIPT_PATH = "permission.script.path";
    String SFTP_USER_GROUP_NAME = "sftp.user.group.name";
    
    //DELETION SCRIPT PATH CONSTANT
    String DELETION_SCRIPT_PATH = "deletion.script.path";
    
    //HOME PATH CONSTANTS
    String FTP_PATH="ftp.user.home.path";
    String FTPS_PATH="ftps.user.home.path";
    String SFTP_PATH="sftp.user.home.path";
    String HTTP_PATH="http.user.home.path";
    String HTTPS_PATH="https.user.home.path";
    
    //Gateway types
    String SFTP = "SFTP";
    String FTP = "FTP";
    String FTPS = "FTPS";
    String HTTP = "HTTP";
    String HTTPS = "HTTPS";

    String PROCESSOR_IS_ALREDAY_RUNNING = "The processor is already in progress or Processor Execution state is not available in run time DB for processor ";

    String SERVICE_BROKER_BASE_URL = "com.liaison.servicebroker.api.baseUrl";
    String SERVICE_INSTANCE = "ServiceInstance";
    String PIPELINE = "Pipeline";
    String STUCK_PROCESSORS_IN_RELAY = "Stuck Processors in Relay";
    String MAILBOX_STUCK_PROCESSOR_TIME_UNIT = "com.liaison.mailbox.stuck.processor.time.unit";
    String MAILBOX_STUCK_PROCESSOR_TIME_VALUE = "com.liaison.mailbox.stuck.processor.time.value";
    String MAILBOX_STUCK_PROCESSOR_RECEIVER = "com.liaison.mailbox.stuck.processor.receiver";

    //Cluster type
    String CLUSTER_TYPE = "clusterType";
    String SECURE = "SECURE";

    String LOWSECURE = "LOWSECURE";
    String PROP_DATA_FOLDER_PATTERN = "com.liaison.data.folder.pattern";
    
    // sweeper and conditional sweeper
    int MAX_PAYLOAD_SIZE_IN_WORKTICKET_GROUP = 131072;
    int MAX_NUMBER_OF_FILES_IN_GROUP = 10;
    
    String FILTER = "filter";
    String TRIGGER_FILE_REGEX = "trigger";
    String INPROGRESS_EXTENTION = ".INP";
    
    Object SORT_BY_NAME = "Name";
    Object SORT_BY_SIZE = "Size";

    String WATCH_DOG_SERVICE = "WatchDog Service";
    String STALE_FILE_NOTIFICATION_SUBJECT = "Deleting Stale files in %s";
    String STALE_FILE_NOTIFICATION_EMAIL_FILES = "Files :";
    String STALE_FILE_NOTIFICATION_EMAIL_CONTENT = "Deleting stale files in % named '%s' from the location '%s'";

    //Deployment type
    String RELAY = "RELAY";
    String LOW_SECURE_RELAY = "LOW_SECURE_RELAY";
    String CONVEYOR = "CONVEYOR";

    //process Dc
    String PROCESS_DC = "process_dc";
    String ALL_DATACENTER = "ALL";

    String MANIFEST_DTO = "manifest_dto";
    String TRIGGER_FILE = "Trigger File";

    String FILE_STAGE_REPLICATION_RETRY_DELAY = "com.liaison.mailbox.file.stage.replication.retry.delay";
    String FILE_STAGE_REPLICATION_RETRY_MAX_COUNT = "com.liaison.mailbox.file.stage.replication.max.retry.count";
    String DIRECTORY_STAGE_REPLICATION_RETRY_DELAY = "com.liaison.mailbox.directory.stage.replication.retry.delay";
    String DIRECTORY_STAGE_REPLICATION_RETRY_MAX_COUNT = "com.liaison.mailbox.directory.stage.replication.max.retry.count";

    String URI = "uri";
    String RETRY_COUNT = "retry";

    String RESPONSE_FS2_URI = "response_fs2_uri";

    String EXECUTE = "infinite";

    String CATEGORY = "category";
    String CATEGORY_DEFAULT = "default";

    String PROPERTY_SKIP_KAFKA_QUEUE = "com.liaison.skip.kafka.queue";

    String PROCESS_DC_LIST = "com.liaison.mailbox.processdc.list";

    DecryptableConfiguration CONFIGURATION = LiaisonArchaiusConfiguration.getInstance();

    // sso properties
    String PROPERTY_ENABLE_SSO = "com.liaison.dropbox.sso.enable";
    String SSO_CLIENT_AUTH_URL = "com.liaison.dropbox.sso.auth.url";
    String SSO_CLIENT_ID = "com.liaison.dropbox.sso.clientId";
    String SSO_CLIENT_SECRET = "com.liaison.dropbox.sso.client.secret";

    //SSO constants
    int BYTE_SIZE = 4096;
    String ACL = "acl";
    String TOKEN = "token";
    String ACTIVE = "active";
    String ACCESS_TOKEN = "access_token";
    String AUTHORIZATION = "Authorization";
    String MANIFEST_COOKIE_NAME = "manifest";
    String SIGNATURE_COOKIE_NAME = "signature";
    String TOKEN_TYPE_HINT = "token_type_hint";
    String GROUP_GUID_COOKIE_NAME = "signerPublicKeyGroupGuid";
    String SSO_USER_INFO_URL = "com.liaison.dropbox.sso.user.info.url";
    String TOKEN_AUTHORIZATION  = String.format("%s:%s", CONFIGURATION.getString(SSO_CLIENT_ID), CONFIGURATION.getString(SSO_CLIENT_SECRET));

    String FILE_COUNT_SEPARATOR = "/";
    String DOT_OPERATOR = ".";

    String FAILOVER_MSG_TYPE="failoverMessageType";
    String MESSAGE = "message";
    String PRODUCE_KAFKA_MESSAGE = "produceKafkaMessage";

    String CONFIGURATION_QUEUE_SERVICE_ENABLED = "com.liaison.queueservice.enabled";

    String SERVICE_BROKER_APP_ID = "service-broker";

    String WORK_RESULT_QUEUE_TOPIC_SUFFIX = "workResult";
    String WORK_TICKET_QUEUE_TOPIC_SUFFIX = "workTicket";

    String TOPIC_REPLICATION_FAILOVER_RECEIVER_ID = "com.liaison.queueservice.topic.replicationFailover.receiverId";
    String TOPIC_REPLICATION_FAILOVER_DEFAULT_TOPIC_SUFFIX = "com.liaison.queueservice.topic.replicationFailover.topicSuffix.default";
    String TOPIC_REPLICATION_FAILOVER_ADDITIONAL_TOPIC_SUFFIXES = "com.liaison.queueservice.topic.replicationFailover.topicSuffix.additional";

    String TOPIC_MAILBOX_PROCESSOR_RECEIVER_ID = "com.liaison.queueservice.topic.mailboxProcessor.receiverId";
    String TOPIC_MAILBOX_PROCESSOR_DEFAULT_TOPIC_SUFFIX = "com.liaison.queueservice.topic.mailboxProcessor.topicSuffix.default";
    String TOPIC_MAILBOX_PROCESSOR_ADDITIONAL_TOPIC_SUFFIXES = "com.liaison.queueservice.topic.mailboxProcessor.topicSuffix.additional";

    String TOPIC_SERVICE_BROKER_TO_DROPBOX_RECEIVER_ID = "com.liaison.queueservice.topic.serviceBrokerToDropbox.receiverId";
    String TOPIC_SERVICE_BROKER_TO_DROPBOX_DEFAULT_TOPIC_SUFFIX = "com.liaison.queueservice.topic.serviceBrokerToDropbox.topicSuffix.default";
    String TOPIC_SERVICE_BROKER_TO_DROPBOX_ADDITIONAL_TOPIC_SUFFIXES = "com.liaison.queueservice.topic.serviceBrokerToDropbox.topicSuffix.additional";

    String TOPIC_SERVICE_BROKER_TO_MAILBOX_RECEIVER_ID = "com.liaison.queueservice.topic.serviceBrokerToMailbox.receiverId";
    String TOPIC_SERVICE_BROKER_TO_MAILBOX_DEFAULT_TOPIC_SUFFIX = "com.liaison.queueservice.topic.serviceBrokerToMailbox.topicSuffix.default";
    String TOPIC_SERVICE_BROKER_TO_MAILBOX_ADDITIONAL_TOPIC_SUFFIXES = "com.liaison.queueservice.topic.serviceBrokerToMailbox.topicSuffix.additional";

    String TOPIC_USER_MANAGEMENT_TO_RELAY_DIRECTORY_RECEIVER_ID = "com.liaison.queueservice.topic.userManagementToRelayDirectory.receiverId";
    String TOPIC_USER_MANAGEMENT_TO_RELAY_DIRECTORY_DEFAULT_TOPIC_SUFFIX = "com.liaison.queueservice.topic.userManagementToRelayDirectory.topicSuffix.default";
    String TOPIC_USER_MANAGEMENT_TO_RELAY_DIRECTORY_ADDITIONAL_TOPIC_SUFFIXES = "com.liaison.queueservice.topic.userManagementToRelayDirectory.topicSuffix.additional";

    String TOPIC_MAILBOX_TOPIC_MESSAGE_RECEIVER_ID = "com.liaison.queueservice.topic.mailboxTopicMessage.receiverId";
    String TOPIC_MAILBOX_TOPIC_MESSAGE_DEFAULT_TOPIC_SUFFIX = "com.liaison.queueservice.topic.mailboxTopicMessage.topicSuffix.default";
    String TOPIC_MAILBOX_TOPIC_MESSAGE_ADDITIONAL_TOPIC_SUFFIXES = "com.liaison.queueservice.topic.mailboxTopicMessage.topicSuffix.additional";
}
