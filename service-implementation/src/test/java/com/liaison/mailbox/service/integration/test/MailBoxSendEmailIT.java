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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.base.test.InitInitialDualDBContext;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.core.processor.AbstractProcessor;
import com.liaison.mailbox.service.core.processor.SFTPRemoteDownloader;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.PropertyDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;

/**
 * Class to verify and test the emails configured in mailbox and scripts.
 * 
 * @author OFS
 */
public class MailBoxSendEmailIT extends BaseServiceTest {

    private final static String mailboxMailId = "vnagarajan@liaison.com";
    
    private final static String scriptMailId = "kdevarajan@liaison.com";
    
    private final static String subject = "Failure";
    
    private final static String emailBody = "Sample SFTP Downloader";
    
    private final static String type = "HTML";
    
    /**
     * Method to verify and test the emails configured in mailbox and scripts.
     * 
     * @throws JAXBException
     * @throws IOException
     */
    @Test
    public void testSendEmail() throws JAXBException, IOException {
        
        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        PropertyDTO property = new PropertyDTO();
        property.setName(MailBoxConstants.MBX_RCVR_PROPERTY);
        property.setValue(mailboxMailId);
        mbxDTO.getProperties().add(property);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        MailBoxConfigurationDAO mailBoxConfigDAO = new MailBoxConfigurationDAOBase();
        MailBox mailBox = mailBoxConfigDAO.find(MailBox.class, response.getMailBox().getGuid());
        
        Processor processor = new Processor();
        processor.setProcsrName("TestProcessor");
        processor.setProcsrStatus("ACTIVE");
        processor.setMailbox(mailBox);
        AbstractProcessor downloader = new SFTPRemoteDownloader(processor);
        
        List<String> scriptMailList = new ArrayList<String>();
        scriptMailList.add(scriptMailId);
        
        //Overwrites the mailID in mailbox
        downloader.sendEmail(scriptMailList, subject, emailBody, type, true);
        Assert.assertNotNull(scriptMailList);
        Assert.assertTrue(scriptMailList.contains(scriptMailId));
        
        //Appends the given mailID with the configured mailID in mailbox
        downloader.sendEmail(scriptMailList, subject, emailBody, type, false);
        Assert.assertNotNull(scriptMailList);
        Assert.assertTrue(scriptMailList.contains(scriptMailId));
        Assert.assertTrue(scriptMailList.contains(mailboxMailId));
        
        scriptMailList.clear();
        //This will never send a mail even for the email address configured in the mailbox
        downloader.sendEmail(scriptMailList, subject, emailBody, type, true);
        Assert.assertNotNull(scriptMailList);
        Assert.assertTrue(scriptMailList.isEmpty());
        
        //Overwrites the mailID in mailbox if exists
        processor.getMailbox().setMailboxProperties(null);
        scriptMailList.add(scriptMailId);
        downloader.sendEmail(scriptMailList, subject, emailBody, type, true);
        Assert.assertNotNull(scriptMailList);
        Assert.assertTrue(scriptMailList.contains(scriptMailId));
        
        //Appends the given mailID with the configured mailID in mailbox if exists
        downloader.sendEmail(scriptMailList, subject, emailBody, type, false);
        Assert.assertNotNull(scriptMailList);
        Assert.assertTrue(scriptMailList.contains(scriptMailId));
        
        //This will never send a mail even mailID exists in mailbox
        scriptMailList.clear();
        downloader.sendEmail(scriptMailList, subject, emailBody, type, true);
        Assert.assertNotNull(scriptMailList);
        Assert.assertTrue(scriptMailList.isEmpty());
        
        //This will never send a mail since no mailID's in script as well as mailbox
        downloader.sendEmail(scriptMailList, subject, emailBody, type, true);
        Assert.assertNotNull(scriptMailList);
        Assert.assertTrue(scriptMailList.isEmpty());        
    }
    
}