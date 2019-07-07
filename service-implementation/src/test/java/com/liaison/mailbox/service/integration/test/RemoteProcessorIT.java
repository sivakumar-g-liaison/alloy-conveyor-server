/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.integration.test;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.dtdm.model.Credential;
import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.RemoteDownloader;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.processor.AbstractProcessor;
import com.liaison.mailbox.service.core.processor.HTTPRemoteDownloader;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * @author OFS
 *
 */
public class RemoteProcessorIT extends BaseServiceTest {

    private String responseLocation;

    @BeforeMethod
    public void setUp() throws Exception {
        responseLocation = System.getProperty("java.io.tmpdir")
                + File.separator + "sample";
        Files.deleteIfExists(Paths.get(responseLocation));
        Files.createDirectory(Paths.get(responseLocation));
    }

    /**
     * Method to test remote downloader with valid data.
     *
     * @throws LiaisonException
     */
    @Test
    public void testRemoteDownloader() throws LiaisonException {

        Processor processor = new RemoteDownloader();
        processor.setProcsrName("Processor");
        processor.setProcsrStatus("ACTIVE");
        processor.setProcsrProtocol(Protocol.HTTP.getCode());
        String remoteProperties = ServiceUtils.readFileFromClassPath("requests/processor/remoteprocessor.json");
        processor.setProcsrProperties(remoteProperties);

        Set<Folder> folders = new HashSet<Folder>();
        Folder folder = new Folder();
        folder.setFldrType("RESPONSE_LOCATION");
        folder.setFldrUri(responseLocation);
        folders.add(folder);
        processor.setFolders(folders);

        AbstractProcessor downloader = new HTTPRemoteDownloader(processor);
        HTTPRequest request = (HTTPRequest) downloader.getClient();

        Assert.assertEquals(200, request.execute().getStatusCode());
    }

