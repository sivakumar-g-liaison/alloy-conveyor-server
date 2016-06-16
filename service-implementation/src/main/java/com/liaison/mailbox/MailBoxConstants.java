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

/**
 * Contains the static variables which we can use globally.
 *
 * @author OFS
 */
public interface MailBoxConstants {
	
	// pattern for retry attempts validation
	public final static String retryAttemptsRegex = "^[0-4]$";
	
	// pattern for timeout validation
	public final static String TimeoutRegex = "^([0-9][0-9]{0,3}|[1-5][0-9]{4}|60000)$";
	
	// pattern for number of files validation
	public final static String NUMBER_OF_FILES_THRESHOLD_REGX = "^0*([0-9]|1[0-9]|2[0-5])$";

	// Key descriminator values
	/** File datetime format. */
	public final static String FILETIME_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	/** MailBox Sweeper DateTime Format. */
	public final static String MAILBOXSWEEPER_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	/** Resolved folder or hidden folder name. */
	public final static String SWEEPED_FOLDER_NAME = "sweeped";

	/** The processed file extension */
	public final static String SWEEPED_FILE_EXTN = ".queued";

	/** Property which is used to get the configured receivers from mailbox. */
	public final static String MBX_RCVR_PROPERTY = "emailnotificationids";

	public final static String MBX_STATUS = "EntityStatus";

	public final static String PROCESSOR_TYPE = "ProcessorType";

	public final static String PROCESSOR_PROTOCOL = "Protocol";

	public final static String FOLDER_TYPE = "FolderType";

	public final static String CREDENTIAL_TYPE = "CredentialType";

	public final static String INCOMPLETE_CONFIGURATION = "INCOMPLETE_CONFIGURATION";

	public final static String COMPLETE_STATUS = "COMPLETED";

	public final static String GROUPING_JS_PROP_NAME = "filegroupingjspath";

	public final static String LAST_MODIFIED_TOLERANCE = "last.modified.tolerance";

	public final static String FOLDER_HEADER = "folderlocation";
	public final static String FILE_NAME_HEADER = "filename";
	public final static String COMMENT_HEADER = "comment";
	public final static String FROM_HEADER = "from";

	public final static String URL = "resturl";

	// Added for list files in get payload
	public final static String META_FILE_NAME = ".meta";
	public final static String PROCESSED_FOLDER = "PROCESSED";

	// used for write response for http remote downloader. This is the default
	// processor name.
	public final static String PROCESSOR = "PROCESSOR";

	public final static String MAILBOX_PROCESSOR = "Processor";

	// Added for processing Mount Location from Folder Path given by User
	public final static String MOUNT_LOCATION = "MOUNT_POINT";
	public final static String MOUNT_LOCATION_PATTERN = "(?i:MOUNT_POINT)";

	// Added for Error File Location
	public final static String ERROR_FOLDER = "ERROR";

	// Type of Credentials
	public final static String TRUSTSTORE_CERT = "truststore_cert";
	public final static String SSH_KEYPAIR = "ssh_keypair";
	public final static String LOGIN_CREDENTIAL = "login_credential";

	// Type of Truststore
	public final static String GLOBAL = "GLOBAL";
	public final static String SELFSIGNED = "SELFSIGNED";

	// Added to acl Manifest Request construction
	public final static String DOMAIN_NAME = "SERVICE_BROKER";
	public final static String DOMAIN_TYPE = "ORGANIZATION";
	public final static String PLATFORM_NAME = "SERVICE_BROKER";
	public final static String SERVICE_NAME = "KEYMANAGER";
	public final static String ROLE_NAME = "MailboxAdmin";
	
	public static final String MAILBOX_ID = "mailboxId";

	// properties used to configure sla rules of an mailbox
	public final static String TIME_TO_PICK_UP_FILE_POSTED_TO_MAILBOX = "timetopickupfilepostedtomailbox";
	public final static String TIME_TO_PICK_UP_FILE_POSTED_BY_MAILBOX = "timetopickupfilepostedbymailbox";
	public final static String EMAIL_NOTIFICATION_FOR_SLA_VIOLATION = "emailnotificationforslaviolation";
    public final static String MAX_NUM_OF_NOTIFICATION_FOR_SLA_VIOLATION = "maxnumberofnotificationforslaviolation";
    public final static String LENS_NOTIFICATION_FOR_UPLOADER_FAILURE = "maxnumberoflensnotificationforuploaderfailure";
	

