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

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;

import javax.xml.bind.JAXBException;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.gson.JsonParseException;
import com.liaison.commons.exception.BootstrapingFailedException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.gem.service.client.GEMACLClient;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.gem.service.dto.request.ManifestRequestDTO;
import com.liaison.mailbox.MailBoxConstants;

/**
 * Utilities for ACL.
 *
 * @author OFS
 */
public class ACLClientUtil {
	
	private static final Logger LOGGER = LogManager.getLogger(ACLClientUtil.class);
	public static final String TRADITIONAL_ACL_REQUEST = "traditional.acl.request";	
	
	/**
	 * method retrieve SignedManifest response
	 * 
	 * @param guid
	 * @return GEMManifestResponse
	 * @throws IOException
	 * @throws CertificateEncodingException
	 * @throws UnrecoverableKeyException
	 * @throws OperatorCreationException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CMSException
	 * @throws BootstrapingFailedException
	 * @throws LiaisonException
	 * @throws JAXBException 
	 * @throws JsonParseException 
	 */
	public static GEMManifestResponse retrieveSignedManifestDTO(String guid) throws IOException, CertificateEncodingException, UnrecoverableKeyException, OperatorCreationException, KeyStoreException, NoSuchAlgorithmException, CMSException, BootstrapingFailedException, LiaisonException, JsonParseException, JAXBException {
    	
		LOGGER.debug("Entering the retrieveSignedManifestDTO method.");
		// get gem manifest response		
		GEMACLClient gemClient = new GEMACLClient();
		String unsignedData;
		String manifestRequest;
		// read the public key guid from properties file
		String publicKeyGuid = MailBoxUtil.getEnvironmentProperties().getString("mailbox.signer.public.key.guid");
		
		if (Boolean.valueOf(MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.ACL_BACKWARD_COMPATABILITY_PROPERTY))) {
			
			manifestRequest = constructACLManifestRequest();
			if (!StringUtil.isNullOrEmptyAfterTrim(guid)) {
				unsignedData = guid;
			} else {
				unsignedData = manifestRequest;
			}				
			LOGGER.info("get manifest response from ACL.");
		} else {
			
			ManifestRequestDTO manifestRequestDTO = gemClient.constructACLManifestRequest();
			manifestRequest = MailBoxUtil.marshalToJSON(manifestRequestDTO);
			if (!StringUtil.isNullOrEmptyAfterTrim(guid)) {
				unsignedData = guid;
			} else {
				unsignedData = manifestRequest;
			}						
	    	LOGGER.info("get manifest response from GEM.");
		}	
		return gemClient.getACLManifest(unsignedData, gemClient.signRequestData(unsignedData), 
				publicKeyGuid, manifestRequest);
    }
	
	/**
     * Method used to construct the ACLManifestRequest json.
     * 
     * @return GEMManifestResponse
     * @throws JsonMappingException
     * @throws JsonParseException
     * @throws JAXBException
     * @throws IOException
     * @throws CertificateEncodingException
     * @throws UnrecoverableKeyException
     * @throws OperatorCreationException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CMSException
     * @throws BootstrapingFailedException
     */
    private static String constructACLManifestRequest() throws IOException {
       	LOGGER.info("Constructing the acl manifest request");    	
    	String encodedAclManifestJson = MailBoxUtil.getEnvironmentProperties().getString(TRADITIONAL_ACL_REQUEST);
    	String decodedManifestJson = new String(Base64.decodeBase64(encodedAclManifestJson), "UTF-8");		
    	return decodedManifestJson;
    }

}
