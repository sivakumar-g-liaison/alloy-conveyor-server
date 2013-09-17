--------------------------------------------------------
--  File created - Wednesday-September-04-2013   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Table MAILBOXES
--------------------------------------------------------

  CREATE TABLE "MAILBOXES" ("PGUID" CHAR(32), "SERVICE_INST_ID" NUMBER, "MBX_NAME" VARCHAR2(128), "MBX_DESC" VARCHAR2(1024), "MBX_STATUS" VARCHAR2(128), "SHARD_KEY" VARCHAR2(512)); 

   COMMENT ON COLUMN "MAILBOXES"."PGUID" IS 'primary key';
   COMMENT ON COLUMN "MAILBOXES"."MBX_NAME" IS 'name of the mailbox';
--------------------------------------------------------
--  DDL for Index MAILBOXES_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "MAILBOXES_PK" ON "MAILBOXES" ("PGUID");
--------------------------------------------------------
--  Constraints for Table MAILBOXES
--------------------------------------------------------

  ALTER TABLE "MAILBOXES" ADD CONSTRAINT "MAILBOXES_PK" PRIMARY KEY ("PGUID") ENABLE;
  ALTER TABLE "MAILBOXES" MODIFY ("MBX_STATUS" NOT NULL ENABLE);
  ALTER TABLE "MAILBOXES" MODIFY ("MBX_NAME" NOT NULL ENABLE);
  ALTER TABLE "MAILBOXES" MODIFY ("PGUID" NOT NULL ENABLE);

  
  --------------------------------------------------------
--  File created - Wednesday-September-04-2013   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Table MAILBOX_PROPERTIES
--------------------------------------------------------

  CREATE TABLE "MAILBOX_PROPERTIES" ("PGUID" CHAR(32), "MAILBOX_GUID" CHAR(32), "MBX_PROP_NAME" VARCHAR2(128), "MBX_PROP_VALUE" VARCHAR2(512));
--------------------------------------------------------
--  DDL for Index MAILBOX_PROPERTIES_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "MAILBOX_PROPERTIES_PK" ON "MAILBOX_PROPERTIES" ("PGUID");
--------------------------------------------------------
--  Constraints for Table MAILBOX_PROPERTIES
--------------------------------------------------------

  ALTER TABLE "MAILBOX_PROPERTIES" ADD CONSTRAINT "MAILBOX_PROPERTIES_PK" PRIMARY KEY ("PGUID") ENABLE;
  ALTER TABLE "MAILBOX_PROPERTIES" MODIFY ("MAILBOX_GUID" NOT NULL ENABLE);
  ALTER TABLE "MAILBOX_PROPERTIES" MODIFY ("PGUID" NOT NULL ENABLE);
--------------------------------------------------------
--  Ref Constraints for Table MAILBOX_PROPERTIES
--------------------------------------------------------

  ALTER TABLE "MAILBOX_PROPERTIES" ADD CONSTRAINT "MAILBOX_PROPERTIES_FK1" FOREIGN KEY ("MAILBOX_GUID") REFERENCES "MAILBOXES" ("PGUID") ON DELETE CASCADE ENABLE;

  --------------------------------------------------------
--  File created - Wednesday-September-04-2013   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Table SCHEDULE_PROFILES_REF
--------------------------------------------------------

  CREATE TABLE "SCHEDULE_PROFILES_REF" ("PGUID" CHAR(32), "SCH_PROF_NAME" VARCHAR2(128)) ;

   COMMENT ON COLUMN "SCHEDULE_PROFILES_REF"."PGUID" IS 'primary key';
   COMMENT ON COLUMN "SCHEDULE_PROFILES_REF"."SCH_PROF_NAME" IS 'sample is onceinaday,onceinaweek';
--------------------------------------------------------
--  DDL for Index SCHEDULE_PROFILES_REF_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "SCHEDULE_PROFILES_REF_PK" ON "SCHEDULE_PROFILES_REF" ("PGUID");
--------------------------------------------------------
--  Constraints for Table SCHEDULE_PROFILES_REF
--------------------------------------------------------

  ALTER TABLE "SCHEDULE_PROFILES_REF" ADD CONSTRAINT "SCHEDULE_PROFILES_REF_PK" PRIMARY KEY ("PGUID") ENABLE;
  ALTER TABLE "SCHEDULE_PROFILES_REF" MODIFY ("SCH_PROF_NAME" NOT NULL ENABLE);
  ALTER TABLE "SCHEDULE_PROFILES_REF" MODIFY ("PGUID" NOT NULL ENABLE);

  --------------------------------------------------------
--  File created - Wednesday-September-04-2013   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Table MAILBOX_SCHED_PROFILES
--------------------------------------------------------

  CREATE TABLE "MAILBOX_SCHED_PROFILES" ("PGUID" CHAR(32), "MAILBOX_PROFILE_GUID" CHAR(32), "SCHEDULE_PROFILES_REF_GUID" CHAR(32), "MBX_PROFILE_STATUS" VARCHAR2(128));