	// sla validation
	public final static String PROCESSOR_NOT_AVAILABLE = "-NA-";
	public final static String PROFILE_NOT_AVAILABLE = "-NA-";
	public final static String DUMMY_MAILBOX_ID_FOR_FSM_STATE =   "ABCDEFGHIJKLMNOPQRSTUVWXYZ789101";
	public final static String DUMMY_PROCESSOR_ID_FOR_FSM_STATE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456";

	// spectrum payload prefix
	public final static String SPECTRUM_PAYLOAD_PREFIX = "/mailboxsweeper/payload/1.0/";

	// FS2 Header Keys
	public final static String KEY_PIPELINE_ID = "PIPELINE_ID";
	public final static String KEY_GLOBAL_PROCESS_ID = "GLOBAL_PROCESS_ID";
	public final static String KEY_RAW_PAYLOAD_SIZE = "RAW_PAYLOAD_SIZE";
	public final static String KEY_SERVICE_INSTANCE_ID = "SERVICE_INSTANCE_ID";
	public final static String KEY_PAYLOAD_DESCRIPTION = "PAYLOAD_DESCRIPTION";
	public final static String KEY_TENANCY_KEY = "TENANCY_KEY";
	public final static String KEY_LENS_VISIBILITY = "LENS_VISIBILITY";
	public final static String KEY_MESSAGE_NAME = "MESSAGE_NAME";
	

	public final static String PIPELINE_FULLY_QUALIFIED_PACKAGE = "com.liaison.service.core.edm.model.Pipeline";
	public final static String PAYLOAD_DESCRIPTION_VALUE = "Sweeped Payload from payload location %s";

	// additional context keys Worker ticket
	public final static String KEY_FOLDER_NAME = "folderName";
	public final static String KEY_FILE_NAME = "fileName";
	public final static String KEY_FILE_CREATED_NAME = "createdTime";
	public final static String KEY_FILE_MODIFIED_NAME = "modifiedTime";
	public final static String KEY_MAILBOX_ID = "mailboxId";
	public final static String KEY_PROCESSOR_ID = "processorId";
	public final static String KEY_OVERWRITE = "overwrite";
	public final static String KEY_FILE_PATH = "path";
	public final static String KEY_WORKTICKET_PROCESSOR_ID = "processorId";
	public final static String KEY_WORKTICKET_TENANCYKEY = "tenancyKey";
	public final static String KEY_TARGET_DIRECTORY = "targetDirectory";
	public final static String KEY_TARGET_DIRECTORY_MODE = "targetDirectoryMode";
	public final static String KEY_MAILBOX_NAME = "mailboxName";
	public static final String STORAGE_IDENTIFIER_TYPE = "Storage Type";

	public final static String TARGET_DIRECTORY_MODE_OVERWRITE = "overwrite";

	//Overwrite
	public final static String OVERWRITE_TRUE = "true";
	public final static String OVERWRITE_FALSE = "false";
	public final static String OVERWRITE_ERROR = "error";

	//GITURI
	public static final String PROPERTY_GITLAB_ACTIVITY_PROJECT_ID = "com.liaison.gitlab.mailbox.script.project.id";
	public static final String PROPERTY_GITLAB_ACTIVITY_PRIVATE_TOKEN = "com.liaison.gitlab.mailbox.script.private_token";
	public static final String PROPERTY_GITLAB_ACTIVITY_SERVER_HOST = "com.liaison.gitlab.mailbox.script.server.host";
	public static final String PROPERTY_GITLAB_ACTIVITY_SERVER_FOLDER = "com.liaison.gitlab.mailbox.script.folder";

	//Empty script file
	public static final String DEFAULT_SCRIPT_TMPLATE_CONTENT = "// Scripts File is empty.";

