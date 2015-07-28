/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.processor.helper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.codehaus.jettison.json.JSONException;

import com.liaison.commons.exception.BootstrapingFailedException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.sftp.G2SFTPClient;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.mailbox.dtdm.model.Credential;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.service.core.processor.AbstractProcessor;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SFTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SFTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.KMSUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * @author VNagarajan
 *
 */
public class SFTPClient {

	private static final Logger LOGGER = LogManager.getLogger(HTTPClient.class);

	/**
	 * @param processor
	 * @return
	 */
	public static Object getClient(AbstractProcessor processor) {

		try {
			SFTPUploaderPropertiesDTO sftpUploaderStaticProperties = null;
			SFTPDownloaderPropertiesDTO sftpDownloaderStaticProperties = null;
			String url = null;
			int connectionTimeout = 0;
			int retryAttempts = 0;

			if (processor.getConfigurationInstance().getProcessorType().equals(ProcessorType.REMOTEUPLOADER)) {
				sftpUploaderStaticProperties = (SFTPUploaderPropertiesDTO) processor.getProperties();
				url = sftpUploaderStaticProperties.getUrl();
				connectionTimeout = sftpUploaderStaticProperties.getConnectionTimeout();
				retryAttempts = sftpUploaderStaticProperties.getRetryAttempts();
			} else if (processor.getConfigurationInstance().getProcessorType().equals(ProcessorType.REMOTEDOWNLOADER)) {
				sftpDownloaderStaticProperties = (SFTPDownloaderPropertiesDTO) processor.getProperties();
				url = sftpDownloaderStaticProperties.getUrl();
				connectionTimeout = sftpDownloaderStaticProperties.getConnectionTimeout();
				retryAttempts = sftpDownloaderStaticProperties.getRetryAttempts();
			}

			// retrieve required properties
			G2SFTPClient sftpRequest = new G2SFTPClient();
			sftpRequest.setURI(url);

			// set Log Level
			Level level = (sftpDownloaderStaticProperties.isDebugTranscript()) ? Level.INFO : Level.DEBUG;
			MailBoxUtil.setLogLevelDuringRuntime(SFTPClient.class.getName(), level);

			sftpRequest.setDiagnosticLogger(LOGGER);
			sftpRequest.setCommandLogger(LOGGER);
			sftpRequest.setTimeout(connectionTimeout);
			sftpRequest.setStrictHostChecking(false);
			sftpRequest.setRetryCount(retryAttempts);

			Credential loginCredential = processor.getCredentialOfSpecificType(CredentialType.LOGIN_CREDENTIAL);

			if ((loginCredential != null)) {

				// password has to be retrieved from KMS only if password is
				// present in login credential
				// in case of sftp using keys, password will not be available
				// and hence retrieval of
				// password from KMS is not valid in this case.
				String passwordFromKMS = (!StringUtil.isNullOrEmptyAfterTrim(loginCredential.getCredsPassword())) ? KMSUtil
						.getSecretFromKMS(loginCredential.getCredsPassword()) : null;

				if (!MailBoxUtil.isEmpty(loginCredential.getCredsUsername())) {
					sftpRequest.setUser(loginCredential.getCredsUsername());

				}
				if (!MailBoxUtil.isEmpty(passwordFromKMS)) {
					sftpRequest.setPassword(passwordFromKMS);
				}
			}
			Credential sshKeyPairCredential = processor.getCredentialOfSpecificType(CredentialType.SSH_KEYPAIR);

			if (sshKeyPairCredential != null) {

				if (MailBoxUtil.isEmpty(sshKeyPairCredential.getCredsIdpUri())) {

					LOGGER.info("Credential requires file path");
					throw new MailBoxServicesException("Credential requires file path", Response.Status.CONFLICT);
				}

				byte[] privateKeyStream = KMSUtil.fetchSSHPrivateKey(sshKeyPairCredential.getCredsIdpUri());

				if (privateKeyStream == null) {
					throw new MailBoxServicesException(Messages.SSHKEY_RETRIEVE_FAILED, Response.Status.BAD_REQUEST);
				}

				String privateKeyPath = MailBoxUtil.getEnvironmentProperties().getString(
						"ssh.private.key.temp.location")
						+ sshKeyPairCredential.getCredsIdpUri() + ".txt";
				// write to a file
				try (FileOutputStream out = new FileOutputStream(privateKeyPath)) {
					out.write(privateKeyStream);
				}
				sftpRequest.setPrivateKeyPath(privateKeyPath);

			}

			return sftpRequest;
		} catch (JAXBException | IOException | LiaisonException | MailBoxServicesException
				| SymmetricAlgorithmException | CertificateEncodingException | UnrecoverableKeyException
				| OperatorCreationException | KeyStoreException | NoSuchAlgorithmException | JSONException
				| CMSException | BootstrapingFailedException | IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}

	}
}
