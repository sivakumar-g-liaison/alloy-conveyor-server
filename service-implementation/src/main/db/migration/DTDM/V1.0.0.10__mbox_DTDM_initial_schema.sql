CREATE TABLE GATEWAY_OWNR.CREDENTIAL
(
	PGUID CHAR(32) CONSTRAINT NN01_CREDENTIAL NOT NULL,
	PROCESSOR_GUID CHAR(32) CONSTRAINT NN02_CREDENTIAL NOT NULL,
	TYPE VARCHAR2(32) CONSTRAINT NN03_CREDENTIAL NOT NULL
		CONSTRAINT CK01_CREDENTIAL CHECK (TYPE IN ('TRUSTSTORE_CERT', 'SSH_KEYPAIR', 'LOGIN_CREDENTIAL')),
	URI_GUID VARCHAR2(128),
	USERNAME VARCHAR2(128),
	PASSWORD VARCHAR2(128),
	IDP_TYPE VARCHAR2(128),
	IDP_URI VARCHAR2(128)
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
	NOPARALLEL
;

ALTER TABLE GATEWAY_OWNR.CREDENTIAL ADD 
	CONSTRAINT PK_CREDENTIAL PRIMARY KEY (PGUID)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

ALTER TABLE GATEWAY_OWNR.CREDENTIAL ADD 
	CONSTRAINT AK01_CREDENTIAL UNIQUE (PROCESSOR_GUID, TYPE)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

CREATE INDEX FK01_CREDENTIAL ON GATEWAY_OWNR.CREDENTIAL
(
	PROCESSOR_GUID
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
;

CREATE TABLE GATEWAY_OWNR.FOLDER
(
	PGUID CHAR(32) CONSTRAINT NN01_FOLDER NOT NULL,
	PROCESSOR_GUID CHAR(32) CONSTRAINT NN02_FOLDER NOT NULL,
	DESCRIPTION VARCHAR2(512) CONSTRAINT NN03_FOLDER NOT NULL,
	TYPE VARCHAR2(32) CONSTRAINT NN04_FOLDER NOT NULL
		CONSTRAINT CK01_FOLDER CHECK (TYPE IN ('PAYLOAD_LOCATION', 'RESPONSE_LOCATION')),
	URI VARCHAR2(512) NOT NULL
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
	NOPARALLEL
;

ALTER TABLE GATEWAY_OWNR.FOLDER ADD 
	CONSTRAINT PK_FOLDER PRIMARY KEY (PGUID)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

ALTER TABLE GATEWAY_OWNR.FOLDER ADD 
	CONSTRAINT AK01_FOLDER UNIQUE (PROCESSOR_GUID, TYPE)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

CREATE INDEX FK01_FOLDER ON GATEWAY_OWNR.FOLDER
(
	PROCESSOR_GUID
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
;

CREATE TABLE GATEWAY_OWNR.MAILBOX
(
	PGUID CHAR(32) CONSTRAINT NN01_MAILBOX NOT NULL,
	NAME VARCHAR2(128) CONSTRAINT NN02_MAILBOX NOT NULL,
	TENANCY_KEY VARCHAR2(128) CONSTRAINT NN03_MAILBOX NOT NULL,
	STATUS VARCHAR2(16) CONSTRAINT NN04_MAILBOX NOT NULL
		CONSTRAINT CK01_MAILBOX CHECK (STATUS IN ('ACTIVE', 'INACTIVE')),
	DESCRIPTION VARCHAR2(512) CONSTRAINT NN05_MAILBOX NOT NULL,
	SHARD_KEY VARCHAR2(512)
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
	NOPARALLEL
;

ALTER TABLE GATEWAY_OWNR.MAILBOX ADD 
	CONSTRAINT PK_MAILBOX PRIMARY KEY (PGUID)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

ALTER TABLE GATEWAY_OWNR.MAILBOX ADD 
	CONSTRAINT AK01_MAILBOX UNIQUE (NAME, TENANCY_KEY)
		USING INDEX
			TABLESPACE JUNK
			STORAGE
			(
				PCTINCREASE 0
			)
;

CREATE TABLE GATEWAY_OWNR.MAILBOX_PROPERTY
(
	PGUID CHAR(32) CONSTRAINT NN01_MAILBOX_PROPERTY NOT NULL,
	MAILBOX_GUID CHAR(32) CONSTRAINT NN02_MAILBOX_PROPERTY NOT NULL,
	NAME VARCHAR2(128) CONSTRAINT NN03_MAILBOX_PROPERTY NOT NULL,
	VALUE VARCHAR2(512) CONSTRAINT NN04_MAILBOX_PROPERTY NOT NULL
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
	NOPARALLEL
;

ALTER TABLE GATEWAY_OWNR.MAILBOX_PROPERTY ADD 
	CONSTRAINT PK_MAILBOX_PROPERTY PRIMARY KEY (PGUID)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

ALTER TABLE GATEWAY_OWNR.MAILBOX_PROPERTY ADD 
	CONSTRAINT AK01_MAILBOX_PROPERTY UNIQUE (MAILBOX_GUID, NAME)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

CREATE INDEX FK01_MAILBOX_PROPERTY ON GATEWAY_OWNR.MAILBOX_PROPERTY
(
	MAILBOX_GUID
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
;

CREATE TABLE GATEWAY_OWNR.MAILBOX_SVC_INSTANCE
(
	SERVICE_INSTANCE_GUID CHAR(32) CONSTRAINT NN01_MAILBOX_SVC_INSTANCE NOT NULL,
	MAILBOX_GUID CHAR(32) CONSTRAINT NN02_MAILBOX_SVC_INSTANCE NOT NULL
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
	NOPARALLEL
;

ALTER TABLE GATEWAY_OWNR.MAILBOX_SVC_INSTANCE ADD 
	CONSTRAINT PK_MAILBOX_SVC_INSTANCE PRIMARY KEY (SERVICE_INSTANCE_GUID, MAILBOX_GUID)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

CREATE INDEX FK01_MAILBOX_SVC_INSTANCE ON GATEWAY_OWNR.MAILBOX_SVC_INSTANCE
(
	SERVICE_INSTANCE_GUID
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
;

CREATE INDEX FK02_MAILBOX_SVC_INSTANCE ON GATEWAY_OWNR.MAILBOX_SVC_INSTANCE
(
	MAILBOX_GUID
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
;

CREATE TABLE GATEWAY_OWNR.PROCESSOR
(
	PGUID CHAR(32) CONSTRAINT NN01_PROCESSOR NOT NULL,
	MAILBOX_GUID CHAR(32) CONSTRAINT NN02_PROCESSOR NOT NULL,
	SERVICE_INSTANCE_GUID CHAR(32) CONSTRAINT NN03_PROCESSOR NOT NULL,
	NAME VARCHAR2(512) CONSTRAINT NN04_PROCESSOR NOT NULL,
	STATUS VARCHAR2(128) CONSTRAINT NN05_PROCESSOR NOT NULL,
	TYPE VARCHAR2(128) CONSTRAINT NN06_PROCESSOR NOT NULL
		CONSTRAINT CK01_PROCESSOR CHECK (TYPE IN ('REMOTEDOWNLOADER', 'REMOTEUPLOADER', 'SWEEPER', 'HTTPASYNCPROCESSOR', 'HTTPSYNCPROCESSOR')),
	DESCRIPTION VARCHAR2(512) CONSTRAINT NN07_PROCESSOR NOT NULL,
	PROTOCOL VARCHAR2(128) CONSTRAINT NN08_PROCESSOR NOT NULL,
	PROPERTIES VARCHAR2(2048),
	JAVASCRIPT_URI VARCHAR2(512)
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
	NOPARALLEL
;

ALTER TABLE GATEWAY_OWNR.PROCESSOR ADD 
	CONSTRAINT PK_PROCESSOR PRIMARY KEY (PGUID)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

ALTER TABLE GATEWAY_OWNR.PROCESSOR ADD 
	CONSTRAINT AK01_PROCESSOR UNIQUE (MAILBOX_GUID, NAME)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

CREATE INDEX FK01_PROCESSOR ON GATEWAY_OWNR.PROCESSOR
(
	MAILBOX_GUID
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
;

CREATE INDEX FK02_PROCESSOR ON GATEWAY_OWNR.PROCESSOR
(
	SERVICE_INSTANCE_GUID
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
;

CREATE TABLE GATEWAY_OWNR.PROCESSOR_PROPERTY
(
	PGUID CHAR(32) CONSTRAINT NN01_PROCESSOR_PROPERTY NOT NULL,
	PROCESSOR_GUID CHAR(32) CONSTRAINT NN02_PROCESSOR_PROPERTY NOT NULL,
	NAME VARCHAR2(128) CONSTRAINT NN03_PROCESSOR_PROPERTY NOT NULL,
	VALUE VARCHAR2(512) CONSTRAINT NN04_PROCESSOR_PROPERTY NOT NULL
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
	NOPARALLEL
;

ALTER TABLE GATEWAY_OWNR.PROCESSOR_PROPERTY ADD 
	CONSTRAINT PK_PROCESSOR_PROPERTY PRIMARY KEY (PGUID)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

ALTER TABLE GATEWAY_OWNR.PROCESSOR_PROPERTY ADD 
	CONSTRAINT AK01_PROCESSOR_PROPERTY UNIQUE (PROCESSOR_GUID, NAME)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

CREATE INDEX FK01_PROCESSOR_PROPERTY ON GATEWAY_OWNR.PROCESSOR_PROPERTY
(
	PROCESSOR_GUID
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
;

CREATE TABLE GATEWAY_OWNR.SCHED_PROCESSOR
(
	SCHED_PROFILE_GUID CHAR(32) CONSTRAINT NN01_SCHED_PROCESSOR NOT NULL,
	PROCESSOR_GUID CHAR(32) CONSTRAINT NN02_SCHED_PROCESSOR NOT NULL
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
	NOPARALLEL
;

ALTER TABLE GATEWAY_OWNR.SCHED_PROCESSOR ADD 
	CONSTRAINT PK_SCHED_PROCESSOR PRIMARY KEY (SCHED_PROFILE_GUID, PROCESSOR_GUID)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

CREATE INDEX FK01_SCHED_PROCESSOR ON GATEWAY_OWNR.SCHED_PROCESSOR
(
	SCHED_PROFILE_GUID
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
;

CREATE INDEX FK02_SCHED_PROCESSOR ON GATEWAY_OWNR.SCHED_PROCESSOR
(
	PROCESSOR_GUID
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
;

CREATE TABLE GATEWAY_OWNR.SCHED_PROFILE
(
	PGUID CHAR(32) CONSTRAINT NN01_SCHED_PROFILE NOT NULL,
	NAME VARCHAR2(128) CONSTRAINT NN02_SCHED_PROFILE NOT NULL
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
	NOPARALLEL
;

ALTER TABLE GATEWAY_OWNR.SCHED_PROFILE ADD 
	CONSTRAINT PK_SCHED_PROFILE PRIMARY KEY (PGUID)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

ALTER TABLE GATEWAY_OWNR.SCHED_PROFILE ADD 
	CONSTRAINT AK01_SCHED_PROFILE UNIQUE (NAME)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

CREATE TABLE GATEWAY_OWNR.SERVICE_INSTANCE
(
	PGUID CHAR(32) CONSTRAINT NN01_SERVICE_INSTANCE NOT NULL,
	SERVICE_INSTANCE_ID VARCHAR2(32) CONSTRAINT NN02_SERVICE_INSTANCE NOT NULL
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		PCTINCREASE 0
	)
	NOPARALLEL
;

ALTER TABLE GATEWAY_OWNR.SERVICE_INSTANCE ADD 
	CONSTRAINT PK_SERVICE_INSTANCE PRIMARY KEY (PGUID)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

ALTER TABLE GATEWAY_OWNR.SERVICE_INSTANCE ADD 
	CONSTRAINT AK01_SERVICE_INSTANCE UNIQUE (SERVICE_INSTANCE_ID)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				PCTINCREASE 0
			)
;

ALTER TABLE GATEWAY_OWNR.CREDENTIAL ADD 
	CONSTRAINT FK01_CREDENTIAL FOREIGN KEY (PROCESSOR_GUID)
		REFERENCES GATEWAY_OWNR.PROCESSOR (PGUID) ON DELETE CASCADE ENABLE
;

ALTER TABLE GATEWAY_OWNR.FOLDER ADD 
	CONSTRAINT FK01_FOLDER FOREIGN KEY (PROCESSOR_GUID)
		REFERENCES GATEWAY_OWNR.PROCESSOR (PGUID) ON DELETE CASCADE ENABLE
;

ALTER TABLE GATEWAY_OWNR.MAILBOX_PROPERTY ADD 
	CONSTRAINT FK01_MAILBOX_PROPERTY FOREIGN KEY (MAILBOX_GUID)
		REFERENCES GATEWAY_OWNR.MAILBOX (PGUID) ON DELETE CASCADE ENABLE
;

ALTER TABLE GATEWAY_OWNR.MAILBOX_SVC_INSTANCE ADD 
	CONSTRAINT FK01_MAILBOX_SVC_INSTANCE FOREIGN KEY (SERVICE_INSTANCE_GUID)
		REFERENCES GATEWAY_OWNR.SERVICE_INSTANCE (PGUID)
;

ALTER TABLE GATEWAY_OWNR.MAILBOX_SVC_INSTANCE ADD 
	CONSTRAINT FK02_MAILBOX_SVC_INSTANCE FOREIGN KEY (MAILBOX_GUID)
		REFERENCES GATEWAY_OWNR.MAILBOX (PGUID)
;

ALTER TABLE GATEWAY_OWNR.PROCESSOR ADD 
	CONSTRAINT FK01_PROCESSOR FOREIGN KEY (MAILBOX_GUID)
		REFERENCES GATEWAY_OWNR.MAILBOX (PGUID) ON DELETE CASCADE ENABLE
;

ALTER TABLE GATEWAY_OWNR.PROCESSOR ADD 
	CONSTRAINT FK02_PROCESSOR FOREIGN KEY (SERVICE_INSTANCE_GUID)
		REFERENCES GATEWAY_OWNR.SERVICE_INSTANCE (PGUID)
;

ALTER TABLE GATEWAY_OWNR.PROCESSOR_PROPERTY ADD 
	CONSTRAINT FK01_PROCESSOR_PROPERTY FOREIGN KEY (PROCESSOR_GUID)
		REFERENCES GATEWAY_OWNR.PROCESSOR (PGUID) ON DELETE CASCADE ENABLE
;

ALTER TABLE GATEWAY_OWNR.SCHED_PROCESSOR ADD 
	CONSTRAINT FK01_SCHED_PROCESSOR FOREIGN KEY (SCHED_PROFILE_GUID)
		REFERENCES GATEWAY_OWNR.SCHED_PROFILE (PGUID) ON DELETE CASCADE ENABLE
;

ALTER TABLE GATEWAY_OWNR.SCHED_PROCESSOR ADD 
	CONSTRAINT FK02_SCHED_PROCESSOR FOREIGN KEY (PROCESSOR_GUID)
		REFERENCES GATEWAY_OWNR.PROCESSOR (PGUID) ON DELETE CASCADE ENABLE
;