	//DEFAULTVALUE_FOR_REQUIRED_PROPS

	public static final String DEFAULT_SCRIPT_TEMPLATE_NAME = "mailbox.script.default.template";
	public static final String DEFAULT_JOB_SEARCH_PERIOD_IN_HOURS = "default.job.search.period.in.hours";
	public static final String DEFAULT_INTERRUPT_SIGNAL_FREQUENCY_IN_SEC = "check.for.interrupt.signal.frequency.in.sec";
	public static final String PROCESSOR_SYNC_URL_DISPLAY_PREFIX = "processor.sync.url.display.prefix";
	public static final String PROCESSOR_ASYNC_URL_DISPLAY_PREFIX = "processor.async.url.display.prefix";

	public static String MAILBOX = "MailBox";
	public static final String PROCESSOR_STATUS = "Processor Status";
	public static String INTERRUPT_SIGNAL = "Interrupt Signal";
	public static String EXECUTING_PROCESSORS = "Running Processors";
	public static String PROFILE = "Profile";

	//added for generic error message if any location is missing
	public static final String PAYLOAD_LOCATION = "payload";
	public static final String RESPONSE_LOCATION = "response";
	public static final String FILEWRITE_LOCATION = "file write";
	public static final String COMMON_LOCATION = "payload or file write";

	// added for Dropbox file transfer
	public static final String UM_AUTH_TOKEN = "um-auth-token";
	public static final String LOGIN_ID = "loginId";
	public static final String ACL_MANIFEST_HEADER = "acl-manifest";
	public static final String DBX_WORK_TICKET_PROFILE_NAME = "profileName";
	public static final String UPLOAD_FILE_NAME = "filename";
	public static final String UPLOAD_META = "meta-data";

	public static final String DROPBOX_AUTH_TOKEN = "token";
	public static final String DROPBOX_LOGIN_ID = "loginId";
	public static final String ACL_SIGNED_MANIFEST_HEADER = "acl-signature";
	public static final String ACL_MANIFEST_FAILURE_MESSAGE = "Get manifest failed.";

	public static final int AUTH_SUCCESS_CODE = 200;
	public static final int AUTH_FAILURE_CODE = 401;
	public static final int ACL_RETRIVAL_FAILURE_CODE = 400;
	public static final int CLOSING_DATA_CONNECTION = 226;
	public static final int FTP_FILE_TRANSFER_ACTION_OK = 250;
	public static final int SFTP_FILE_TRANSFER_ACTION_OK = 0;
	public static final String TTL = "ttl";
	public static final String TTL_UNIT = "ttlunit";
	public static final String TTL_IN_SECONDS = "ttlinseconds";
	public static final String DROPBOX_PAYLOAD_TTL_DAYS = "com.liaison.dropbox.payload.ttl.days";
	public static final String MAILBOX_PAYLOAD_TTL_DAYS = "com.liaison.mailbox.payload.ttl.days";
	public static final String MAILBOX_PAYLOAD_TTL_DAYS_DEFAULT = "7";
	public static final String FS2_STORAGE_DEFAULT_USE = "fs2.storage.file.default.use";
	public static final String VALUE_FOR_DEFAULT_TTL = "30";
	public static final String TTL_UNIT_SECONDS = "Seconds";
	public static final String TTL_UNIT_MINUTES = "Minutes";
	public static final String CUSTOM_TTL_UNIT = "CustomTTL";
	public static final String TTL_NUMBER = "TTLnumber";
	public static final String TTL_UNIT_HOURS = "Hours";
	public static final String TTL_UNIT_DAYS = "Days";
	public static final String TTL_UNIT_WEEKS = "Weeks";
	public static final String TTL_UNIT_MONTHS = "Months";
	public static final String TTL_UNIT_YEARS = "Years";
	public static final String FS2_OPTIONS_TTL="fs2.options.ttl";

	//charsetname
	public static final String CHARSETNAME = "UTF-8";
	public final static String TOKEN_SEPARATOR = "::";

