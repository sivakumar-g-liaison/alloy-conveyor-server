/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.services.util.unit.test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.service.core.processor.FileWriter;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Class to test the MailBoxUtil
 * 
 * @author OFS
 */
public class MailBoxUtilTest {

    private String ftpURLWithPort = "ftp://10.146.18.10:25";
    private String ftpURLWithoutPort = "ftp://10.146.18.10";
    private String ftpsURLWithPort = "ftps://10.146.18.15:26";
    private String ftpsURLWithoutPort = "ftps://10.146.18.15";
    private String sftpURLWithPort = "sftp://10.146.18.20:28";
    private String sftpURLWithoutPort = "sftp://10.146.18.20";
    private String httpURLWithPort = "http://mailbox.liaison.com:30";
    private String httpURLWithoutPort = "http://mailbox.liaison.com";
    private String httpsURLWithPort = "https://mailbox.liaison.com:32";
    private String httpsURLWithoutPort = "https://mailbox.liaison.com";
    
    /**
     * @throws Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
        System.setProperty("com.liaison.secure.properties.path", "invalid");
        System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
    }
    
    @Test
    public void test_FTP_WithValidURLAndPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(ftpURLWithPort);
        propertiesDTO.setPort(25);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(ftpURLWithPort, propertiesDTO.getUrl());
        Assert.assertEquals(25, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_FTP_WithValidURLAndWithoutPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(ftpURLWithPort);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(ftpURLWithPort, propertiesDTO.getUrl());
        Assert.assertEquals(25, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_FTP_WithoutPortInURLAndWithPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(ftpURLWithoutPort);
        propertiesDTO.setPort(25);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(ftpURLWithPort, propertiesDTO.getUrl());
        Assert.assertEquals(25, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_FTP_WithoutPortInURLAndWithoutPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(ftpURLWithoutPort);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(ftpURLWithoutPort + ":" + MailBoxConstants.FTPS_PORT, propertiesDTO.getUrl());
        Assert.assertEquals(MailBoxConstants.FTPS_PORT, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_FTPS_WithValidURLAndPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(ftpsURLWithPort);
        propertiesDTO.setPort(26);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(ftpsURLWithPort, propertiesDTO.getUrl());
        Assert.assertEquals(26, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_FTPS_WithValidURLAndWithoutPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(ftpsURLWithPort);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(ftpsURLWithPort, propertiesDTO.getUrl());
        Assert.assertEquals(26, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_FTPS_WithoutPortInURLAndWithPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(ftpsURLWithoutPort);
        propertiesDTO.setPort(26);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(ftpsURLWithPort, propertiesDTO.getUrl());
        Assert.assertEquals(26, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_FTPS_WithoutPortInURLAndWithoutPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(ftpsURLWithoutPort);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(ftpsURLWithoutPort + ":" + MailBoxConstants.FTPS_PORT, propertiesDTO.getUrl());
        Assert.assertEquals(MailBoxConstants.FTPS_PORT, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_SFTP_WithValidURLAndPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(sftpURLWithPort);
        propertiesDTO.setPort(28);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(sftpURLWithPort, propertiesDTO.getUrl());
        Assert.assertEquals(28, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_SFTP_WithValidURLAndWithoutPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(sftpURLWithPort);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(sftpURLWithPort, propertiesDTO.getUrl());
        Assert.assertEquals(28, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_SFTP_WithoutPortInURLAndWithPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(sftpURLWithoutPort);
        propertiesDTO.setPort(28);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(sftpURLWithPort, propertiesDTO.getUrl());
        Assert.assertEquals(28, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_SFTP_WithoutPortInURLAndWithoutPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(sftpURLWithoutPort);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(sftpURLWithoutPort + ":" + MailBoxConstants.SFTP_PORT, propertiesDTO.getUrl());
        Assert.assertEquals(MailBoxConstants.SFTP_PORT, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_HTTP_WithValidURLAndPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(httpURLWithPort);
        propertiesDTO.setPort(30);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(httpURLWithPort, propertiesDTO.getUrl());
        Assert.assertEquals(30, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_HTTP_WithValidURLAndWithoutPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(httpURLWithPort);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(httpURLWithPort, propertiesDTO.getUrl());
        Assert.assertEquals(30, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_HTTP_WithoutPortInURLAndWithPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(httpURLWithoutPort);
        propertiesDTO.setPort(30);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(httpURLWithPort, propertiesDTO.getUrl());
        Assert.assertEquals(30, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_HTTP_WithoutPortInURLAndWithoutPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(httpURLWithoutPort);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(httpURLWithoutPort + ":" + MailBoxConstants.HTTP_PORT, propertiesDTO.getUrl());
        Assert.assertEquals(MailBoxConstants.HTTP_PORT, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_HTTPS_WithValidURLAndPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(httpsURLWithPort);
        propertiesDTO.setPort(32);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(httpsURLWithPort, propertiesDTO.getUrl());
        Assert.assertEquals(32, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_HTTPS_WithValidURLAndWithoutPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(httpsURLWithPort);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(httpsURLWithPort, propertiesDTO.getUrl());
        Assert.assertEquals(32, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_HTTPS_WithoutPortInURLAndWithPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(httpsURLWithoutPort);
        propertiesDTO.setPort(32);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(httpsURLWithPort, propertiesDTO.getUrl());
        Assert.assertEquals(32, propertiesDTO.getPort());
        
    }
    
    @Test
    public void test_HTTPS_WithoutPortInURLAndWithoutPort() throws MalformedURLException, URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        propertiesDTO.setUrl(httpsURLWithoutPort);
        MailBoxUtil.constructURLAndPort(propertiesDTO);
        
        Assert.assertNotNull(propertiesDTO);
        Assert.assertEquals(httpsURLWithoutPort + ":" + MailBoxConstants.HTTPS_PORT, propertiesDTO.getUrl());
        Assert.assertEquals(MailBoxConstants.HTTPS_PORT, propertiesDTO.getPort());
        
    }

    /**
     * Method to test valid processor type and protocol for HTTP Uploader.
     * 
     */
    @Test
    public void testValidHttpOrHttpsRemoteUploader() {

        boolean isvalid;
        Processor proces = constructUploaderProcessor();

        proces.setProcsrProtocol(MailBoxConstants.HTTP);
        isvalid = MailBoxUtil.isHttpOrHttpsRemoteUploader(proces);
        Assert.assertEquals(isvalid, true);

        proces.setProcsrProtocol(MailBoxConstants.HTTPS);
        isvalid = MailBoxUtil.isHttpOrHttpsRemoteUploader(proces);
        Assert.assertEquals(isvalid, true);
    }