    /**
     * Method to test credential utility.
     *
     */
    @Test
    public void testCredentialUtility() {

        CredentialDTO credentialDTO = new CredentialDTO();
        credentialDTO.setCredentialType("LOGIN_CREDENTIAL");
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

    /**
     * Method to test credential utility with invalid credential type.
     *
     */
    @Test(expectedExceptions = MailBoxConfigurationServicesException.class)
    public void testCredentialUtility_InvalidCredentialType_ShouldFail(){

        CredentialDTO credentialDTO = new CredentialDTO();
        credentialDTO.setCredentialType("123456");

        Credential credential = new Credential();
        credentialDTO.copyToEntity(credential);

    }

    /**
     * Method to test mailbox response.
     */
    @Test
    public void testWriteResponseToMailBox() throws IOException, URISyntaxException {

        Processor processor = new Processor();
        processor.setProcsrName("TestProcessor");
        processor.setProcsrStatus("ACTIVE");
        String remoteProperties = ServiceUtils
                .readFileFromClassPath("requests/processor/remoteprocessor.json");
        processor.setProcsrProperties(remoteProperties);

        Set<Folder> folders = new HashSet<Folder>();
        Folder folder = new Folder();
        folder.setFldrType("RESPONSE_LOCATION");// Entity
        folder.setFldrUri(responseLocation);
        folders.add(folder);
        processor.setFolders(folders);

        String test = "Testing write response to mailbox.";
        ByteArrayOutputStream stream = new ByteArrayOutputStream(4096);
        stream.write(test.getBytes());

        AbstractProcessor downloader = new HTTPRemoteDownloader(processor);
        downloader.writeResponseToMailBox(stream);
    }

    /**
     * Method to test mailbox response without processor name.
     */
    @Test
    public void testWriteResponseToMailBox_WithoutProcessorName() throws IOException, URISyntaxException {

        Processor processor = new Processor();
        processor.setProcsrStatus("ACTIVE");
        String remoteProperties = ServiceUtils
                .readFileFromClassPath("requests/processor/remoteprocessor.json");
        processor.setProcsrProperties(remoteProperties);

        Set<Folder> folders = new HashSet<Folder>();
        Folder folder = new Folder();
        folder.setFldrType("RESPONSE_LOCATION");// Entity
        folder.setFldrUri(responseLocation);
        folders.add(folder);
        processor.setFolders(folders);

        String test = "Testing write response to mailbox.";
        ByteArrayOutputStream stream = new ByteArrayOutputStream(4096);
        stream.write(test.getBytes());

        AbstractProcessor downloader = new HTTPRemoteDownloader(processor);
        downloader.writeResponseToMailBox(stream);
    }

    /**
     *  Method to test mailbox response from file.
     *
     */
    @Test
    public void testWriteFileResponseToMailBox() throws IOException, URISyntaxException {

        Processor processor = new Processor();
        processor.setProcsrName("TestProcessor");
        processor.setProcsrStatus("ACTIVE");
        String remoteProperties = ServiceUtils
                .readFileFromClassPath("requests/processor/remoteprocessor.json");
        processor.setProcsrProperties(remoteProperties);

        Set<Folder> folders = new HashSet<Folder>();
        Folder folder = new Folder();
        folder.setFldrType("RESPONSE_LOCATION");// Entity
        folder.setFldrUri(responseLocation);
        folders.add(folder);
        processor.setFolders(folders);

        String test = "Testing write file response to mailbox.";
        ByteArrayOutputStream stream = new ByteArrayOutputStream(4096);
        stream.write(test.getBytes());

        AbstractProcessor downloader = new HTTPRemoteDownloader(processor);
        downloader.writeResponseToMailBox(stream, "test" + System.nanoTime() + ".txt");
    }

    /**
     * Method to test mailbox response with new directory.
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testWriteFileResponseToMailBox_EveryTimeNewDirectory() throws IOException, URISyntaxException {

        Processor processor = new Processor();
        processor.setProcsrName("TestProcessor");
        processor.setProcsrStatus("ACTIVE");
        String remoteProperties = ServiceUtils
                .readFileFromClassPath("requests/processor/remoteprocessor.json");
        processor.setProcsrProperties(remoteProperties);

        Set<Folder> folders = new HashSet<Folder>();
        Folder folder = new Folder();
        folder.setFldrType("RESPONSE_LOCATION");// Entity
        folder.setFldrUri(responseLocation + File.separator + System.nanoTime());
        folders.add(folder);
        processor.setFolders(folders);

        String test = "Testing write file response to mailbox.";
        ByteArrayOutputStream stream = new ByteArrayOutputStream(4096);
        stream.write(test.getBytes());

        AbstractProcessor downloader = new HTTPRemoteDownloader(processor);
        downloader.writeResponseToMailBox(stream, "test" + System.nanoTime() + ".txt");
    }

    /**
     *  Method to test mailbox response with invalid file name.
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testWriteFileResponseToMailBox_WithFileNameWithSpaces() throws IOException, URISyntaxException {

        Processor processor = new Processor();
        processor.setProcsrName("TestProcessor");
        processor.setProcsrStatus("ACTIVE");
        String remoteProperties = ServiceUtils
                .readFileFromClassPath("requests/processor/remoteprocessor.json");
        processor.setProcsrProperties(remoteProperties);

        Set<Folder> folders = new HashSet<Folder>();
        Folder folder = new Folder();
        folder.setFldrType("RESPONSE_LOCATION");// Entity
        folder.setFldrUri(responseLocation);
        folders.add(folder);
        processor.setFolders(folders);

        String test = "Testing write file response to mailbox.";
        ByteArrayOutputStream stream = new ByteArrayOutputStream(4096);
        stream.write(test.getBytes());

        AbstractProcessor downloader = new HTTPRemoteDownloader(processor);
        downloader.writeResponseToMailBox(stream,
                "test file name" + System.nanoTime() + ".txt");

    }

    @AfterMethod
    public void tearDown() throws Exception {
        deleteFolder(Paths.get(responseLocation));
    }

    private void deleteFolder(Path folder) {

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
