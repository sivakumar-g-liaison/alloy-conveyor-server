--
-- Copyright 2010-2013 Axel Fontaine and the many contributors.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
-- See https://github.com/flyway/flyway/blob/master/flyway-core/src/main/resources/com/googlecode/flyway/core/dbsupport/oracle/createMetaDataTable.sql
--
-- NOTE! This script gets modified for use by DEV build with regex replacement. DO NOT modify this script without also verifying
--       that is does not break the "buildFlywayTable" task in service-implementation/build.gradle.
--

set serverout on
set echo on

DECLARE
    v_stmt VARCHAR2(4000);
    v_c      int;
BEGIN
    SELECT count(*) INTO v_c FROM dba_tables WHERE upper(table_name) = upper('SCHEMA_VERSION') AND upper(owner) = upper('GATEWAY_OWNR');
    IF v_c = 0 THEN
        v_stmt := 'CREATE TABLE "GATEWAY_OWNR"."schema_version"
              (
                "version_rank"   NUMBER(*,0) NOT NULL ENABLE,
                "installed_rank" NUMBER(*,0) NOT NULL ENABLE,
                "version"        VARCHAR2(50 BYTE) NOT NULL ENABLE,
                "description"    VARCHAR2(200 BYTE) NOT NULL ENABLE,
                "type"           VARCHAR2(20 BYTE) NOT NULL ENABLE,
                "script"         VARCHAR2(1000 BYTE) NOT NULL ENABLE,
                "checksum"       NUMBER(*,0),
                "installed_by"   VARCHAR2(100 BYTE) NOT NULL ENABLE,
                "installed_on" TIMESTAMP (6) DEFAULT CURRENT_TIMESTAMP NOT NULL ENABLE,
                "execution_time" NUMBER(*,0) NOT NULL ENABLE,
                "success"        NUMBER(1,0) NOT NULL ENABLE
              )
            TABLESPACE "GATEWAY_CONF"
            STORAGE
            (
                INITIAL 64K
                NEXT 64K
                PCTINCREASE 0
            )
            NOPARALLEL';
        EXECUTE IMMEDIATE v_stmt;

        v_stmt := 'ALTER TABLE "GATEWAY_OWNR"."schema_version" ADD CONSTRAINT "schema_version_pk" PRIMARY KEY ("version") USING INDEX TABLESPACE "GATEWAY_CONF"';
        EXECUTE IMMEDIATE v_stmt;

        v_stmt := 'CREATE INDEX "GATEWAY_OWNR"."schema_version_s_idx" ON "GATEWAY_OWNR"."schema_version"("success") TABLESPACE "GATEWAY_CONF"';
        EXECUTE IMMEDIATE v_stmt;
        v_stmt := 'CREATE INDEX "GATEWAY_OWNR"."schema_version_ir_idx" ON "GATEWAY_OWNR"."schema_version" ("installed_rank") TABLESPACE "GATEWAY_CONF"';
        EXECUTE IMMEDIATE v_stmt;
        v_stmt := 'CREATE INDEX "GATEWAY_OWNR"."schema_version_vr_idx" ON "GATEWAY_OWNR"."schema_version" ("version_rank") TABLESPACE "GATEWAY_CONF"';
        EXECUTE IMMEDIATE v_stmt;
    END IF;
END;
/

EXIT;