	public static final String DUMMY_MANIFEST_USAGE_PROPERTY = "use.dummy.manifest.as.backup";
	public static final String DUMMY_MANIFEST_PROPERTY = "dummy.acl.manifest.json";
	public static final String REQUEST_HEADER = "Request Header";
	public static final String PROPERTIES_FILE = "Properties file";

	// added for self signed truststore
	public static final String SELF_SIGNED_TRUSTORE_PASSPHRASE = "mailbox.self.signed.trustore.passphrase";

	public static final String DEFAULT_FIRST_CORNER_NAME = "SECOND CORNER";
	public static final String PROPERTY_FIRST_CORNER_NAME = "com.liaison.secondcorner.name";
	public static final String DEFAULT_SECOND_CORNER_NAME = "SECOND CORNER";
	public static final String PROPERTY_SECOND_CORNER_NAME = "com.liaison.secondcorner.name";
	public static final String DEFAULT_THIRD_CORNER_NAME = "THIRD CORNER";
	public static final String PROPERTY_THIRD_CORNER_NAME = "com.liaison.thirdcorner.name";
	public static final String DEFAULT_FOURTH_CORNER_NAME = "FOURTH CORNER";
	public static final String PROPERTY_FOURTH_CORNER_NAME = "com.liaison.fourthcorner.name";


	// static properties
	public static final String PROPERTY_URL = "url";
	public static final String PROPERTY_PORT = "port";
	public static final String PROPERTY_HTTP_VERSION = "httpVersion";
	public static final String PROPERTY_HTTP_VERB = "httpVerb";
	public static final String PROPERTY_CONTENT_TYPE = "contentType";
	public static final String PROPERTY_CHUNKED_ENCODING = "chunkedEncoding";
	public static final String PROPERTY_CONNECTION_TIMEOUT = "connectionTimeout";
	public static final String PROPERTY_SOCKET_TIMEOUT = "socketTimeout";
	public static final String PROPERTY_RETRY_ATTEMPTS = "retryAttempts";
	public static final String PROPERTY_OTHER_REQUEST_HEADERS = "otherRequestHeader";
	public static final String PROPERTY_PASSIVE = "passive";
	public static final String PROPERTY_BINARY = "binary";
	public static final String PROPERTY_ERROR_FILE_LOCATION = "errorFileLocation";
	public static final String PROPERTY_PROCESSED_FILE_LOCATION = "processedFileLocation";
	public static final String PROPERTY_PIPELINEID = "pipelineID";
	public static final String PROPERTY_DELETE_FILE_AFTER_SWEEP = "deleteFileAfterSweep";
	public static final String PROPERTY_FILE_RENAME_FORMAT = "fileRenameFormat";
	public static final String PROPERTY_NO_OF_FILES_THRESHOLD = "numOfFilesThreshold";
	public static final String PROPERTY_PAYLOAD_SIZE_THRESHOLD = "payloadSizeThreshold";
	public static final String PROPERTY_SWEEPED_FILE_LOCATION = "sweepedFileLocation";
	public static final String PROPERTY_HTTPLISTENER_AUTH_CHECK = "httpListenerAuthCheckRequired";
	public static final String PROPERTY_HTTPLISTENER_PIPELINEID = "httpListenerPipeLineId";
	public static final String PROPERTY_HTTPLISTENER_PAYLOAD_LOCATION = "httpListenerPayload";
	public static final String PROPERTY_HTTPLISTENER_SECUREDPAYLOAD = "securedPayload";
	public static final String PROPERTY_LENS_VISIBILITY = "lensVisibility";
	public static final String PROPERTY_TENANCY_KEY = "MBX_TENANCY_KEY";

	// Properties for sweeper grouping boundary condition
	public final static String PAYLOAD_SIZE_THRESHOLD = "payloadsizethreshold";
	public final static String NUMBER_OF_FILES_THRESHOLD = "numoffilesthreshold";
	public final static String FILE_RENAME_FORMAT_PROP_NAME = "filerenameformat";
	public final static String SWEEPED_FILE_LOCATION = "sweepedfilelocation";
	public final static String PROCESSED_FILE_LOCATION = "processedfilelocation";
	public final static String ERROR_FILE_LOCATION = "errorfilelocation";
	public static final String HTTPLISTENER_AUTH_CHECK = "httplistenerauthcheckrequired";

