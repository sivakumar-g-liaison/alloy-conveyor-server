/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.codehaus.jettison.json.JSONException;

import com.google.gson.JsonParseException;
import com.liaison.commons.exception.BootstrapingFailedException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.keymanage.grammar.KeyServiceResponse;
import com.liaison.keymanage.grammar.KeySet;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.exception.MailBoxServicesException;

/**
 * Utilities for KMS.
 *
 * @author OFS
 */
public class KMSUtil {
	
	private static final Logger LOGGER = LogManager.getLogger(KMSUtil.class);
	public static final String PROPERTY_KEY_MANAGEMENT_BASE_URL = "client.key-management.baseUrl";
	
	/**
	 * Util method get stored secret from KMS
	 * 
	 * @param guid
	 * @return String
	 * @throws CertificateEncodingException
	 * @throws UnrecoverableKeyException
	 * @throws OperatorCreationException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CMSException
	 * @throws IOException
	 * @throws BootstrapingFailedException
	 * @throws LiaisonException
	 * @throws MailBoxServicesException
	 * @throws JAXBException 
	 * @throws JsonParseException 
	 */
	public static String getSecretFromKMS(String guid) throws CertificateEncodingException, UnrecoverableKeyException, OperatorCreationException, KeyStoreException,
			NoSuchAlgorithmException, CMSException, IOException, BootstrapingFailedException, LiaisonException, MailBoxServicesException, JsonParseException, JAXBException {
		
			// get gem manifest response from GEM
			String unsignedData = guid;
			GEMManifestResponse gemManifestFromGEM = ACLClientUtil.retrieveSignedManifestDTO(unsignedData);
			
			// setting the request headers in the request to key manager from gem
			// manifest response
			Map<String, String> headerMap = ACLClientUtil.getRequestHeaders(gemManifestFromGEM);
			String url = MailBoxUtil.getEnvironmentProperties().getString("kms-base-url") + "secret/" + guid;
			String base64EncodedPassword = HTTPClientUtil.getHTTPResponseInString(LOGGER, url, headerMap);
			
			if (base64EncodedPassword == null || base64EncodedPassword == "") {
				throw new MailBoxServicesException(Messages.READ_SECRET_FAILED, Response.Status.BAD_REQUEST);
			} else {
				String decodeLevel1 = new String(Base64.decodeBase64(base64EncodedPassword));
				String base64DecodedPassword = new String(Base64.decodeBase64(decodeLevel1));
				return base64DecodedPassword;
			}
    }
	
	/**
	 * 
	 * Method for fetching SSH Privatekey as an InputStream
	 * 
	 * @return InputStream
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws IOException
	 * @throws CMSException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws OperatorCreationException
	 * @throws UnrecoverableKeyException
	 * @throws CertificateEncodingException
	 * @throws BootstrapingFailedException
	 * @throws JAXBException
	 */
	public static byte[] fetchSSHPrivateKey(String keypairPguid) throws LiaisonException, JSONException, IOException, CertificateEncodingException,
			UnrecoverableKeyException, OperatorCreationException, KeyStoreException, NoSuchAlgorithmException, CMSException, BootstrapingFailedException,
			JAXBException {

		byte[] privateKeyBytes = null;

		String url = MailBoxUtil.getEnvironmentProperties().getString("kms-base-url");
		url = url + "fetch/group/keypair/current/";

		// To be fetched from DataBase
		url = url + keypairPguid;
		
		// get gem manifest response from GEM
	    String unsignedData = keypairPguid;
		GEMManifestResponse gemManifestFromGEM = ACLClientUtil.retrieveSignedManifestDTO(unsignedData);
		
		// setting the request headers in the request to key manager from gem
		// manifest response
		Map<String, String> headerMap = ACLClientUtil.getRequestHeaders(gemManifestFromGEM);
		String jsonResponse = HTTPClientUtil.getHTTPResponseInString(LOGGER, url, headerMap);

		if (jsonResponse != null) {

			KeyServiceResponse mkr = JSONUtil.unmarshalFromKeyManagerJSON(jsonResponse, KeyServiceResponse.class);
			KeySet keySet = (KeySet) mkr.getDataTransferObject();
			privateKeyBytes = keySet.getCurrentPrivateKey().getBytes();
		}

		return privateKeyBytes;
	}
	
	/**
	 * 
	 * Method for fetching TrustStore as an InputStream
	 * 
	 * @return InputStream
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws BootstrapingFailedException 
	 * @throws CMSException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyStoreException 
	 * @throws OperatorCreationException 
	 * @throws UnrecoverableKeyException 
	 * @throws CertificateEncodingException 
	 */
	public static InputStream fetchTrustStore(String trustStoreId) throws LiaisonException, JSONException, IOException, JAXBException, CertificateEncodingException, UnrecoverableKeyException, OperatorCreationException, KeyStoreException, NoSuchAlgorithmException, CMSException, BootstrapingFailedException {

		InputStream is = null;

		String url = MailBoxUtil.getEnvironmentProperties().getString("kms-base-url");
		url = url + "fetch/truststore/current/";

		// To be fetched from DataBase
		url = url + trustStoreId;
		
		// get gem manifest response from GEM
	    String unsignedData = trustStoreId;
		GEMManifestResponse gemManifestFromGEM = ACLClientUtil.retrieveSignedManifestDTO(unsignedData);
		
		Map<String, String> headerMap = ACLClientUtil.getRequestHeaders(gemManifestFromGEM);

		LOGGER.info("The KMS URL TO PULL TRUSTSTORE IS " + url);
		String jsonResponse = HTTPClientUtil.getHTTPResponseInString(LOGGER, url, headerMap);

		if (jsonResponse != null) {

			KeyServiceResponse mkr = JSONUtil.unmarshalFromKeyManagerJSON(jsonResponse, KeyServiceResponse.class);

			KeySet keySet = (KeySet) mkr.getDataTransferObject();
			is = new ByteArrayInputStream(Base64.decodeBase64(keySet.getCurrentPublicKey()));
		}

		return is;
	}
	
	/**
     * Construct a KMS URL from a partial path. Base URL comes from properties.
     *
     * @param path
     * @return String
	 * @throws IOException 
     */
    public static String getKeyManagementUrl(String path) throws IOException {
    	
        String baseUrl = MailBoxUtil.getEnvironmentProperties().getString(PROPERTY_KEY_MANAGEMENT_BASE_URL);
        if(baseUrl == null) {
            throw new RuntimeException(String.format("Property [%s] cannot be null", PROPERTY_KEY_MANAGEMENT_BASE_URL));
        }
        // strip trailing slashes
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + path;
    } 

}
