/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.services.unit.test;

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
public class MailBoxSendEmailTest extends BaseServiceTest{

    private String serviceInstanceId = "5D9C3B487184426E9F9629EFEE7C5913";
    
    private String aclManifest = "H4sIAAAAAAAAAO1YbW/aMBD+K5U/TqRNokACn5aW0EUroaLRJrWqIpMckVfHjpwQlVb973NeKKBWXcdWEVV8QbLvfPfwnO8ewyMCVgDlKaDBIwoF4ByiofxAA6SrWldR+4qq+7o6MPoD1Tg2e8Y16qCY8hmmboQGbEFpB6VYAMs31oKHkGXrjUUGolyhguEYC/wLs6+UYJJxdhxBgWqPERFZ7uEENo9d4O29BuTp0k5TSkKcE85q25NMTHE+5yJBg5vH50V9Gp3rMo3gFE5xBpEdlgjPOMvlVuUe8QQT9uwcDJ0fgev5wWR6Lg/WVn9ZMoXklu2517bvTrxnm8tyEAzTlxHGE8/97kyb9DKZNNrDseuh25IrUhAKMVQgBGR8IcIywJfSv1k2eaeQ5dOVx9pafgu4z6WD5KsgISgzwe9ASJcUREKyrOJIhi8wXaxir01N9J/fXN+5cK989HT71PlnLGXxEizrDYm8HPvFEuuyQnTG7xuC9ovmDpZKW5iJcI4lmDTd93WpZwqUTSRbIoOaoD2DSihNlZCSvZepAlJe3v/JyEnI2ZzEJ7Hgi3QHUCAEFwrjOZmvBvHHYGNypGZ7B1hB3FKJIST8aCJizMhDFV7bRSlGPVMdmYap2n1dNcyRZmjGmTOybN1ynJ7R3dCNAkDgoFq9JR3bo7ehciEqiLvw+N5RXvJxuTa9TWjn/dxvuF4600A3Jmd+oKraDiUb1zoQlDWIxepO/HXNXg+zKtMr1sCOEsI+teC3SdbaJfhtegp9ZsF/2e6nE8f1dnq/1ydfebs3ho95wLfr3h6my6Gf29XPrh/4Ekgof8LrvaMrYBGIrRfYTmJOGpEec0bkRVdCs6tapmYq4XxmKIY615SZZYZKV7Ow1Y90sGawMRH+COog/gfxbwUzn3tYyP5iMjFEV3Xdh5CWvcjCZfU/n2x8mfqm9PwNJYKk5vgUAAA=";

    private final static String mailboxMailId = "vnagarajan@liaison.com";
    
    private final static String scriptMailId = "kdevarajan@liaison.com";
    
    private final static String subject = "Failure";
    
    private final static String emailBody = "Sample SFTP Downloader";
    
    private final static String type = "HTML";
    
    /**
     * @throws Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
        System.setProperty("com.liaison.secure.properties.path", "invalid");
        System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
        System.setProperty("archaius.deployment.environment", "test");
        InitInitialDualDBContext.init();
    }
    
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
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest, mbxDTO.getModifiedBy());
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