--------------------------------------------------------
--  DDL for Index MAILBOX_SCHED_PROFILES_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "MAILBOX_SCHED_PROFILES_PK" ON "MAILBOX_SCHED_PROFILES" ("PGUID");
--------------------------------------------------------
--  Constraints for Table MAILBOX_SCHED_PROFILES
--------------------------------------------------------

  ALTER TABLE "MAILBOX_SCHED_PROFILES" ADD CONSTRAINT "MAILBOX_SCHED_PROFILES_PK" PRIMARY KEY ("PGUID") ENABLE;
  ALTER TABLE "MAILBOX_SCHED_PROFILES" MODIFY ("MBX_PROFILE_STATUS" NOT NULL ENABLE);
  ALTER TABLE "MAILBOX_SCHED_PROFILES" MODIFY ("SCHEDULE_PROFILES_REF_GUID" NOT NULL ENABLE);
  ALTER TABLE "MAILBOX_SCHED_PROFILES" MODIFY ("MAILBOX_PROFILE_GUID" NOT NULL ENABLE);
  ALTER TABLE "MAILBOX_SCHED_PROFILES" MODIFY ("PGUID" NOT NULL ENABLE);
--------------------------------------------------------
--  Ref Constraints for Table MAILBOX_SCHED_PROFILES
--------------------------------------------------------

  ALTER TABLE "MAILBOX_SCHED_PROFILES" ADD CONSTRAINT "MAILBOX_SCHED_PROFILES_MA_FK1" FOREIGN KEY ("MAILBOX_PROFILE_GUID") REFERENCES "MAILBOXES" ("PGUID") ON DELETE CASCADE ENABLE;
  ALTER TABLE "MAILBOX_SCHED_PROFILES" ADD CONSTRAINT "MAILBOX_SCHED_PROFILES_SC_FK1" FOREIGN KEY ("SCHEDULE_PROFILES_REF_GUID") REFERENCES "SCHEDULE_PROFILES_REF" ("PGUID") ENABLE;

  --------------------------------------------------------
--  File created - Wednesday-September-04-2013   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Table PROCESSORS
--------------------------------------------------------

  CREATE TABLE "PROCESSORS" ("PGUID" CHAR(32), "MAILBOX_SCHED_PROFILES_GUID" CHAR(32), "PROCSR_DESC" VARCHAR2(512), "PROCSR_TYPE" VARCHAR2(128), "PROCSR_PROPERTIES" VARCHAR2(2048), "PROCSR_STATUS" VARCHAR2(128), "JAVA_SCRIPT_URI" VARCHAR2(512)) ;

   COMMENT ON COLUMN "PROCESSORS"."MAILBOX_SCHED_PROFILES_GUID" IS 'ign key refrence to  MAILBOX_SCHED_PROFILES';
   COMMENT ON COLUMN "PROCESSORS"."PROCSR_DESC" IS 'processor description';
   COMMENT ON COLUMN "PROCESSORS"."PROCSR_TYPE" IS 'discriminator column';
   COMMENT ON COLUMN "PROCESSORS"."PROCSR_PROPERTIES" IS 'JSON ';
   COMMENT ON COLUMN "PROCESSORS"."JAVA_SCRIPT_URI" IS 'refrence to filesystem, js pulled from git.';
--------------------------------------------------------
--  DDL for Index PROCESSORS_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "PROCESSORS_PK" ON "PROCESSORS" ("PGUID");
--------------------------------------------------------
--  Constraints for Table PROCESSORS
--------------------------------------------------------

  ALTER TABLE "PROCESSORS" ADD CONSTRAINT "PROCESSORS_PK" PRIMARY KEY ("PGUID") ENABLE;
  ALTER TABLE "PROCESSORS" MODIFY ("PROCSR_STATUS" NOT NULL ENABLE);
  ALTER TABLE "PROCESSORS" MODIFY ("PROCSR_TYPE" NOT NULL ENABLE);
  ALTER TABLE "PROCESSORS" MODIFY ("MAILBOX_SCHED_PROFILES_GUID" NOT NULL ENABLE);
  ALTER TABLE "PROCESSORS" MODIFY ("PGUID" NOT NULL ENABLE);
--------------------------------------------------------
--  Ref Constraints for Table PROCESSORS
--------------------------------------------------------

  ALTER TABLE "PROCESSORS" ADD CONSTRAINT "PROCESSORS_FK1" FOREIGN KEY ("MAILBOX_SCHED_PROFILES_GUID") REFERENCES "MAILBOX_SCHED_PROFILES" ("PGUID") ON DELETE CASCADE ENABLE;

  --------------------------------------------------------
--  File created - Tuesday-September-10-2013   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Table PROCESSOR_PROPERTIES
--------------------------------------------------------

  CREATE TABLE "PROCESSOR_PROPERTIES" ("PGUID" CHAR(32), "PROCESSORS_GUID" CHAR(32), "PROCSR_PROP_NAME" VARCHAR2(128), "PROCSR_PROP_VALUE" VARCHAR2(512));
