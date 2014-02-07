CREATE TABLE GATEWAY_OWNR.CREDENTIAL
(
	PGUID CHAR(32) NOT NULL,
	PROCESSOR_GUID CHAR(32) NOT NULL,
	TYPE VARCHAR2(128) NOT NULL,
	URI VARCHAR2(128),
	USERNAME VARCHAR2(128),
	PASSWORD VARCHAR2(128),
	IDP_TYPE VARCHAR2(128),
	IDP_URI VARCHAR2(128)
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		INITIAL 64K
		NEXT 64K
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
				INITIAL 64K
				NEXT 64K
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
		INITIAL 64K
		NEXT 64K
		PCTINCREASE 0
	)
;

CREATE TABLE GATEWAY_OWNR.FOLDER
(
	PGUID CHAR(32) NOT NULL,
	PROCESSOR_GUID CHAR(32) NOT NULL,
	DESCRIPTION VARCHAR2(250) NOT NULL,
	TYPE VARCHAR2(50) NOT NULL,
	URI VARCHAR2(50)
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		INITIAL 64K
		NEXT 64K
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
				INITIAL 64K
				NEXT 64K
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
		INITIAL 64K
		NEXT 64K
		PCTINCREASE 0
	)
;

CREATE TABLE GATEWAY_OWNR.MAILBOX
(
	PGUID CHAR(32) NOT NULL,
	NAME VARCHAR2(128) NOT NULL,
	DESCRIPTION VARCHAR2(1024) NOT NULL,
	STATUS VARCHAR2(128) NOT NULL,
	SERVICE_INST_ID NUMBER,
	SHARD_KEY VARCHAR2(512)
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		INITIAL 64K
		NEXT 64K
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
				INITIAL 64K
				NEXT 64K
				PCTINCREASE 0
			)
;

CREATE TABLE GATEWAY_OWNR.MAILBOX_PROPERTY
(
	PGUID CHAR(32) NOT NULL,
	MAILBOX_GUID CHAR(32) NOT NULL,
	NAME VARCHAR2(128) NOT NULL,
	VALUE VARCHAR2(512) NOT NULL
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		INITIAL 64K
		NEXT 64K
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
				INITIAL 64K
				NEXT 64K
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
		INITIAL 64K
		NEXT 64K
		PCTINCREASE 0
	)
;

CREATE TABLE GATEWAY_OWNR.PROCESSOR
(
	PGUID CHAR(32) NOT NULL,
	MAILBOX_GUID CHAR(32) NOT NULL,
	DESCRIPTION VARCHAR2(512) NOT NULL,
	STATUS VARCHAR2(128) NOT NULL,
	TYPE VARCHAR2(128) NOT NULL,
	PROPERTIES VARCHAR2(2048),
	JAVASCRIPT_URI VARCHAR2(512),
	NAME VARCHAR2(512),
	PROTOCOL VARCHAR2(128) NOT NULL,
	EXEC_STATUS VARCHAR2(128),
	CERTIFICATE_URI VARCHAR2(512),
	SELF_SIGNED CHAR(1),
	TRUSTSTORE_ID CHAR(32)
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		INITIAL 64K
		NEXT 64K
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
				INITIAL 64K
				NEXT 64K
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
		INITIAL 64K
		NEXT 64K
		PCTINCREASE 0
	)
;

CREATE TABLE GATEWAY_OWNR.PROCESSOR_PROPERTY
(
	PGUID CHAR(32) NOT NULL,
	PROCESSOR_GUID CHAR(32) NOT NULL,
	NAME VARCHAR2(128) NOT NULL,
	VALUE VARCHAR2(512) NOT NULL
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		INITIAL 64K
		NEXT 64K
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
				INITIAL 64K
				NEXT 64K
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
		INITIAL 64K
		NEXT 64K
		PCTINCREASE 0
	)
;

CREATE TABLE GATEWAY_OWNR.SCHED_PROCESSOR
(
	PGUID CHAR(32) NOT NULL,
	SCHED_PROFILE_GUID CHAR(32) NOT NULL,
	PROCESSOR_GUID CHAR(32) NOT NULL
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		INITIAL 64K
		NEXT 64K
		PCTINCREASE 0
	)
	NOPARALLEL
;

ALTER TABLE GATEWAY_OWNR.SCHED_PROCESSOR ADD 
	CONSTRAINT PK_SCHED_PROCESSOR PRIMARY KEY (PGUID)
		USING INDEX
			TABLESPACE GATEWAY_CONF
			STORAGE
			(
				INITIAL 64K
				NEXT 64K
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
		INITIAL 64K
		NEXT 64K
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
		INITIAL 64K
		NEXT 64K
		PCTINCREASE 0
	)
;

CREATE TABLE GATEWAY_OWNR.SCHED_PROFILE
(
	PGUID CHAR(32) NOT NULL,
	NAME VARCHAR2(128) NOT NULL
)
	TABLESPACE GATEWAY_CONF
	STORAGE
	(
		INITIAL 64K
		NEXT 64K
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
				INITIAL 64K
				NEXT 64K
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

ALTER TABLE GATEWAY_OWNR.PROCESSOR ADD 
	CONSTRAINT FK01_PROCESSOR FOREIGN KEY (MAILBOX_GUID)
		REFERENCES GATEWAY_OWNR.MAILBOX (PGUID) ON DELETE CASCADE ENABLE
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

