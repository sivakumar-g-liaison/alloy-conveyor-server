/**
 * Copyright 2018 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.integration.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.MailboxTenancyKeyService;
import com.liaison.mailbox.service.dto.configuration.response.GetTenancyKeysResponseDTO;

public class MailboxTenancyKeyServiceIT extends BaseServiceTest{
    
    @Test
    public void testGetAllTenancyKeysFromACLManifest() throws Exception {
        // Retrieve all tenancy keys present in acl manifest
        MailboxTenancyKeyService service = new MailboxTenancyKeyService(); 
        GetTenancyKeysResponseDTO serviceResponse = service.getAllTenancyKeysFromACLManifest(aclManifest); 
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus()); 
    }
	
    @Test
    public void testGetAllTenancyKeysWithACLManifestIsNULL() throws Exception {
        // Retrieve all tenancy keys present in acl manifest
        MailboxTenancyKeyService service = new MailboxTenancyKeyService();
        GetTenancyKeysResponseDTO serviceResponse = service.getAllTenancyKeysFromACLManifest(null);
        Assert.assertEquals(FAILURE, serviceResponse.getResponse().getStatus());
    }
}
