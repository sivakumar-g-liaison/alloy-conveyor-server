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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.fs2.api.FS2Exception;
import com.liaison.mailbox.jpa.model.Credential;
import com.liaison.mailbox.jpa.model.Folder;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.processor.AbstractRemoteProcessor;
import com.liaison.mailbox.service.core.processor.HttpRemoteDownloader;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;

/**
 * @author praveenu
 * 
 */
public class RemoteProcessorTest extends BaseServiceTest {

	private static String responseLocation;

	@BeforeClass
	public static void setUp() throws Exception {
		responseLocation = System.getProperty("java.io.tmpdir")
				+ File.separator + "sample";
		Files.deleteIfExists(Paths.get(responseLocation));
		Files.createDirectory(Paths.get(responseLocation));
	}

	@Test
	public void testRemoteDownloader() throws JsonParseException,
			JsonMappingException, LiaisonException, JSONException,
			JAXBException, IOException, com.google.gson.JsonParseException,
			URISyntaxException, MailBoxServicesException,
			SymmetricAlgorithmException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, com.liaison.commons.exception.LiaisonException {

		Processor processor = new Processor();
		processor.setProcsrName("Processor");
		processor.setProcsrStatus("ACTIVE");
		processor.setProcsrProtocol("HTTP");
		String remoteProperties = ServiceUtils
				.readFileFromClassPath("requests/processor/remoteprocessor.json");
		processor.setProcsrProperties(remoteProperties);

		List<Folder> folders = new ArrayList<Folder>();
		Folder folder = new Folder();
		folder.setFldrType("RESPONSE_LOCATION");
		folder.setFldrUri(responseLocation);
		folders.add(folder);
		processor.setFolders(folders);

		AbstractRemoteProcessor downloader = new HttpRemoteDownloader(processor);
		HTTPRequest request = (HTTPRequest) downloader
				.getClientWithInjectedConfiguration();

		Assert.assertEquals(200, request.execute().getStatusCode());
	}

	@Test
	public void testCredentialUtility() throws SymmetricAlgorithmException,
			MailBoxConfigurationServicesException {

		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setCredentialType("KEY_STORE");
		credentialDTO.setCredentialURI("\test");
		credentialDTO.setIdpType("test");
		credentialDTO.setIdpURI("\test");
		credentialDTO.setPassword("123456");
		credentialDTO.setUserId("test");

		Credential credential = new Credential();
		credentialDTO.copyToEntity(credential);

		CredentialDTO resultDTO = new CredentialDTO();
		resultDTO.copyFromEntity(credential);

		Assert.assertEquals(credentialDTO.getCredentialType(),
				resultDTO.getCredentialType());
		Assert.assertEquals(credentialDTO.getCredentialURI(),
				resultDTO.getCredentialURI());
		Assert.assertEquals(credentialDTO.getIdpType(), resultDTO.getIdpType());
		Assert.assertEquals(credentialDTO.getIdpURI(), resultDTO.getIdpURI());
		Assert.assertEquals(credentialDTO.getPassword(),
				resultDTO.getPassword());
		Assert.assertEquals(credentialDTO.getUserId(), resultDTO.getUserId());

	}

	@Test(expected = MailBoxConfigurationServicesException.class)
	public void testCredentialUtility_InvalidCredentialType_ShoudFail()
			throws SymmetricAlgorithmException,
			MailBoxConfigurationServicesException {

		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setCredentialType("123456");

		Credential credential = new Credential();
		credentialDTO.copyToEntity(credential);

	}

	@Test
	public void testWriteResponseToMailBox() throws JsonParseException,
			JsonMappingException, LiaisonException, JSONException,
			JAXBException, IOException, com.google.gson.JsonParseException,
			URISyntaxException, MailBoxServicesException, FS2Exception {

		Processor processor = new Processor();
		processor.setProcsrName("TestProcessor");
		processor.setProcsrStatus("ACTIVE");
		String remoteProperties = ServiceUtils
				.readFileFromClassPath("requests/processor/remoteprocessor.json");
		processor.setProcsrProperties(remoteProperties);

		List<Folder> folders = new ArrayList<Folder>();
		Folder folder = new Folder();
		folder.setFldrType("response_location");// Entity
		folder.setFldrUri(responseLocation);
		folders.add(folder);
		processor.setFolders(folders);

		String test = "Testing write response to mailbox.";
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		stream.write(test.getBytes());

		AbstractRemoteProcessor downloader = new HttpRemoteDownloader(processor);
		downloader.writeResponseToMailBox(stream);
	}

