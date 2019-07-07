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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.rtdm.dao.InboundFileDAO;
import com.liaison.mailbox.rtdm.dao.InboundFileDAOBase;
import com.liaison.mailbox.rtdm.model.InboundFile;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.InboundFileService;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Test class for inbound file service
 *
 */
public class InboundFileServiceTest extends BaseServiceTest {

    private static final String DIRECTORY_PATH = "/data/sftp/testuser/inbox/folder1/folder2/folder3/folder4";
    private static final String FILE_PATH = "/data/sftp/testuser/inbox/";

    private static final String FOLDER_URI_ONE = "/data/sftp/testuser/inbox/folder1/folder2";
    private static final String FOLDER_URI_TWO = "/data/sftp/testuser/inbox/folder1/folder2/folder3";
    private static final String FOLDER_URI_THREE = "/data/sftp/testuser/inbox/folder1";

    private static final String FOLDER_URI_ONE_INVALID = "/data/sftp/testuser/inbox/folder/folder2";
    private static final String FOLDER_URI_TWO_INVALID = "/data/sftp/testuser/inbox/folder1/folder/folder3";
    private static final String FOLDER_URI_THREE_INVALID = "/data/sftp/testuser/inbox/folder3";
    private static final String AT4 = "at4";

    @Test
    public void testExtractUserPath() {

        InboundFileService service = new InboundFileService();
        String expected = "/data/sftp/testuser/inbox";
        String outputPath = service.extractUserPath(DIRECTORY_PATH);

        Assert.assertNotNull(outputPath);
        Assert.assertEquals(outputPath, expected);
    }

    @Test
    public void testFindMatchingProcessor() {

        InboundFileService service = new InboundFileService();
        List<Processor> processors = getProcessorList();
        Processor processor = service.findMatchingProcessor(processors, DIRECTORY_PATH);

        Assert.assertNotNull(processor);
        Assert.assertNotNull(processor.getFolders());
        Assert.assertEquals(processor.getFolders().stream()
                .findFirst().get().getFldrUri(), FOLDER_URI_TWO);
    }

    @Test
    public void testFindMatchingProcessorInvalid() {

        InboundFileService service = new InboundFileService();
        List<Processor> processors = getProcessorListWithInvalidFolders();
        Processor processor = service.findMatchingProcessor(processors, DIRECTORY_PATH);

        Assert.assertNull(processor);
    }

    @Test(enabled = false)
    public void testPersistInboundFile() {

        Processor processor = new Processor();
        processor.setPguid(MailBoxUtil.getGUID());
        processor.setOriginatingDc(AT4);
        processor.setProcessDc(AT4);

        InboundFileService service = new InboundFileService();
        String fileName = RandomStringUtils.randomAlphabetic(5);
        service.persistInboundFile(fileName, 10, processor, "fs2://test", DIRECTORY_PATH);

        InboundFileDAO dao = new InboundFileDAOBase();
        InboundFile file = dao.findInboundFile(DIRECTORY_PATH, fileName, AT4);

        Assert.assertNotNull(file);
        Assert.assertEquals(file.getFileName(), fileName);
        Assert.assertEquals(file.getFilePath(), DIRECTORY_PATH);
        Assert.assertEquals(file.getProcessDc(), AT4);
        Assert.assertEquals(file.getProcessorId(), processor.getPguid());
        Assert.assertEquals(file.getStatus(), EntityStatus.ACTIVE.name());
    }

    /**
     * Helper method to construct and return the processors list with invalid folders
     * 
     * @return list
     */
    private List<Processor> getProcessorListWithInvalidFolders() {

        Processor processorOne = new Processor();
        Folder folderOne = new Folder();
        folderOne.setFldrUri(FOLDER_URI_ONE_INVALID);
        Set<Folder> folderListOne = new HashSet<Folder>();
        folderListOne.add(folderOne);
        processorOne.setFolders(folderListOne);

        Processor processorTwo = new Processor();
        Folder folderTwo = new Folder();
        folderTwo.setFldrUri(FOLDER_URI_TWO_INVALID);
        Set<Folder> folderListTwo = new HashSet<Folder>();
        folderListTwo.add(folderTwo);
        processorTwo.setFolders(folderListTwo);

        Processor processorThree = new Processor();
        Folder folderThree = new Folder();
        folderThree.setFldrUri(FOLDER_URI_THREE_INVALID);
        Set<Folder> folderListThree = new HashSet<Folder>();
        folderListThree.add(folderThree);
        processorThree.setFolders(folderListThree);

        List<Processor> processors = new ArrayList<>();
        processors.add(processorOne);
        processors.add(processorTwo);
        processors.add(processorThree);
        return processors;
    }

    /**
     * Helper method to construct and return the processors list
     * 
     * @return list
     */
    private List<Processor> getProcessorList() {

        Processor processorOne = new Processor();
        Folder folderOne = new Folder();
        folderOne.setFldrUri(FOLDER_URI_ONE);
        Set<Folder> folderListOne = new HashSet<Folder>();
        folderListOne.add(folderOne);
        processorOne.setFolders(folderListOne);

        Processor processorTwo = new Processor();
        Folder folderTwo = new Folder();
        folderTwo.setFldrUri(FOLDER_URI_TWO);
        Set<Folder> folderListTwo = new HashSet<Folder>();
        folderListTwo.add(folderTwo);
        processorTwo.setFolders(folderListTwo);

        Processor processorThree = new Processor();
        Folder folderThree = new Folder();
        folderThree.setFldrUri(FOLDER_URI_THREE);
        Set<Folder> folderListThree = new HashSet<Folder>();
        folderListThree.add(folderThree);
        processorThree.setFolders(folderListThree);

        List<Processor> processors = new ArrayList<>();
        processors.add(processorOne);
        processors.add(processorTwo);
        processors.add(processorThree);
        return processors;
    }

    @Test
    private void testSimpleCreateInboundFile() {

        InboundFileService service = new InboundFileService();
        String actualPath = service.extractUserPath(FILE_PATH);
        String expectedPath = "/data/sftp/testuser/inbox";

        Assert.assertNotNull(actualPath);
        Assert.assertEquals(actualPath, expectedPath);
    }
}
