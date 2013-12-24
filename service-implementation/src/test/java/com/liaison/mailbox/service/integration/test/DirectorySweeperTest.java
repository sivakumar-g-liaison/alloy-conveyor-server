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

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.liaison.framework.util.ServiceUtils;
import com.liaison.fs2.api.FS2Exception;
import com.liaison.mailbox.jpa.model.Folder;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.processor.DirectorySweeperProcessor;
import com.liaison.mailbox.service.dto.directorysweeper.FileAttributesDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;

/**
 * @author praveenu
 * 
 */
public class DirectorySweeperTest extends BaseServiceTest {

	private String inbox;
	private String fileRenameFormat;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		inbox = System.getProperty("java.io.tmpdir") + File.separator + "INBOX1";
		Files.deleteIfExists(Paths.get(inbox));
		Files.createDirectory(Paths.get(inbox));
		fileRenameFormat = ".tested";
	}

	@Test
	public void testSweeper() throws IOException, URISyntaxException, MailBoxServicesException, FS2Exception {

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
		List<FileAttributesDTO> path = downloader.sweepDirectory(inbox, false, false, null, fileRenameFormat);
		Files.delete(target);
		Assert.assertEquals(1, path.size());
		Assert.assertEquals(name, path.get(0).getFilename());
	}

	@Test
	public void testSweeperWithoutFilePermission() throws IOException, URISyntaxException, MailBoxServicesException, FS2Exception {

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
		List<FileAttributesDTO> path = downloader.sweepDirectory(inbox, false, false, null, fileRenameFormat);
		target.toFile().setReadable(true);
		target.toFile().setWritable(true);
		Files.delete(target);
		Assert.assertEquals(1, path.size());
		Assert.assertEquals(name, path.get(0).getFilename());
	}

	@Test
	public void testSweeperWithSpaceInFileName() throws IOException, URISyntaxException, MailBoxServicesException, FS2Exception {

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
		List<FileAttributesDTO> path = downloader.sweepDirectory(inbox, false, false, null, fileRenameFormat);
		Files.delete(target);
		Assert.assertEquals(1, path.size());
		Assert.assertEquals(name, path.get(0).getFilename());
	}

	@Test
	public void testMarkAsSweeped() throws IOException, URISyntaxException, MailBoxServicesException, FS2Exception, JSONException {

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
		List<FileAttributesDTO> files = downloader.sweepDirectory(inbox, false, false, null, fileRenameFormat);
		Files.delete(target);
		Assert.assertEquals(1, files.size());
		Assert.assertEquals(name, files.get(0).getFilename());

	}

	@After
	public void tearDown() throws Exception {
		Files.deleteIfExists(Paths.get(inbox));
	}

}