	public static final String ADD_NEW_PROPERTY = "add new -->";

	public static final String PAGING_OFFSET = "pagingoffset";
	public static final String PAGING_COUNT = "pagingcount";

	public static final String PORT_PROPERTY = "port";

	//HTTPLISTENER CONSTANTS
	public static final String GATEWAY_HEADER_PREFIX = "x-gate-";
	public static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HTTP_HEADER_TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    public static final String GLOBAL_PROCESS_ID_HEADER = GATEWAY_HEADER_PREFIX + "globalprocessid";

	//WORKTICKET CONSTANTS
	public static final String HTTP_METHOD = "httpMethod";
	public static final String HTTP_QUERY_STRING = "httpQueryString";
	public static final String HTTP_REMOTE_PORT = "httpRemotePort";
	public static final String HTTP_CHARACTER_ENCODING = "httpCharacterEncoding";
	public static final String HTTP_REMOTE_USER = "httpRemoteUser";
	public static final String HTTP_REMOTE_ADDRESS = "httpRemoteAddress";
	public static final String HTTP_REQUEST_PATH = "httpRequestPath";
	public static final String HTTP_CONTENT_TYPE = "httpContentType";

	//FOLDER PROPERTIES
	public static final String PROPERTY_FILEWRITE_DISPLAYTYPE = "File Write Location";
	public static final String PROPERTY_SWEEPER_DISPLAYTYPE = "Payload Location";
	public static final String PROPERTY_LOCAL_TARGET_LOCATION_DISPLAYTYPE = "Local Target Location";
	public static final String PROPERTY_LOCAL_PAYLOAD_LOCATION_DISPLAYTYPE = "Local Payload Location";
	public static final String PROPERTY_REMOTE_TARGET_LOCATION_DISPLAYTYPE = "Remote Target Location";
	public static final String PROPERTY_REMOTE_PAYLOAD_LOCATION_DISPLAYTYPE = "Remote Payload Location";

	//Credential Properties
	public static final String PROPERTY_LOGIN_CREDENTIAL_DISPLAY_TYPE = "Login Credential";
	public static final String PROPERTY_TRUSTORE_DISPLAY_TYPE = "Trustore Certificate";
	public static final String PROPERTY_SSH_KEYPAIR_DISPLAY_TYPE = "SSH KeyPair";

	public static final String PROPERTY_LOGIN_CREDENTIAL = "LOGIN_CREDENTIAL";
    public static final String PROPERTY_TRUSTORE = "TRUSTSTORE_CERT";
    public static final String PROPERTY_SSH_KEYPAIR = "SSH_KEYPAIR";

    // glass log messages
    public static final String DROPBOX_FILE_TRANSFER = "dropbox_file_transfer";
	public static final String DROPBOX_SERVICE_NAME = "ALLOY Conveyor";
	public static final String DROPBOX_PROCESSOR = "dropboxprocessor";
	public static final String DROPBOX_WORKTICKET_CONSUMED = "Workticket consumed from queue";
	public static final String FILE_QUEUED_SUCCESSFULLY = DROPBOX_SERVICE_NAME + ": File queued for transfer successfully";
    public static final String FILE_STAGED_SUCCESSFULLY = DROPBOX_SERVICE_NAME + ": File staged for delivery";
    public static final String FILE_DOWNLOADED_SUCCESSFULLY = "File downloaded successfully";
    public static final String FILE_QUEUEING_FAILED = "File content queued for transfer got failed";
    public static final String FILE_STAGING_FAILED = "File staging got failed after consuming from queue";
    public static final String FILE_DOWNLOADING_FAILED = "File downloading got failed";

    // internal dl for error notification
    public static final String ERROR_RECEIVER = "com.liaison.mailbox.error.receiver";

    //added for lens visibility property
    public static final String LENS_VISIBLE = "Visible";
    public static final String LENS_INVISIBLE = "Invisible";

