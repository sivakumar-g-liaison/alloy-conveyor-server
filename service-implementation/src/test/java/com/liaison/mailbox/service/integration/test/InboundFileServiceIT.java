/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.integration.test;

import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.InboundFileService;
import com.liaison.mailbox.service.dto.dropbox.response.GetInboundFilesResponseDTO;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class which tests the mailbox functional services.
 *
 * @author OFS
 */
public class InboundFileServiceIT extends BaseServiceTest {

    /**
     * Method to test Inbound file.
     */
    @Test
    public void testInboundFile() throws Exception {

        String filterText = null;// "{\"filterText\":[],\"useExternalFilter\":true} ";
        String sortInfo = null;// "{\"fields\":[\"fileName\"],\"directions\":[\"asc\"]";

        // Create Inbound File here

        InboundFileService inboundFileService = new InboundFileService();

        GetInboundFilesResponseDTO serviceResponse = inboundFileService.getInboundFiles(PAGE, PAGE_SIZE, sortInfo,
                filterText);

        // Assertion
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());

    }

}