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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.gson.JsonParseException;
import com.liaison.commons.acl.manifest.dto.NestedServiceDependencyContraint;
import com.liaison.commons.exception.BootstrapingFailedException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.security.KeyStoreUtil;
import com.liaison.commons.security.pkcs7.signandverify.DigitalSignature;
import com.liaison.commons.util.bootstrap.BootstrapRemoteKeystore;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.gem.service.client.GEMACLClient;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.gem.service.dto.EnvelopeDTO;
import com.liaison.gem.service.dto.request.ManifestRequestDTO;
import com.liaison.gem.service.dto.request.ManifestRequestDomain;
import com.liaison.gem.service.dto.request.ManifestRequestGEM;
import com.liaison.gem.service.dto.request.ManifestRequestPlatform;
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
	 * Method used to construct the GEMManifestRequest
	 * 
	 * @return ManifestRequestDTO
	 * @throws IOException 
	 */
	public static ManifestRequestDTO constructGEMManifestRequest() throws IOException {

		LOGGER.info("Constructing the gem manifest request with default values");
		// Construct Envelope
		EnvelopeDTO envelope = new EnvelopeDTO();

		// Construct Domain
		ManifestRequestDomain domain = new ManifestRequestDomain();
		domain.setName(MailBoxUtil.getEnvironmentProperties().getString("mailbox.gemrequest.domain.name"));		
		domain.setType(MailBoxUtil.getEnvironmentProperties().getString("mailbox.gemrequest.domain.type"));
		List<String> roles = new ArrayList<String>();
		roles.add(MailBoxUtil.getEnvironmentProperties().getString("mailbox.gemrequest.role.name"));
		domain.setRoles(roles);

		List<ManifestRequestDomain> domains = new ArrayList<ManifestRequestDomain>();
		domains.add(domain);

		// Construct NestedServiceDependency
		NestedServiceDependencyContraint dependencyConstraint = new NestedServiceDependencyContraint();
		dependencyConstraint.setServiceName(MailBoxUtil.getEnvironmentProperties().getString("mailbox.gemrequest.service.name"));		
		//dependencyConstraint.setPrimaryId(configurationInstance.getServiceInstance().getName());
		List<NestedServiceDependencyContraint> constraintList = new ArrayList<NestedServiceDependencyContraint>();
		constraintList.add(dependencyConstraint);
		
		// Construct Platform
		ManifestRequestPlatform platform = new ManifestRequestPlatform();
		platform.setName(MailBoxUtil.getEnvironmentProperties().getString("mailbox.gemrequest.platform.name"));
		platform.setConstraintList(constraintList);
		platform.setDomains(domains);

		List<ManifestRequestPlatform> platforms = new ArrayList<ManifestRequestPlatform>();
		platforms.add(platform);

		// Construct ManifestRequestGEM
		ManifestRequestGEM manifestRequestGEM = new ManifestRequestGEM();
		manifestRequestGEM.setEnvelope(envelope);
		manifestRequestGEM.setPlatforms(platforms);

		// Construct ManifestRequestDTO
		ManifestRequestDTO manifestRequest = new ManifestRequestDTO();
		manifestRequest.setAcl(manifestRequestGEM);

		return manifestRequest;
	}
	
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
			
			ManifestRequestDTO manifestRequestDTO = ACLClientUtil.constructGEMManifestRequest();
			manifestRequest = MailBoxUtil.marshalToJSON(manifestRequestDTO);
			if (!StringUtil.isNullOrEmptyAfterTrim(guid)) {
				unsignedData = guid;
			} else {
				unsignedData = manifestRequest;
			}						
	    	LOGGER.info("get manifest response from GEM.");
		}	
		return gemClient.getACLManifest(unsignedData, ACLClientUtil.signRequestData(unsignedData), 
				publicKeyGuid, manifestRequest);
    }
	
	/**
	 * Method to set the request header for the requests to key manager
	 * 
	 * @param gemManifestResponse
	 * @return requestHeaders in a Map object
	 */
	public static Map<String, String> getRequestHeaders(GEMManifestResponse gemManifestFromGEM) {

		LOGGER.info("setting request headers from gem manifest response to key manager request");
		Map<String, String> headerMap = new HashMap<String, String>();
		String gemManifest = (gemManifestFromGEM != null) ? gemManifestFromGEM.getManifest() : null;
		String signedGEMManifest = (gemManifestFromGEM != null) ? gemManifestFromGEM.getSignature() : null;
		String gemSignerPublicKey = (gemManifestFromGEM != null) ? gemManifestFromGEM.getPublicKeyGuid() : null;
		headerMap.put("gem-manifest", gemManifest);
		headerMap.put("gem-signature", signedGEMManifest);
		headerMap.put("gem_signer_public_key_guid", gemSignerPublicKey);
		headerMap.put("Content-Type", "application/json");
		return headerMap;
	}	
	
	/**
	 * Method used to sign the actual request to be sent to keyManager
	 * 
	 * @param unsignedData
	 * @return String - signed base64 encoded string of actual request to be
	 *         signed
	 * @throws IOException
	 * @throws CMSException
	 * @throws OperatorCreationException
	 * @throws CertificateEncodingException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableKeyException
	 * @throws BootstrapingFailedException
	 */
	public static String signRequestData(String unsignedData) throws CertificateEncodingException, OperatorCreationException, CMSException, IOException,
			KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, BootstrapingFailedException {

		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		}
		char[] password = null;
		KeyStore ks = null;
		// read keystore from Bootstrap
		// BootstrapRemoteKeystore.bootstrap();
		password = BootstrapRemoteKeystore.getDecryptedRemoteKeypairPassphrase().toCharArray();
		LOGGER.debug("Loading keystore from Bootstrap");
		ks = BootstrapRemoteKeystore.getRemoteKeyStore();

		X509Certificate originalSignerCert = KeyStoreUtil.getX509Certificate(ks);
		PrivateKey privateKey = (PrivateKey) ks.getKey(KeyStoreUtil.getKeyAlias(ks), password);
		List<X509Certificate> listOfOriginalSignerCerts = new ArrayList<>();
		listOfOriginalSignerCerts.add(originalSignerCert);

		// FOR SIGNING
		DigitalSignature sig = new DigitalSignature();
		byte[] MESSAGE_TO_SIGN = unsignedData.getBytes();
		byte[] singedData = null;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(MESSAGE_TO_SIGN)) {
		    singedData = sig.sign(bis, originalSignerCert, privateKey);
		}
		return Base64.encodeBase64String(singedData);
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