    //constants for staged file entry
    public static final String FILE_EXISTS = "fileExists";
    
    //Constants for LENS Logging
    public static final String HTTPASYNCPROCESSOR = "HTTPASYNCPROCESSOR";
    public static final String HTTPSYNCPROCESSOR = "HTTPSYNCPROCESSOR";
    public static final String DROPBOXPROCESSOR = "DROPBOXPROCESSOR";
    public static final String FILEWRITER = "FILEWRITER";
    /**
     * STATIC KEY used to encrypt/decrypt the token. The token contains username:: tokenCreatedDate::mostRecentRevisionDate and it doesn't contain any sensitive data
     */
    public static byte[] STATIC_KEY = "A3$1E*8^%ER256%$".getBytes();

    /**
     * ivBytes used to encrypt/decrypt the token. The token contains username:: tokenCreatedDate::mostRecentRevisionDate and it doesn't contain any sensitive data
     */
    public static byte[] IV_BYTES = new byte[] { (byte) 0x8E, 0x12, 0x39, (byte) 0x9C, 0x07, 0x72, 0x6F, 0x5A,
                         (byte) 0x8E, 0x12, 0x39, (byte) 0x9C, 0x07, 0x72, 0x6F, 0x5A };

    public static int ENCRYPT_MODE = 1;
    public static int DECRYPT_MODE = 2;
    
	// Added for read resource for migrator services 
    public static String TYPE_MAILBOX = "mailbox";
    public static String TYPE_PROCESSOR = "processor";
    public static String TYPE_PROFILE = "profile";
    
    // configurations for sla validation
    public static String DEFAULT_CUSTOMER_SLA = "com.liaison.mailbox.default.customer.sla";
    public static String DEFAULT_MAILBOX_SLA = "com.liaison.mailbox.default.mailbox.sla";
    public static String  MBX_SLA_CONFIG_UNIT = "com.liaison.mailbox.default.sla.time.unit";
    public static String DEFAULT_SLA_EMAIL_NOTIFICATION = "com.liaison.mailbox.default.sla.notification";
    public static String DEFAULT_SLA_MAX_NOTIFICATION_COUNT = "com.liaison.mailbox.default.sla.max.notification.count";
    public static String DEFAULT_LENS_FAILURE_NOTIFICATION_COUNT = "com.liaison.mailbox.default.lens.failure.notification.count";

    /**
     * Properties required for Healthcheck
     */
    public static final String DTDM_DB_DRIVER_PROPERTY = "com.liaison.dtdm.DB_DRIVER";
    public static final String DTDM_DB_URL_PROPERTY = "com.liaison.dtdm.DB_URL";
    public static final String DTDM_DB_USER_PROPERTY = "com.liaison.dtdm.DB_USER";
    public static final String DTDM_DB_PASSWORD_PROPERTY = "com.liaison.dtdm.DB_PASSWORD";

    public static final String RTDM_DB_DRIVER_PROPERTY = "com.liaison.rtdm.DB_DRIVER";
    public static final String RTDM_DB_URL_PROPERTY = "com.liaison.rtdm.DB_URL";
    public static final String RTDM_DB_USER_PROPERTY = "com.liaison.rtdm.DB_USER";
    public static final String RTDM_DB_PASSWORD_PROPERTY = "com.liaison.rtdm.DB_PASSWORD";
    
    //Default Ports
    public final static int HTTP_PORT = 80;
    public final static int HTTPS_PORT = 443;
    public final static int FTPS_PORT = 21;
    public final static int SFTP_PORT = 22;
    
    // TTL in days for stale file cleanup in sweeper location
    public static final int STALE_FILE_CLEAN_UP_TTL = 30;
    public static final String PROPERTY_STALE_FILE_CLEAN_UP = "com.liaison.mailbox.sweeper.stalefile.ttl";
    
    // Timeout range min and max
    public static final int TIMEOUT_RANGE_MIN = 0;
    public static final int TIMEOUT_RANGE_MAX = 60000;

    String POST = "POST";
    String PUT = "PUT";
   
}