	@Test
	public void testWriteResponseToMailBox_WithoutProcessorName()
			throws JsonParseException, JsonMappingException, LiaisonException,
			JSONException, JAXBException, IOException,
			com.google.gson.JsonParseException, URISyntaxException,
			MailBoxServicesException, FS2Exception {

		Processor processor = new Processor();
		processor.setProcsrStatus("ACTIVE");
		String remoteProperties = ServiceUtils
				.readFileFromClassPath("requests/processor/remoteprocessor.json");
		processor.setProcsrProperties(remoteProperties);

		List<Folder> folders = new ArrayList<Folder>();
		Folder folder = new Folder();
		folder.setFldrType("response_location");// Entity
		folder.setFldrUri(responseLocation);
		folders.add(folder);
		processor.setFolders(folders);

		String test = "Testing write response to mailbox.";
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		stream.write(test.getBytes());

		AbstractRemoteProcessor downloader = new HttpRemoteDownloader(processor);
		downloader.writeResponseToMailBox(stream);
	}

	@Test
	public void testWriteFileResponseToMailBox() throws JsonParseException,
			JsonMappingException, LiaisonException, JSONException,
			JAXBException, IOException, com.google.gson.JsonParseException,
			URISyntaxException, MailBoxServicesException, FS2Exception {

		Processor processor = new Processor();
		processor.setProcsrName("TestProcessor");
		processor.setProcsrStatus("ACTIVE");
		String remoteProperties = ServiceUtils
				.readFileFromClassPath("requests/processor/remoteprocessor.json");
		processor.setProcsrProperties(remoteProperties);

		List<Folder> folders = new ArrayList<Folder>();
		Folder folder = new Folder();
		folder.setFldrType("response_location");// Entity
		folder.setFldrUri(responseLocation);
		folders.add(folder);
		processor.setFolders(folders);

		String test = "Testing write file response to mailbox.";
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		stream.write(test.getBytes());

		AbstractRemoteProcessor downloader = new HttpRemoteDownloader(processor);
		downloader.writeFileResponseToMailBox(stream,
				"test" + System.nanoTime() + ".txt");
	}

	@Test
	public void testWriteFileResponseToMailBox_EveryTimeNewDirectory()
			throws JsonParseException, JsonMappingException, LiaisonException,
			JSONException, JAXBException, IOException,
			com.google.gson.JsonParseException, URISyntaxException,
			MailBoxServicesException, FS2Exception {

		Processor processor = new Processor();
		processor.setProcsrName("TestProcessor");
		processor.setProcsrStatus("ACTIVE");
		String remoteProperties = ServiceUtils
				.readFileFromClassPath("requests/processor/remoteprocessor.json");
		processor.setProcsrProperties(remoteProperties);

		List<Folder> folders = new ArrayList<Folder>();
		Folder folder = new Folder();
		folder.setFldrType("response_location");// Entity
		folder.setFldrUri(responseLocation + File.separator + System.nanoTime());
		folders.add(folder);
		processor.setFolders(folders);

		String test = "Testing write file response to mailbox.";
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		stream.write(test.getBytes());

		AbstractRemoteProcessor downloader = new HttpRemoteDownloader(processor);
		downloader.writeFileResponseToMailBox(stream,
				"test" + System.nanoTime() + ".txt");
	}

	@Test
	public void testWriteFileResponseToMailBox_WithFileNameWithSpaces()
			throws IOException, URISyntaxException, FS2Exception,
			MailBoxServicesException {

		Processor processor = new Processor();
		processor.setProcsrName("TestProcessor");
		processor.setProcsrStatus("ACTIVE");
		String remoteProperties = ServiceUtils
				.readFileFromClassPath("requests/processor/remoteprocessor.json");
		processor.setProcsrProperties(remoteProperties);

		List<Folder> folders = new ArrayList<Folder>();
		Folder folder = new Folder();
		folder.setFldrType("response_location");// Entity
		folder.setFldrUri(responseLocation);
		folders.add(folder);
		processor.setFolders(folders);

		String test = "Testing write file response to mailbox.";
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		stream.write(test.getBytes());

		AbstractRemoteProcessor downloader = new HttpRemoteDownloader(processor);
		downloader.writeFileResponseToMailBox(stream,
				"test file name" + System.nanoTime() + ".txt");

	}

	@AfterClass
	public static void tearDown() throws Exception {
		deleteFolder(Paths.get(responseLocation));
	}

	public static void deleteFolder(Path folder) {

		File[] files = folder.toFile().listFiles();
		if (files != null) { // some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f.toPath());
				} else {
					f.delete();
				}
			}
		}
		folder.toFile().delete();
	}
}
