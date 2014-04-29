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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.commons.util.client.sftp.G2SFTPClient;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.util.HTTPClientUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
/**
 * Test class to test mailbox configuration service.
 * 
 * @author OFS
 */
public class KeyManagerIntegrationServiceTest extends BaseServiceTest {

	private Logger logger;
	private String jsonResponse;
	private String jsonRequest;
	private HTTPRequest request;
	private G2SFTPClient sftpRequest;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public void setUp() throws Exception {
		logger = LogManager.getLogger(KeyManagerIntegrationServiceTest.class);
		System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
		System.setProperty("archaius.deployment.environment", "ci");
	}
    
	/**
	 * Method constructs TrustStore.
	 * 
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */

	@Test
	public void testCreateTrustStore() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		// Adding the mailbox
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/keymanager/truststorerequest.json");
		
		 // prepare post method  
        HttpPost httpPost = new HttpPost(MailBoxUtil.getEnvironmentProperties().getString("kms-base-url")+"upload/truststore"); 
        DefaultHttpClient httpclient = new DefaultHttpClient();
       
        StringBody jsonRequestBody = new StringBody(jsonRequest, ContentType.APPLICATION_JSON);
        FileBody keyStore = new FileBody(new File(this.getClass().getResource("/requests/keymanager/g2truststore.jks").getPath()));
        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("jsonRequest", jsonRequestBody)
                .addPart("keystore", keyStore)
                .build();
               
