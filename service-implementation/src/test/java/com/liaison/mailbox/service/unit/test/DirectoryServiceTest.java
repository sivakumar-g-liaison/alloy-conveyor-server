/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.unit.test;

import java.io.IOException;

import org.testng.annotations.Test;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.directory.DirectoryService;
import com.liaison.usermanagement.enums.DirectoryOperationTypes;
import com.liaison.usermanagement.service.dto.DirectoryMessageDTO;

/**
 * Unit test cases for directory service 
 * @author OFS
 *
 */
public class DirectoryServiceTest extends BaseServiceTest {

    /**
     * Method to test directory creation.
     * 
     * @throws IOException
     */
    @Test
    public void directoryCreationTest() throws IOException {
        DirectoryService service =  new DirectoryService();
        DirectoryMessageDTO messageDTO =  new DirectoryMessageDTO();
        messageDTO.setGatewayType(MailBoxConstants.FTP);
        messageDTO.setOperationType(DirectoryOperationTypes.CREATE.value());
        messageDTO.setUserName("test");
        
        service.executeDirectoryOperation(messageDTO);
    }
    
    /**
     * Method to test directory deletion.
     * 
     * @throws IOException
     */
    @Test
    public void direcoryDeletionTest() throws IOException {
        DirectoryService service =  new DirectoryService();
        DirectoryMessageDTO messageDTO =  new DirectoryMessageDTO();
        messageDTO.setGatewayType(MailBoxConstants.FTP);
        messageDTO.setOperationType(DirectoryOperationTypes.DELETE.value());
        messageDTO.setUserName("test");
        
        service.executeDirectoryOperation(messageDTO);
    }
}
