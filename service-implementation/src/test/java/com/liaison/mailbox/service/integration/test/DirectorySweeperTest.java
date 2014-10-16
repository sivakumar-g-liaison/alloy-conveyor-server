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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.codehaus.jettison.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.framework.util.ServiceUtils;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.processor.DirectorySweeperProcessor;
import com.liaison.mailbox.service.dto.directorysweeper.FileAttributesDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;

/**
 * @author OFS
 * 
 */
public class DirectorySweeperTest extends BaseServiceTest {

	private String inbox;
	private String fileRenameFormat;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		inbox = System.getProperty("java.io.tmpdir") + File.separator + "INBOX1";
		Files.deleteIfExists(Paths.get(inbox));
		Files.createDirectory(Paths.get(inbox));
		fileRenameFormat = ".tested";
	}
    
	/**
	 * Method to test Sweeper with valid data.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws MailBoxServicesException
	 * @throws FS2Exception
	 * @throws JAXBException
	 */
	@Test
	public void testSweeper() throws IOException, URISyntaxException, MailBoxServicesException, FS2Exception, JAXBException {

		String name = "Purchase.txt";
		Path target = Paths.get(inbox + File.separator + name);
		Files.createFile(target);

		Processor processor = new Processor();
		processor.setProcsrName("Processor");
		processor.setProcsrStatus("ACTIVE");
		String remoteProperties = ServiceUtils.readFileFromClassPath("requests/processor/remoteprocessor.json");
		processor.setProcsrProperties(remoteProperties);

		List<Folder> folders = new ArrayList<Folder>();
		Folder folder = new Folder();
		folder.setFldrType("PAYLOAD_LOCATION");
		folder.setFldrUri(inbox);
		folders.add(folder);
		processor.setFolders(folders);

		DirectorySweeperProcessor downloader = new DirectorySweeperProcessor(processor);
		List<FileAttributesDTO> path = downloader.sweepDirectory(inbox, false, false, fileRenameFormat, 0);
		Files.delete(target);
		Assert.assertEquals(1, path.size());
		Assert.assertEquals(name, path.get(0).getFilename());
	}
    
	/**
	 * Method to test Sweeper without file permission.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws MailBoxServicesException
	 * @throws FS2Exception
	 * @throws Exception
	 */
	@Test
	public void testSweeperWithoutFilePermission() throws IOException, URISyntaxException, MailBoxServicesException,
			FS2Exception, Exception {

		String name = "PurchaseError.txt";
		Path target = Paths.get(inbox + File.separator + name);
		Files.createFile(target);
		target.toFile().setReadable(false);
		target.toFile().setWritable(false);

		Processor processor = new Processor();
		processor.setProcsrName("Processor");
		processor.setProcsrStatus("ACTIVE");
		String remoteProperties = ServiceUtils.readFileFromClassPath("requests/processor/remoteprocessor.json");
		processor.setProcsrProperties(remoteProperties);

		List<Folder> folders = new ArrayList<Folder>();
		Folder folder = new Folder();
		folder.setFldrType("PAYLOAD_LOCATION");
		folder.setFldrUri(inbox);
		folders.add(folder);
		processor.setFolders(folders);

		DirectorySweeperProcessor downloader = new DirectorySweeperProcessor(processor);
		List<FileAttributesDTO> path = downloader.sweepDirectory(inbox, false, false, fileRenameFormat, 0);
		target.toFile().setReadable(true);
		target.toFile().setWritable(true);
		Files.delete(target);
		Assert.assertEquals(1, path.size());
		Assert.assertEquals(name, path.get(0).getFilename());
	}
    
	/**
	 * Method to test Sweeper with space in file name.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws MailBoxServicesException
	 * @throws FS2Exception
	 * @throws JAXBException
	 */
	@Test
	public void testSweeperWithSpaceInFileName() throws IOException, URISyntaxException, MailBoxServicesException, FS2Exception,
			JAXBException {

		String name = "Purchase Error.txt";
		Path target = Paths.get(inbox + File.separator + name);
		Files.createFile(target);

		Processor processor = new Processor();
		processor.setProcsrName("Processor");
		processor.setProcsrStatus("ACTIVE");
		String remoteProperties = ServiceUtils.readFileFromClassPath("requests/processor/remoteprocessor.json");
		processor.setProcsrProperties(remoteProperties);

		List<Folder> folders = new ArrayList<Folder>();
		Folder folder = new Folder();
		folder.setFldrType("PAYLOAD_LOCATION");
		folder.setFldrUri(inbox);
		folders.add(folder);
		processor.setFolders(folders);

		DirectorySweeperProcessor downloader = new DirectorySweeperProcessor(processor);
		List<FileAttributesDTO> path = downloader.sweepDirectory(inbox, false, false, fileRenameFormat, 0);
		Files.delete(target);
		Assert.assertEquals(1, path.size());
		Assert.assertEquals(name, path.get(0).getFilename());
	}
    
	/**
	 * Method to test mark as sweeped.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws MailBoxServicesException
	 * @throws FS2Exception
	 * @throws JSONException
	 * @throws JAXBException
	 */
	@Test
	public void testMarkAsSweeped() throws IOException, URISyntaxException, MailBoxServicesException, FS2Exception,
			JSONException, JAXBException {

		String name = "Purchase Error.txt";
		Path target = Paths.get(inbox + File.separator + name);
		Files.createFile(target);

		Processor processor = new Processor();
		processor.setProcsrName("Processor");
		processor.setProcsrStatus("ACTIVE");
		String remoteProperties = ServiceUtils.readFileFromClassPath("requests/processor/remoteprocessor.json");
		processor.setProcsrProperties(remoteProperties);

		List<Folder> folders = new ArrayList<Folder>();
		Folder folder = new Folder();
		folder.setFldrType("PAYLOAD_LOCATION");
		folder.setFldrUri(inbox);
		folders.add(folder);
		processor.setFolders(folders);

		DirectorySweeperProcessor downloader = new DirectorySweeperProcessor(processor);
		List<FileAttributesDTO> files = downloader.sweepDirectory(inbox, false, false, fileRenameFormat, 0);
		Files.delete(target);
		Assert.assertEquals(1, files.size());
		Assert.assertEquals(name, files.get(0).getFilename());

	}

	@AfterMethod
	public void tearDown() throws Exception {
		Files.deleteIfExists(Paths.get(inbox));
	}

}