        httpPost.setEntity(reqEntity);
        HttpResponse response = httpclient.execute(httpPost);
        System.out.println(response.getStatusLine());
	}
	
	/**
	 * Method to test upload public key with valid data.
	 * 
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testUploadPublicKey() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		// Adding the mailbox
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/keymanager/publickeyrequest.json");
		
		 // prepare post method  
        HttpPost httpPost = new HttpPost(MailBoxUtil.getEnvironmentProperties().getString("kms-base-url")+"/upload/public"); 
        DefaultHttpClient httpclient = new DefaultHttpClient();
       
        StringBody jsonRequestBody = new StringBody(jsonRequest, ContentType.APPLICATION_JSON);
        FileBody publicKeyCert = new FileBody(new File(this.getClass().getResource("/requests/keymanager/publickey.cer").getPath()));
        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("request", jsonRequestBody)
                .addPart("key", publicKeyCert)
                .build();
               
        httpPost.setEntity(reqEntity);
        HttpResponse response = httpclient.execute(httpPost);
        System.out.println(response.getStatusLine());
	}
	
	/**
	 * Method to test upload trustStore with valid data.
	 * 
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */

	@Test
	public void testUpdateTrustStore() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException {

		// Adding the mailbox
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/keymanager/truststore_update_request.json");
		
		 // prepare post method  
        HttpPut httpPut = new HttpPut(MailBoxUtil.getEnvironmentProperties().getString("kms-base-url")+"update/truststore/0C3A3BC50A0037B00665D98D2D86079D"); 
        DefaultHttpClient httpclient = new DefaultHttpClient();
        
        httpPut.addHeader("Content-Type", "application/json");
        httpPut.setEntity(new StringEntity(jsonRequest));
        
        HttpResponse response = httpclient.execute(httpPut);
        System.out.println(response.getStatusLine());
	}
	
	/**
	 * Method to test fetch trustStore with valid data.
	 * 
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws com.liaison.commons.exception.LiaisonException
	 */
	@Test
	public void testFetchTrustStore() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException, com.liaison.commons.exception.LiaisonException {

		// Get the mailbox
		String url = MailBoxUtil.getEnvironmentProperties().getString("kms-base-url")+"fetch/truststore/current/75D5112D0A0006340665134D334351D5";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		System.out.println(jsonResponse);
	}
	
		/**
		 * Method to test httpsTrustStore.
		 * 
		 * @throws LiaisonException
		 * @throws JSONException
		 * @throws JsonParseException
		 * @throws JsonMappingException
		 * @throws JAXBException
		 * @throws IOException
		 * @throws KeyStoreException
		 * @throws NoSuchAlgorithmException
		 * @throws CertificateException
		 * @throws com.liaison.commons.exception.LiaisonException
		 */
		
		public void testHttpsTrustStore() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
				JAXBException, IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, com.liaison.commons.exception.LiaisonException {

			String url = "https://10.0.24.76:19443/g2mailboxservice/rest/v1/mailbox/profile";
			
			//base 64 trust store fetched from key manager service
			String base64 = "/u3+7QAAAAIAAAABAAAAAgBBY249MTAuMC4yNC43Niwgb3U9bGlhaXNvbiwgbz1saWFpc29uLCBsPXVzLCBzdD11cywgYz11czE3MzgyMjM2OTQAAAFDIE3SRgAFWC41MDkAAANjMIIDXzCCAkegAwIBAgIEZ5swTjANBgkqhkiG9w0BAQsFADBgMQswCQYDVQQGEwJ1czELMAkGA1UECBMCdXMxCzAJBgNVBAcTAnVzMRAwDgYDVQQKEwdsaWFpc29uMRAwDgYDVQQLEwdsaWFpc29uMRMwEQYDVQQDEwoxMC4wLjI0Ljc2MB4XDTEzMTExOTEwMDAzN1oXDTE0MDIxNzEwMDAzN1owYDELMAkGA1UEBhMCdXMxCzAJBgNVBAgTAnVzMQswCQYDVQQHEwJ1czEQMA4GA1UEChMHbGlhaXNvbjEQMA4GA1UECxMHbGlhaXNvbjETMBEGA1UEAxMKMTAuMC4yNC43NjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANvzG1gdCS787pIvf6QLJpXOzzbekFud+4Cu/L1ABxl/EuadCIbiOzCQY413hPh00c6Qew8J/BTZpeALHbfkXaSW/ic9O+3W55Wsg9ce7sUi19+M6HJ/6AY+NjyOsRS3xMWkCRcVG7AOjyrefk6g0juY4nJIrMF7uCxr+ZbfD8Prg5ujrM4pMkJD8tPKktvNfy+0Bfpbw7aPfmCe7ArDTQt0uGB4bUzyG5AVxFRVEChqkprsNrERTxUhUkNAb8HvFyqEmUSCsYNimXAbGso7bFr6ZvlLaXJJ7EyMLXruDyIrrl2LtVezD2Wm/1s6Xb/pArUyboakKfapzHthZ3F5bOcCAwEAAaMhMB8wHQYDVR0OBBYEFNeX5NFshSd5+10DIhRUrFlLRn/eMA0GCSqGSIb3DQEBCwUAA4IBAQCqZfN5tcjbWX/0YojN/tg5fbK+VPuFU53pKsm8v1DdgutmZq3gdwmrGOaAhq00gV0QyhIxSs3AE3UjFHXJYIZ1GBkxnpzz8uLy1EDmQRK+YrP+Dh/7MZzH04v0EFa+ZF2J8mg3yswlxWsUjUD9kF9L/bmHcuZK9hTsahRJu/Yb1vUYSFtNRmOl5ZnsIp6LkJzYB+zMiOmE5ERWDu9G9yzDpa+h3sfa4FKyTOcEnJ7+BcYC8pRde7wJDCiCdY/7B21HVigT66bLu9Lm1sG38i2K4sJdUo8QhUx2rFVpqy3GoJ39s3kn8OBRXbhOgmHK+I1VYs0XfdxakIgNngsoa5n0EoKMicJ7FO2frbtbEQQhe17JWLw=";
			
			byte[] trustStoreBytes = Base64.decodeBase64(base64.getBytes());
			
			InputStream is = new ByteArrayInputStream(trustStoreBytes);
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

		    char[] password = {'L','i','a','i','s','o','n','@','1','2','3'};
		   	    try {
		        ks.load(is, password);
		    } finally {
		        if (is != null) {
		            is.close();
		        }
		    }
			
			request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
			request.truststore(ks);
			request.execute();
			
			jsonResponse = getOutput().toString();
			System.out.println(jsonResponse);
		}
		
		/**
		 * Method to test SFTP With SSH Keypair.
		 * 
		 * @throws IOException
		 * @throws LiaisonException
		 * @throws JSONException
		 * @throws LiaisonException
		 * @throws com.liaison.commons.exception.LiaisonException
		 */
		@Test
		public void testSFTPWithSSHKeypair() throws IOException, LiaisonException, JSONException, LiaisonException, com.liaison.commons.exception.LiaisonException {
			//InputStream is = null;
			byte[] privateKeyBytes = null;
			
			String url = "http://10.0.6.101:8989/key-management/fetch/group/keypair/current/2798BD330A004E740665A6EF61AE4A01";
			//  F45EE0F10A006FF106655CE31D400F66
			
			Map<String, String> headerMap = new HashMap<String, String>();
			headerMap.put("Content-Type", "application/json");
			String jsonResponse = HTTPClientUtil.getHTTPResponseInString(logger, url, headerMap);
			
			if (jsonResponse != null) {
				
				String base64EncodedStr = new JSONObject(jsonResponse).getJSONObject("dataTransferObject").getString("currentPrivateKey");
			  	privateKeyBytes = Base64.decodeBase64(base64EncodedStr);
				
			}
			String privateKeyPath = this.getClass().getResource("/requests/keymanager/opensslkeypair.pem").getPath();
			String password = "passmein";
			/*FileOutputStream out = new FileOutputStream(privateKeyPath);
			out.write(privateKeyBytes);
			out.close();*/
			
			sftpRequest = new G2SFTPClient();
			sftpRequest.setURI("sftp://10.0.24.40:22");
			sftpRequest.setPrivateKeyPath(privateKeyPath);
			sftpRequest.setStrictHostChecking(false);
			sftpRequest.setUser("g2testusr");
			sftpRequest.setPassphrase(password);
			sftpRequest.setDiagnosticLogger(logger);
			sftpRequest.setCommandLogger(logger);
			boolean value = sftpRequest.connect();
			if(value) System.out.println("true sftp connected successfully");
			
		}
		
		/**
		 * Method to test SFTP with SSH key pair downloaded from keymanager.
		 * 
		 * @throws IOException
		 * @throws LiaisonException
		 * @throws JSONException
		 * @throws LiaisonException
		 * @throws com.liaison.commons.exception.LiaisonException
		 */
		@Test
		public void testSFTPWithSSHKeypairDownloadedFromKeyManager() throws IOException, LiaisonException, JSONException, LiaisonException, com.liaison.commons.exception.LiaisonException {
			//InputStream is = null;
			byte[] privateKeyBytes = null;
			
			String url = "http://10.0.6.101:8989/key-management/fetch/group/keypair/current/27C062AB0A004E740665A6EFCE333E6A";
			
			
			Map<String, String> headerMap = new HashMap<String, String>();
			headerMap.put("Content-Type", "application/json");
			String jsonResponse = HTTPClientUtil.getHTTPResponseInString(logger, url, headerMap);
			
			if (jsonResponse != null) {
				
				String base64EncodedStr = new JSONObject(jsonResponse).getJSONObject("dataTransferObject").getString("currentPrivateKey");
			  	privateKeyBytes = base64EncodedStr.getBytes();
				
			}
			String privateKeyPath = this.getClass().getResource("/requests/keymanager/opensslkeypair.txt").getPath();
			//String password = "passmein";
			FileOutputStream out = new FileOutputStream(privateKeyPath);
			out.write(privateKeyBytes);
			out.close();
			
			sftpRequest = new G2SFTPClient();
			sftpRequest.setURI("sftp://10.0.24.40:22");
			sftpRequest.setPrivateKeyPath(privateKeyPath);
			sftpRequest.setStrictHostChecking(false);
			sftpRequest.setUser("g2testusr");
			//sftpRequest.setPassphrase(password);
			sftpRequest.setDiagnosticLogger(logger);
			sftpRequest.setCommandLogger(logger);
			boolean value = sftpRequest.connect();
			if(value) System.out.println("true sftp connected successfully");
			sftpRequest.disconnect();
			
		}
}