    /**
     * Method to test Invalid processor type and protocol for HTTP Uploader.
     * 
     */
    @Test
    public void testInvalidHttpOrHttpsRemoteUploader() {

        boolean isvalid;
        Processor proces;
        
        // uploader
        proces = constructUploaderProcessor();
        
        proces.setProcsrProtocol(MailBoxConstants.SFTP);
        isvalid = MailBoxUtil.isHttpOrHttpsRemoteUploader(proces);
        Assert.assertEquals(isvalid, false);

        proces.setProcsrProtocol(MailBoxConstants.FTP);
        isvalid = MailBoxUtil.isHttpOrHttpsRemoteUploader(proces);
        Assert.assertEquals(isvalid, false);

        proces.setProcsrProtocol(MailBoxConstants.FTPS);
        isvalid = MailBoxUtil.isHttpOrHttpsRemoteUploader(proces);
        Assert.assertEquals(isvalid, false);
        
        // downloader
        proces = constructDownloaderProcessor();

        proces.setProcsrProtocol(MailBoxConstants.SFTP);
        isvalid = MailBoxUtil.isHttpOrHttpsRemoteUploader(proces);
        Assert.assertEquals(isvalid, false);

        proces.setProcsrProtocol(MailBoxConstants.FTP);
        isvalid = MailBoxUtil.isHttpOrHttpsRemoteUploader(proces);
        Assert.assertEquals(isvalid, false);

        proces.setProcsrProtocol(MailBoxConstants.FTPS);
        isvalid = MailBoxUtil.isHttpOrHttpsRemoteUploader(proces);
        Assert.assertEquals(isvalid, false);

        proces.setProcsrProtocol(MailBoxConstants.HTTP);
        isvalid = MailBoxUtil.isHttpOrHttpsRemoteUploader(proces);
        Assert.assertEquals(isvalid, false);
 
        proces.setProcsrProtocol(MailBoxConstants.HTTPS);
        isvalid = MailBoxUtil.isHttpOrHttpsRemoteUploader(proces);
        Assert.assertEquals(isvalid, false);
    }
    
    private Processor constructUploaderProcessor() {
        ProcessorType foundProcessorType = ProcessorType.findByName(ProcessorType.REMOTEUPLOADER.getCode());
        return Processor.processorInstanceFactory(foundProcessorType);
    }

    private Processor constructDownloaderProcessor() {
        ProcessorType foundProcessorType = ProcessorType.findByName(ProcessorType.REMOTEDOWNLOADER.getCode());
        return Processor.processorInstanceFactory(foundProcessorType);
    }
    
}