--------------------------------------------------------
--  DDL for Index PROCESSOR_PROPERTIES_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "PROCESSOR_PROPERTIES_PK" ON "PROCESSOR_PROPERTIES" ("PGUID");
--------------------------------------------------------
--  Constraints for Table PROCESSOR_PROPERTIES
--------------------------------------------------------

  ALTER TABLE "PROCESSOR_PROPERTIES" ADD CONSTRAINT "PROCESSOR_PROPERTIES_PK" PRIMARY KEY ("PGUID") ENABLE;
 
  ALTER TABLE "PROCESSOR_PROPERTIES" MODIFY ("PGUID" NOT NULL ENABLE);
 
  ALTER TABLE "PROCESSOR_PROPERTIES" MODIFY ("PROCESSORS_GUID" NOT NULL ENABLE);
--------------------------------------------------------
--  Ref Constraints for Table PROCESSOR_PROPERTIES
--------------------------------------------------------

  ALTER TABLE "PROCESSOR_PROPERTIES" ADD CONSTRAINT "PROCESSOR_PROPERTIES_FK1" FOREIGN KEY ("PROCESSORS_GUID") REFERENCES "PROCESSORS" ("PGUID") ON DELETE CASCADE ENABLE;

  
  --------------------------------------------------------
--  File created - Wednesday-September-04-2013   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Table FOLDERS
--------------------------------------------------------

  CREATE TABLE "FOLDERS" ("PGUID" CHAR(32), "PROCESSORS_GUID" CHAR(32), "FLDR_TYPE" VARCHAR2(50), "FLDR_URI" VARCHAR2(50), "FLDR_DESC" VARCHAR2(250)) ;

   COMMENT ON COLUMN "FOLDERS"."PGUID" IS 'primary key';
   COMMENT ON COLUMN "FOLDERS"."PROCESSORS_GUID" IS 'foreign key refrence to PROCESSORS';
--------------------------------------------------------
--  DDL for Index FOLDERS_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "FOLDERS_PK" ON "FOLDERS" ("PGUID");
--------------------------------------------------------
--  Constraints for Table FOLDERS
--------------------------------------------------------

  ALTER TABLE "FOLDERS" ADD CONSTRAINT "FOLDERS_PK" PRIMARY KEY ("PGUID") ENABLE;
  ALTER TABLE "FOLDERS" MODIFY ("FLDR_TYPE" NOT NULL ENABLE);
  ALTER TABLE "FOLDERS" MODIFY ("PROCESSORS_GUID" NOT NULL ENABLE);
  ALTER TABLE "FOLDERS" MODIFY ("PGUID" NOT NULL ENABLE);
--------------------------------------------------------
--  Ref Constraints for Table FOLDERS
--------------------------------------------------------

  ALTER TABLE "FOLDERS" ADD CONSTRAINT "FOLDERS_FK1" FOREIGN KEY ("PROCESSORS_GUID") REFERENCES "PROCESSORS" ("PGUID") ON DELETE CASCADE ENABLE;

  
  --------------------------------------------------------
--  File created - Wednesday-September-04-2013   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Table CREDENTIALS
--------------------------------------------------------

  CREATE TABLE "CREDENTIALS" ("PGUID" CHAR(32), "PROCESSORS_GUID" CHAR(32), "CREDS_TYPE" VARCHAR2(128), "CREDS_URI" VARCHAR2(128), "CREDS_USERNAME" VARCHAR2(128), "CREDS_PASSWORD" VARCHAR2(128), "CREDS_IDP_TYPE" VARCHAR2(128), "CREDS_IDP_URI" VARCHAR2(128)); 

   COMMENT ON COLUMN "CREDENTIALS"."PROCESSORS_GUID" IS 'foreign key refrence to PROCESSORS';
--------------------------------------------------------
--  DDL for Index CREDENTIALS_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "CREDENTIALS_PK" ON "CREDENTIALS" ("PGUID");
--------------------------------------------------------
--  Constraints for Table CREDENTIALS
--------------------------------------------------------

  ALTER TABLE "CREDENTIALS" ADD CONSTRAINT "CREDENTIALS_PK" PRIMARY KEY ("PGUID") ENABLE;
  ALTER TABLE "CREDENTIALS" MODIFY ("CREDS_TYPE" NOT NULL ENABLE);
  ALTER TABLE "CREDENTIALS" MODIFY ("PROCESSORS_GUID" NOT NULL ENABLE);
  ALTER TABLE "CREDENTIALS" MODIFY ("PGUID" NOT NULL ENABLE);
--------------------------------------------------------
--  Ref Constraints for Table CREDENTIALS
--------------------------------------------------------

  ALTER TABLE "CREDENTIALS" ADD CONSTRAINT "CREDENTIALS_FK1" FOREIGN KEY ("PROCESSORS_GUID") REFERENCES "PROCESSORS" ("PGUID") ON DELETE CASCADE ENABLE;
