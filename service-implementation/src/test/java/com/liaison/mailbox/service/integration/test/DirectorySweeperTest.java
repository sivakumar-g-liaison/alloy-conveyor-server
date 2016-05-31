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

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.UUIDGen;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.Sweeper;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.processor.DirectorySweeper;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SweeperPropertiesDTO;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;
import org.codehaus.jettison.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author OFS
 *
 */
@Test
public class DirectorySweeperTest extends BaseServiceTest {

	private String inbox;
	private String fileRenameFormat;
	private String includeFiles;
	private String excludeFiles;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public void setUp() throws Exception {

		inbox = System.getProperty("java.io.tmpdir") + File.separator + "INBOX1";
		Files.deleteIfExists(Paths.get(inbox));
		Files.createDirectory(Paths.get(inbox));
		fileRenameFormat = ".tested";
		includeFiles = ".txt,.test";
		excludeFiles = ".js";
	}

    @AfterClass
    public void tearDown() throws Exception {
        Files.deleteIfExists(Paths.get(inbox));
    }

	/**
	 * Method to test Sweeper with valid data.
	 *
	 * @throws Exception
	 */
    @Test(enabled = true)
	public void testSweeper() throws Exception {

		String name = "Purchase.txt";
		Path target = Paths.get(inbox + File.separator + name);
		Files.createFile(target);
        Thread.sleep(2000);

        try {

            List<WorkTicket> workTickets = sweep();
            Assert.assertEquals(workTickets.get(0).getFileName(), name);
        } finally {
            Files.deleteIfExists(target);
        }

	}

    /**
	 * Method to test Sweeper without file permission.
	 *
	 * @throws Exception
	 */
    @Test(enabled = true)
	public void testSweeperWithoutFilePermission() throws Exception {

		String name = "PurchaseError.txt";
		Path target = Paths.get(inbox + File.separator + name);
		Files.createFile(target);
		target.toFile().setReadable(false);
		target.toFile().setWritable(false);
        target.toFile().setReadOnly();
        Thread.sleep(5000);

        try {
            List<WorkTicket> workTickets = sweep();
            Assert.assertEquals(workTickets.get(0).getFileName(), name);
        } finally {
            target.toFile().setReadable(true);
            target.toFile().setWritable(true);
            Files.delete(target);
        }
	}

	/**
	 * Method to test Sweeper with space in file name.
	 *
	 * @throws Exception
	 */
    @Test(enabled = true)
	public void testSweeperWithSpaceInFileName() throws Exception {

		String name = "Purchase Error.txt";
		Path target = Paths.get(inbox + File.separator + name);
		Files.createFile(target);
        Thread.sleep(5000);

        try {
            List<WorkTicket> workTickets = sweep();
            Assert.assertEquals(workTickets.get(0).getFileName(), name);
        } finally {
            Files.delete(target);
        }
	}

    private List<WorkTicket> sweep() throws Exception {

        Processor processor = new Sweeper();
        processor.setProcsrName("Processor");
        processor.setProcsrStatus("ACTIVE");
        processor.setProcsrProperties(ServiceUtils.readFileFromClassPath("requests/processor/sweeper.json"));

        MailBox mailbox = new MailBox();
        mailbox.setMbxName("Test");
        mailbox.setPguid(UUIDGen.getCustomUUID());
        processor.setMailbox(mailbox);

        Set<Folder> folders = new HashSet<Folder>();
        Folder folder = new Folder();
        folder.setFldrType("PAYLOAD_LOCATION");
        folder.setFldrUri(inbox);
        folders.add(folder);
        processor.setFolders(folders);

        DirectorySweeper sweeper = new DirectorySweeper(processor);
        SweeperPropertiesDTO staticProp = (SweeperPropertiesDTO) ProcessorPropertyJsonMapper.getProcessorBasedStaticPropsFromJson
                (processor.getProcsrProperties(), processor);
        List<WorkTicket> workTickets = sweeper.sweepDirectory(inbox, staticProp);

        System.out.println(new JSONObject(JAXBUtility.marshalToJSON(workTickets.get(0))).toString(2));
        return workTickets;
    }

}