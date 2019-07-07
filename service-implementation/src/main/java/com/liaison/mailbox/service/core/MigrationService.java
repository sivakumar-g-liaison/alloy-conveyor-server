/**
 * Copyright 2018 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core;

import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Migration service to update datacenter details
 */
public class MigrationService {

    public void migrate(int option) {

        if (3 == option) {
            new StagedFileDAOBase().updateStagedFileProcessDC(MailBoxUtil.DATACENTER_NAME);
        } else {
            new ProcessorConfigurationDAOBase().updateDatacenters(option);
        }
    }
}
