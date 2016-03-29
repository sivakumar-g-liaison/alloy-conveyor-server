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
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.gem.service.client.GEMHelper;
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
	public static final String UTF_ENCODE = "UTF-8";

	/**
	 * Util method get stored secret from KMS
	 *
	 * @param guid
	 * @return String
	 * @throws MalformedURLException
	 * @throws LiaisonException
	 * @throws IOException
	 */
	public static String getSecretFromKMS(String guid) throws MalformedURLException, LiaisonException, IOException {

		// get gem manifest response from GEM
		GEMManifestResponse gemManifestFromGEM = GEMHelper.getACLManifest();

		// setting the request headers in the request to key manager from gem
		Map<String, String> headerMap = GEMHelper.getRequestHeaders(gemManifestFromGEM, "application/json");

		String url = MailBoxUtil.getEnvironmentProperties().getString("kms-base-url") + "secret/" + URLEncoder.encode(guid, UTF_ENCODE);
		String base64EncodedPassword = HTTPClientUtil.getHTTPResponseInString(LOGGER, url, headerMap);

		if (base64EncodedPassword == null || base64EncodedPassword.isEmpty()) {
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
	 * @param keypairPguid
	 * @return
	 * @throws MalformedURLException
	 * @throws LiaisonException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static byte[] fetchSSHPrivateKey(String keypairPguid) throws MalformedURLException, LiaisonException, IOException, JAXBException {

		byte[] privateKeyBytes = null;

		String url = MailBoxUtil.getEnvironmentProperties().getString("kms-base-url");
		url = url + "fetch/group/keypair/current/";

		// To be fetched from DataBase
		url = url + URLEncoder.encode(keypairPguid, UTF_ENCODE);
		// get gem manifest response from GEM
		GEMManifestResponse gemManifestFromGEM = GEMHelper.getACLManifest();

		// setting the request headers in the request to key manager from gem
		// manifest response
		Map<String, String> headerMap = GEMHelper.getRequestHeaders(gemManifestFromGEM, "application/json");

		String jsonResponse = HTTPClientUtil.getHTTPResponseInString(LOGGER, url, headerMap);

		if (jsonResponse != null) {

			KeyServiceResponse mkr = JAXBUtility.unmarshalFromJSON(jsonResponse, KeyServiceResponse.class);
			KeySet keySet = (KeySet) mkr.getDataTransferObject();
			privateKeyBytes = keySet.getCurrentPrivateKey().getBytes();
		}

		return privateKeyBytes;
	}

	/**
	 *
	 * Method for fetching TrustStore as an InputStream
	 *
	 * @param trustStoreId
	 * @return
	 * @throws MalformedURLException
	 * @throws LiaisonException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static InputStream fetchTrustStore(String trustStoreId) throws MalformedURLException, LiaisonException, IOException, JAXBException {

		InputStream is = null;

		String url = MailBoxUtil.getEnvironmentProperties().getString("kms-base-url");
		url = url + "fetch/truststore/current/";

		// To be fetched from DataBase
		url = url + URLEncoder.encode(trustStoreId, UTF_ENCODE);

		// get gem manifest response from GEM
		GEMManifestResponse gemManifestFromGEM = GEMHelper.getACLManifest();

		Map<String, String> headerMap = GEMHelper.getRequestHeaders(gemManifestFromGEM, "application/json");

        LOGGER.debug("The KMS URL TO PULL TRUSTSTORE IS " + url);
		String jsonResponse = HTTPClientUtil.getHTTPResponseInString(LOGGER, url, headerMap);

		if (jsonResponse != null) {

			KeyServiceResponse mkr = JAXBUtility.unmarshalFromJSON(jsonResponse, KeyServiceResponse.class);
			KeySet keySet = (KeySet) mkr.getDataTransferObject();
			is = new ByteArrayInputStream(Base64.decodeBase64(keySet.getCurrentPublicKey()));
		}

		if (null == is) {
			throw new MailBoxServicesException(Messages.CERTIFICATE_RETRIEVE_FAILED, Response.Status.BAD_REQUEST);
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
		if (baseUrl == null) {
			throw new RuntimeException(String.format("Property [%s] cannot be null", PROPERTY_KEY_MANAGEMENT_BASE_URL));
		}
		// strip trailing slashes
		while (baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		return baseUrl + path;
	}

}
