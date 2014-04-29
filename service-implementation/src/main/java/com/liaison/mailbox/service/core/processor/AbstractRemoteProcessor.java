/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.processor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.gson.JsonParseException;
import com.liaison.accessmanagement.service.client.ACLClient;
import com.liaison.accessmanagement.service.client.ACLManifestResponse;
import com.liaison.accessmanagement.service.dto.EnvelopeDTO;
import com.liaison.accessmanagement.service.dto.request.ManifestRequestACL;
import com.liaison.accessmanagement.service.dto.request.ManifestRequestDTO;
import com.liaison.accessmanagement.service.dto.request.ManifestRequestDomain;
import com.liaison.accessmanagement.service.dto.request.ManifestRequestPlatform;
import com.liaison.commons.acl.manifest.dto.NestedServiceDependencyContraint;
import com.liaison.commons.exception.BootstrapingFailedException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.security.KeyStoreUtil;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.security.pkcs7.signandverify.DigitalSignature;
import com.liaison.commons.util.bootstrap.BootstrapRemoteKeystore;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.authentication.BasicAuthenticationHandler;
import com.liaison.commons.util.client.sftp.G2SFTPClient;
import com.liaison.fs2.api.FS2Exception;
import com.liaison.keymanage.grammar.KeyServiceResponse;
import com.liaison.keymanage.grammar.KeySet;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.FolderType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.Credential;
import com.liaison.mailbox.jpa.model.Folder;
import com.liaison.mailbox.jpa.model.MailBoxProperty;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.model.ProcessorProperty;
import com.liaison.mailbox.service.core.EmailNotifier;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.dto.configuration.DynamicPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.FolderDTO;
import com.liaison.mailbox.service.dto.configuration.request.CredentialInfoModel;
import com.liaison.mailbox.service.dto.configuration.request.HttpOtherRequestHeaderDTO;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.dto.directorysweeper.FileAttributesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.HTTPClientUtil;
import com.liaison.mailbox.service.util.MailBoxCryptoUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * @author OFS
 * 
 */
public abstract class AbstractRemoteProcessor {

	private static final Logger LOGGER = LogManager
			.getLogger(AbstractRemoteProcessor.class);

	private static final ProcessorConfigurationDAO PROCESSOR_DAO = new ProcessorConfigurationDAOBase();

	protected Processor configurationInstance;
	protected Properties mailBoxProperties;
	protected RemoteProcessorPropertiesDTO remoteProcessorProperties;

	public AbstractRemoteProcessor() {
	}

	public AbstractRemoteProcessor(Processor configurationInstance) {
		this.configurationInstance = configurationInstance;
	}

	/**
	 * Construct DTO from Entity.
	 * 
	 * @return the remoteProcessorProperties
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public RemoteProcessorPropertiesDTO getRemoteProcessorProperties()
			throws JsonParseException, JsonMappingException, JAXBException,
			IOException {

		if (null == remoteProcessorProperties) {
			remoteProcessorProperties = MailBoxUtil.unmarshalFromJSON(
					configurationInstance.getProcsrProperties(),
					RemoteProcessorPropertiesDTO.class);
		}

		return remoteProcessorProperties;
	}

	/**
	 * This will return a HTTP,HTTPS or FTPS client based on the processor type.
	 * 
	 * @return The Object based on processor type.
	 */
	public Object getClient() {

		Protocol foundProtocolType = Protocol.findByCode(configurationInstance
				.getProcsrProtocol());

		switch (foundProtocolType) {

		case FTP:
			return new G2FTPSClient();
		case FTPS:
			return new G2FTPSClient();
		case SFTP:
			return new G2SFTPClient();
		case HTTP:
			return new HTTPRequest(null, LOGGER);
		case HTTPS:
			return new HTTPRequest(null, LOGGER);
		default:
			return null;
		}
	}

	/**
	 * To Retrieve the credential info from the processor
	 * 
	 * @return array of CredentialDTO
	 * @throws MailBoxConfigurationServicesException
	 * @throws SymmetricAlgorithmException
	 */
	public CredentialDTO[] getProcessorCredentials()
			throws MailBoxConfigurationServicesException,
			SymmetricAlgorithmException {

		CredentialDTO[] credentialArray = null;

		if (configurationInstance.getCredentials() != null
				&& !configurationInstance.getCredentials().isEmpty()) {

			List<CredentialDTO> credentialsList = new ArrayList<>();
			CredentialDTO credentialDTO = null;
			for (Credential credential : configurationInstance.getCredentials()) {

				credentialDTO = new CredentialDTO();
				credentialDTO.copyFromEntity(credential);
				credentialsList.add(credentialDTO);
			}

			if (credentialsList.size() > 0) {
				credentialArray = Arrays
						.copyOf(credentialsList.toArray(),
								credentialsList.toArray().length,
								CredentialDTO[].class);
			}
		}

		return credentialArray;
	}

	/**
	 * To Retrieve the folder info from the processor
	 * 
	 * @return array of FolderDTO
	 * @throws MailBoxConfigurationServicesException
	 * @throws MailBoxServicesException
	 */
	public FolderDTO[] getProcessorFolders()
			throws MailBoxConfigurationServicesException {

		FolderDTO[] folderArray = null;

		if (configurationInstance.getFolders() != null
				&& !configurationInstance.getFolders().isEmpty()) {

			List<FolderDTO> foldersDTO = new ArrayList<>();
			FolderDTO folderDTO = null;
			for (Folder folder : configurationInstance.getFolders()) {

				folderDTO = new FolderDTO();
				folderDTO.copyFromEntity(folder);
				foldersDTO.add(folderDTO);
			}

			if (!foldersDTO.isEmpty()) {
				folderArray = Arrays.copyOf(foldersDTO.toArray(),
						foldersDTO.toArray().length, FolderDTO[].class);
			}
		}

		return folderArray;
	}

	/**
	 * To Retrieve the Payload file from the URI
	 * 
	 * @return array of files
	 * @throws MailBoxConfigurationServicesException
	 * @throws MailBoxServicesException
	 * @throws IOException
	 */
	public File[] getProcessorPayload() throws MailBoxServicesException,
			IOException {

		File[] files = null;

		if (configurationInstance.getFolders() != null) {

			for (Folder folder : configurationInstance.getFolders()) {

				FolderType foundFolderType = FolderType.findByCode(folder
						.getFldrType());

				if (null == foundFolderType) {
					throw new MailBoxServicesException(
							Messages.FOLDERS_CONFIGURATION_INVALID);
				} else if (FolderType.PAYLOAD_LOCATION.equals(foundFolderType)) {

					LOGGER.debug("Started reading the payload files");
					List<File> result = new ArrayList<>();
					listFiles(processMountLocation(folder.getFldrUri()), result);

					if (!result.isEmpty()) {
						files = Arrays.copyOf(result.toArray(),
								result.toArray().length, File[].class);
						LOGGER.debug("Completed reading the payload files");
					}
				}
			}
		}
		return files;
	}

	/**
	 * To Retrieve the Payload URI
	 * 
	 * @return Payload URI String
	 * @throws MailBoxConfigurationServicesException
	 * @throws MailBoxServicesException
	 */
	protected String getPayloadURI() throws MailBoxServicesException,
			IOException {

		if (configurationInstance.getFolders() != null) {

			for (Folder folder : configurationInstance.getFolders()) {

				FolderType foundFolderType = FolderType.findByCode(folder
						.getFldrType());
				if (null == foundFolderType) {
					throw new MailBoxServicesException(
							Messages.FOLDERS_CONFIGURATION_INVALID);
				} else if (FolderType.PAYLOAD_LOCATION.equals(foundFolderType)) {
					return processMountLocation(folder.getFldrUri());
				}
			}
		}
		return null;
	}

	/**
	 * Get HTTPRequest configurations from processor.
	 * 
	 * @return JSON String containing URL, Verb..etc of the processor
	 */
	public String getClientConfiguration() {

		LOGGER.info("Returns HTTP/S configurations to HTTPClient");
		return configurationInstance.getProcsrProperties();
	}

	/**
	 * Get the URI to which the response should be written, this can be used if
	 * the JS decides to write the response straight to the file system or
	 * database
	 * 
	 * @return URI
	 * @throws MailBoxConfigurationServicesException
	 */
	public String getWriteResponseURI() throws MailBoxServicesException,
			IOException {

		if (configurationInstance.getFolders() != null) {

			for (Folder folder : configurationInstance.getFolders()) {

				FolderType foundFolderType = FolderType.findByCode(folder
						.getFldrType());
				if (null == foundFolderType) {
					throw new MailBoxServicesException(
							Messages.FOLDERS_CONFIGURATION_INVALID);
				} else if (FolderType.RESPONSE_LOCATION.equals(foundFolderType)) {
					return processMountLocation(folder.getFldrUri());
				}
			}
		}
		return null;
	}

	/**
	 * Get the URI to which the mailbox sweeper should be happen
	 * 
	 * @return URI
	 * @throws MailBoxConfigurationServicesException
	 */
	protected Properties getMailBoxProperties() throws MailBoxServicesException {

		if (mailBoxProperties != null) {
			return mailBoxProperties;
		} else {

			mailBoxProperties = new Properties();
			if (null != configurationInstance.getMailbox()
					.getMailboxProperties()) {
				for (MailBoxProperty property : configurationInstance
						.getMailbox().getMailboxProperties()) {
					mailBoxProperties.setProperty(property.getMbxPropName(),
							property.getMbxPropValue());
				}
			}
			return mailBoxProperties;
		}
	}

	/**
	 * Get the credential URI of TrustStore & Keystore to execute the FTPS
	 * uploader/downloader
	 * 
	 * @return String URI
	 * @throws MailBoxServicesException
	 * @throws SymmetricAlgorithmException
	 */
	protected Credential getCredential() throws MailBoxServicesException,
			SymmetricAlgorithmException {

		if (configurationInstance.getCredentials() != null) {

			for (Credential credential : configurationInstance.getCredentials()) {

				CredentialType foundCredentailType = CredentialType
						.findByCode(credential.getCredsType());
				if (null == foundCredentailType) {
					throw new MailBoxServicesException(
							Messages.CREDENTIAL_CONFIGURATION_INVALID);
				} else if (CredentialType.TRUSTSTORE_CERT
						.equals(foundCredentailType)) {
					return credential;

				} else if (CredentialType.SSH_KEYPAIR
						.equals(foundCredentailType)) {
					return credential;
				}
			}
		}
		return null;
	}

	/**
	 * Get the credential URI for login details to execute the FTPS & SFTP
	 * uploader/downloader
	 * 
	 * @return String URI
	 * @throws MailBoxServicesException
	 * @throws SymmetricAlgorithmException
	 */
	protected Credential getUserCredential() throws MailBoxServicesException,
			SymmetricAlgorithmException {

		if (configurationInstance.getCredentials() != null) {

			for (Credential credential : configurationInstance.getCredentials()) {

				CredentialType foundCredentailType = CredentialType
						.findByCode(credential.getCredsType());

				if (null == foundCredentailType) {
					throw new MailBoxServicesException(
							Messages.CREDENTIAL_CONFIGURATION_INVALID);
				} else if (CredentialType.LOGIN_CREDENTIAL
						.equals(foundCredentailType)) {
					return credential;
				}
			}
		}
		return null;
	}

	/**
	 * Get the login details from credentialURI to execute the FTPS & SFTP
	 * uploader/downloader
	 * 
	 * @return String[]
	 * @throws MailBoxServicesException
	 * @throws URISyntaxException
	 */
	protected String[] getUserCredetial(String credentialURI)
			throws URISyntaxException, MailBoxServicesException {

		if (MailBoxUtil.isEmpty(credentialURI)) {
			throw new MailBoxServicesException(
					Messages.CREDENTIAL_CONFIGURATION_INVALID);
		}
		URI uri = new URI(credentialURI);
		String[] userData = null;
		if (MailBoxUtil.isEmpty(uri.getRawAuthority())) {
			throw new MailBoxServicesException(Messages.CREDENTIAL_URI_INVALID);
		}
		userData = uri.getRawAuthority().split("@")[0].split(":");
		return userData;
	}

	/**
	 * call back method to write the response back to MailBox from JS
	 * 
	 * @throws MailBoxServicesException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws FS2Exception
	 */
	public void writeResponseToMailBox(ByteArrayOutputStream response)
			throws URISyntaxException, IOException, FS2Exception,
			MailBoxServicesException {

		LOGGER.info("Started writing response");
		String processorName = MailBoxConstants.PROCESSOR;
		if (configurationInstance.getProcsrName() != null) {
			processorName = configurationInstance.getProcsrName().replaceAll(
					" ", "");
		}
		String fileName = processorName + System.nanoTime();

		writeFileResponseToMailBox(response, fileName);

	}

	/**
	 * call back method to write the file response back to MailBox from JS
	 * 
	 * @throws MailBoxServicesException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws FS2Exception
	 */
	public void writeFileResponseToMailBox(ByteArrayOutputStream response,
			String filename) throws URISyntaxException, IOException,
			FS2Exception, MailBoxServicesException {

		LOGGER.info("Started writing response");
		String responseLocation = getWriteResponseURI();

		if (MailBoxUtil.isEmpty(responseLocation)) {
			throw new MailBoxServicesException(
					Messages.RESPONSE_LOCATION_NOT_CONFIGURED);
		}

		File directory = new File(responseLocation);
		if (!directory.exists()) {
			Files.createDirectories(directory.toPath());
		}

		File file = new File(directory.getAbsolutePath() + File.separatorChar
				+ filename);

		Files.write(file.toPath(), response.toByteArray());

		LOGGER.info("Reponse is successfully written" + file.getAbsolutePath());
	}

	/**
	 * Get the list of dynamic properties of the MailBox known only to java
	 * script
	 * 
	 * @return MailBox dynamic properties
	 */
	public Properties getDynamicProperties() {

		Properties properties = new Properties();

		if (null != configurationInstance.getDynamicProperties()) {

			for (ProcessorProperty property : configurationInstance
					.getDynamicProperties()) {
				properties.setProperty(property.getProcsrPropName(),
						property.getProcsrPropValue());
			}
		}
		return properties;
	}

	/**
	 * Update the dynamic property list of the MailBox known only to java script
	 * 
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * 
	 */
	public void addUpdateDynamicProperty(String dynamicProperties)
			throws JsonParseException, JsonMappingException, JAXBException,
			IOException {

		ProcessorConfigurationService service = new ProcessorConfigurationService();
		DynamicPropertiesDTO dynamicPropertiesDTO = JAXBUtility
				.unmarshalFromJSON(dynamicProperties,
						DynamicPropertiesDTO.class);
		service.addOrUpdateProcessorProperties(
				String.valueOf(configurationInstance.getPrimaryKey()),
				dynamicPropertiesDTO);

	}

	public Object getCertificate() {
		// TODO
		return null;
	}

	/**
	 * Get the decrypted value of a String, known only to java script
	 * 
	 * @param encryptedValue
	 *            The text to be decrypted
	 * @return decrypted data as String
	 */
	public String getDecryptedString(String encryptedValue)
			throws SymmetricAlgorithmException {
		return MailBoxCryptoUtil.doPasswordEncryption(encryptedValue, 2);
	}

	/**
	 * Get HTTPRequest with injected configurations.
	 * 
	 * @return configured HTTPRequest
	 * @throws MailBoxServicesException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws LiaisonException
	 * @throws URISyntaxException
	 * @throws SymmetricAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws JSONException
	 * @throws com.liaison.commons.exception.LiaisonException
	 * @throws CMSException
	 * @throws UnrecoverableKeyException
	 * @throws BootstrapingFailedException
	 */
	public Object getClientWithInjectedConfiguration()
			throws JsonParseException, JsonMappingException, JAXBException,
			IOException, LiaisonException, URISyntaxException,
			MailBoxServicesException, SymmetricAlgorithmException,
			KeyStoreException, NoSuchAlgorithmException, CertificateException,
			JSONException, com.liaison.commons.exception.LiaisonException,
			CMSException, UnrecoverableKeyException, OperatorCreationException,
			BootstrapingFailedException {

		LOGGER.info("Started injecting HTTP/S configurations to HTTPClient");
		// Create HTTPRequest and set the properties
		HTTPRequest request = new HTTPRequest(null, LOGGER);

		// Convert the json string to DTO
		RemoteProcessorPropertiesDTO properties = getRemoteProcessorProperties();

		// Set url to HTTPRequest
		URL url = new URL(properties.getUrl());
		request.setUrl(url);

		// Set configurations
		request.setVersion(properties.getHttpVersion());
		request.setMethod(properties.getHttpVerb());
		request.setNumberOfRetries(properties.getRetryAttempts());
		request.setConnectionTimeout(properties.getConnectionTimeout());
		request.setChunkedEncoding(properties.isChunkedEncoding());

		if (properties.getSocketTimeout() > 0) {
			request.setSocketTimeout(properties.getSocketTimeout());
		}
		if (properties.getPort() > 0) {
			request.setPort(properties.getPort());
		}

		// Set the Other header to HttpRequest
		if (properties.getOtherRequestHeader() != null) {
			for (HttpOtherRequestHeaderDTO header : properties
					.getOtherRequestHeader()) {
				request.addHeader(header.getName(), header.getValue());
			}
		}

		// Set the content type header to HttpRequest
		if (!MailBoxUtil.isEmpty(properties.getContentType())) {
			request.addHeader("Content-Type", properties.getContentType());
		}

		// Set the basic auth header for http request
		Credential loginCredential = getCredentialOfSpecificType(CredentialType.LOGIN_CREDENTIAL);

		if ((loginCredential != null)) {
			if (!MailBoxUtil.isEmpty(loginCredential.getCredsUsername())
					&& !MailBoxUtil.isEmpty(loginCredential.getCredsPassword())) {
				String password = MailBoxCryptoUtil.doPasswordEncryption(
						loginCredential.getCredsPassword(), 2);
				request.setAuthenticationHandler(new BasicAuthenticationHandler(
						loginCredential.getCredsUsername(), password));
			}
		}

		// Configure keystore for HTTPS request
		if (configurationInstance.getProcsrProtocol().equalsIgnoreCase("https")) {

			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			Credential trustStoreCredential = getCredentialOfSpecificType(CredentialType.TRUSTSTORE_CERT);

			if (trustStoreCredential != null) {
				// If no certificate is configured then use default global
				// trustoreid
				String trustStoreID = (MailBoxUtil.isEmpty(trustStoreCredential
						.getCredsIdpUri())) ? (MailBoxUtil
						.getEnvironmentProperties()
						.getString("mailbox.global.trustgroup.id"))
						: trustStoreCredential.getCredsIdpUri();
				InputStream instream = fetchTrustStore(trustStoreID);

				if (instream == null) {
					throw new MailBoxServicesException(
							Messages.CERTIFICATE_RETRIEVE_FAILED);
				}

				try {

					trustStore.load(instream, null);

				} finally {

					if (null != instream)
						instream.close();
				}

				request.truststore(trustStore);
			}

		}
		LOGGER.info("Returns HTTP/S configured HTTPClient");
		return request;

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
	 */
	private InputStream fetchTrustStore(String trustStoreId)
			throws LiaisonException, JSONException, IOException, JAXBException {

		InputStream is = null;

		String url = MailBoxUtil.getEnvironmentProperties().getString(
				"kms-base-url");
		url = url + "fetch/truststore/current/";

		// To be fetched from DataBase
		url = url + trustStoreId;

		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("Content-Type", "application/json");
		LOGGER.info("The KMS URL TO PULL TRUSTSTORE IS " + url);
		String jsonResponse = HTTPClientUtil.getHTTPResponseInString(LOGGER,
				url, headerMap);

		if (jsonResponse != null) {

			 KeyServiceResponse mkr = unmarshalFromJSON(jsonResponse,KeyServiceResponse.class);
		     KeySet keySet = (KeySet) mkr.getDataTransferObject();
			 is =  new ByteArrayInputStream(keySet.getCurrentPublicKey().getBytes());
		}

		return is;
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
	private byte[] fetchSSHPrivateKey(String keypairPguid)
			throws LiaisonException, JSONException, IOException,
			CertificateEncodingException, UnrecoverableKeyException,
			OperatorCreationException, KeyStoreException,
			NoSuchAlgorithmException, CMSException, BootstrapingFailedException, JAXBException {

		byte[] privateKeyBytes = null;

		String url = MailBoxUtil.getEnvironmentProperties().getString(
				"kms-base-url");
		url = url + "fetch/group/keypair/current/";

		// To be fetched from DataBase
		url = url + keypairPguid;

		// get acl manifest response from ACL
		String unsignedData = keypairPguid;
		String signedData = signRequestData(unsignedData);
		ACLManifestResponse aclManifestFromACL = getACLManifestFromACLClient(unsignedData,
				signedData);
		// setting the request headers in the request to key manager from acl manifest response
		Map <String, String> headerMap = getRequestHeaders(aclManifestFromACL);
		String jsonResponse = HTTPClientUtil.getHTTPResponseInString(LOGGER,
				url, headerMap);

		if (jsonResponse != null) {
			
			KeyServiceResponse mkr = unmarshalFromJSON(jsonResponse,KeyServiceResponse.class);
		    KeySet keySet = (KeySet) mkr.getDataTransferObject();
		    privateKeyBytes =  keySet.getCurrentPrivateKey().getBytes();
		}

		return privateKeyBytes;
	}

	/**
	 * Method to read the javascript file as string
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * 
	 */
	protected String getJavaScriptString(String URI) throws IOException,
			URISyntaxException {

		File file = new File(URI);
		if (file.isFile()) { // Added because of this
			// java.nio.file.NotDirectoryException
			String content = FileUtils.readFileToString(file, "UTF-8");
			return content;
		} else {

			StringBuffer buffer = new StringBuffer();
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths
					.get(URI))) {
				for (Path entry : stream) {
					String content = FileUtils.readFileToString(entry.toFile(),
							"UTF-8");
					buffer.append(content);
				}
			} catch (IOException e) {
				throw e;
			}
			return buffer.toString();
		}
	}

	/**
	 * Method to list file from folder
	 * 
	 * @throws MailBoxServicesException
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * 
	 */
	private void listFiles(String path, List<File> files)
			throws MailBoxServicesException {

		if (MailBoxUtil.isEmpty(path)) {
			LOGGER.info("The given URI {} does not exist.", path);
			throw new MailBoxServicesException("The given URI '" + path
					+ "' does not exist.");
		}

		// Modified to support both file and directory.
		File location = new File(path);
		if (location.isFile()) {

			if (location.exists()) {
				files.add(location);
			} else {
				LOGGER.info("The given file {} does not exist.", path);
				throw new MailBoxServicesException("The given file '" + path
						+ "' does not exist.");
			}

		} else {

			if (!location.exists()) {
				LOGGER.info("The given directory {} does not exist.", path);
				throw new MailBoxServicesException("The given directory '"
						+ path + "' does not exist.");
			} else {

				// get all the files from a directory
				for (File file : location.listFiles()) {

					if (file.isFile()) {
						if (!MailBoxConstants.META_FILE_NAME.equals(file
								.getName())) {
							files.add(file);
						}
					} else if (file.isDirectory()) { // get all files from inner
														// directory.
						if (!MailBoxConstants.PROCESSED_FOLDER.equals(file
								.getName())) {
							listFiles(file.getAbsolutePath(), files);
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a filter for directories only.
	 * 
	 * @return Object which implements DirectoryStream.Filter interface and that
	 *         accepts directories only.
	 */
	public DirectoryStream.Filter<Path> defineFilter(
			final boolean listDirectoryOnly) {

		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {

			@Override
			public boolean accept(Path entry) throws IOException {

				return listDirectoryOnly ? Files.isDirectory(entry) : Files
						.isRegularFile(entry);
			}
		};

		return filter;
	}

	/**
	 * Method is used to move the file to the processed folder.
	 * 
	 * @param filePath
	 *            The source location
	 * @throws IOException
	 */
	public void archiveFile(String filePath, boolean isError)
			throws IOException {

		File file = new File(filePath);
		String targetFolder = (isError) ? MailBoxConstants.ERROR_FOLDER
				: MailBoxConstants.PROCESSED_FOLDER;
		Path targetDirectory = file.toPath().getParent().resolve(targetFolder);
		if (!Files.exists(targetDirectory)) {
			LOGGER.info("Creating target(processed/error) folder");
			Files.createDirectories(targetDirectory);
		}
		Path target = targetDirectory.resolve(file.getName());
		// moving to processed/error folder
		Files.move(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * Method is used to move the files to the processed folder.
	 * 
	 * @param filePath
	 *            The source location
	 * @throws IOException
	 */
	protected void archiveFiles(File[] files, boolean isError)
			throws IOException {
		for (File file : files) {
			archiveFile(file.getAbsolutePath(), isError);
		}
	}

	/**
	 * Method is used to move the uploaded file to the given folder.
	 * 
	 * @param file
	 *            File to be moved
	 * @param processedFileLcoation
	 *            The source location
	 * @throws IOException
	 */
	protected void archiveFile(File file, String processedFileLcoation)
			throws IOException {

		Path oldPath = null;
		Path newPath = null;

		oldPath = Paths.get(file.toURI());
		newPath = Paths.get(processedFileLcoation).resolve(file.getName());

		if (!Files.exists(newPath.getParent())) {
			Files.createDirectories(newPath.getParent());
		}

		Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * Method is used to move the uploaded file to the given folder.
	 * 
	 * @param filePath
	 *            The source location
	 * @throws IOException
	 */
	protected void archiveFiles(File[] files, String processedFileLcoation)
			throws IOException {

		for (File file : files) {
			archiveFile(file, processedFileLcoation);
		}
	}

	/**
	 * Method is used to modify the status during failure.
	 */
	protected void modifyProcessorExecutionStatus(ExecutionState status) {

		configurationInstance.setProcsrExecutionStatus(status.value());
		PROCESSOR_DAO.merge(configurationInstance);

	}

	/**
	 * Use to validate the given file can be added in the given group.
	 * 
	 * @param fileGroup
	 *            The file attributes group
	 * @param fileAttribute
	 *            The file attribute to be added in the group
	 * @return true if it can be added false otherwise
	 * @throws MailBoxServicesException
	 */
	protected Boolean validateAdditionalGroupFile(
			List<FileAttributesDTO> fileGroup, FileAttributesDTO fileAttribute)
			throws MailBoxServicesException {

		long maxPayloadSize = 0;
		long maxNoOfFiles = 0;

		try {

			String payloadSize = getDynamicProperties().getProperty(
					MailBoxConstants.PAYLOAD_SIZE_THRESHOLD);
			String maxFile = getDynamicProperties().getProperty(
					MailBoxConstants.NUMER_OF_FILES_THRESHOLD);
			if (!MailBoxUtil.isEmpty(payloadSize)) {
				maxPayloadSize = Long.parseLong(payloadSize);
			}
			if (!MailBoxUtil.isEmpty(maxFile)) {
				maxNoOfFiles = Long.parseLong(maxFile);
			}

		} catch (NumberFormatException e) {
			throw new MailBoxServicesException(
					"The given threshold size is not a valid one.");
		}

		if (maxPayloadSize == 0) {
			maxPayloadSize = 131072;
		}
		if (maxNoOfFiles == 0) {
			maxNoOfFiles = 10;
		}

		if (maxNoOfFiles <= fileGroup.size()) {
			return false;
		}

		if (maxPayloadSize <= (getGroupFileSize(fileGroup) + fileAttribute
				.getSize())) {
			return false;
		}
		return true;
	}

	/**
	 * Get the total file size of the group.
	 * 
	 * @param fileGroup
	 * @return
	 */
	private long getGroupFileSize(List<FileAttributesDTO> fileGroup) {

		long size = 0;

		for (FileAttributesDTO attribute : fileGroup) {
			size += attribute.getSize();
		}

		return size;
	}

	/**
	 * Get FTPSRequest with injected configurations.
	 * 
	 * @return configured FTPSRequest
	 * @throws MailBoxServicesException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws LiaisonException
	 * @throws URISyntaxException
	 * @throws SymmetricAlgorithmException
	 * @throws com.liaison.commons.exception.LiaisonException
	 * @throws JSONException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CMSException
	 * @throws OperatorCreationException
	 * @throws UnrecoverableKeyException
	 * @throws BootstrapingFailedException
	 */
	@SuppressWarnings("unused")
	protected G2FTPSClient getFTPSClient(Logger logger)
			throws LiaisonException, JsonParseException, JsonMappingException,
			JAXBException, IOException, URISyntaxException,
			MailBoxServicesException, SymmetricAlgorithmException,
			com.liaison.commons.exception.LiaisonException, JSONException,
			NoSuchAlgorithmException, CertificateException, KeyStoreException,
			UnrecoverableKeyException, OperatorCreationException, CMSException,
			BootstrapingFailedException {
		// Convert the json string to DTO
		RemoteProcessorPropertiesDTO properties = MailBoxUtil
				.unmarshalFromJSON(configurationInstance.getProcsrProperties(),
						RemoteProcessorPropertiesDTO.class);

		G2FTPSClient ftpsRequest = new G2FTPSClient();
		ftpsRequest.setURI(properties.getUrl());
		ftpsRequest.setDiagnosticLogger(logger);
		ftpsRequest.setCommandLogger(logger);
		ftpsRequest.setConnectionTimeout(properties.getConnectionTimeout());

		ftpsRequest.setSocketTimeout(properties.getSocketTimeout());
		ftpsRequest.setRetryCount(properties.getRetryAttempts());

		Credential loginCredential = getCredentialOfSpecificType(CredentialType.LOGIN_CREDENTIAL);

		/*
		 * For FTPS, SFTP, and FTP processors credential password will be
		 * getting from KM
		 */
		String passwordFromKMS = null;
		if ((loginCredential != null)) {

			// get acl manifest response from ACL
			String unsignedData = loginCredential.getCredsPassword();
			String signedData = signRequestData(unsignedData);
			ACLManifestResponse aclManifestFromACL = getACLManifestFromACLClient(
					unsignedData, signedData);
			// setting the request headers in the request to key manager from acl manifest response
			Map <String, String> headerMap = getRequestHeaders(aclManifestFromACL);
			String url = MailBoxUtil.getEnvironmentProperties().getString(
					"kms-base-url")
					+ "secret/" + loginCredential.getCredsPassword();
			String base64EncodedPassword = HTTPClientUtil
					.getHTTPResponseInString(LOGGER, url, headerMap);

			if (base64EncodedPassword != null || base64EncodedPassword != "") {
				String decodeLevel1 = new String(
						Base64.decodeBase64(base64EncodedPassword));
				String base64DecodedPassword = new String(
						Base64.decodeBase64(decodeLevel1));
				passwordFromKMS = base64DecodedPassword;
			} else {
				throw new MailBoxServicesException(Messages.READ_SECRET_FAILED);
			}

			if (!MailBoxUtil.isEmpty(loginCredential.getCredsUsername())
					&& !MailBoxUtil.isEmpty(passwordFromKMS)) {
				// String password =
				// MailBoxCryptoUtil.doPasswordEncryption(loginCredential.getCredsPassword(),
				// 2);
				ftpsRequest.setUser(loginCredential.getCredsUsername());
				ftpsRequest.setPassword(passwordFromKMS);
			}
		}

		// Configure keystore for HTTPS request
		if (configurationInstance.getProcsrProtocol().equalsIgnoreCase("ftps")) {

			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			Credential trustStoreCredential = getCredentialOfSpecificType(CredentialType.TRUSTSTORE_CERT);
			if (trustStoreCredential != null) {
				// If no certificate is configured then use default global
				// trustoreid
				String trustStoreID = (MailBoxUtil.isEmpty(trustStoreCredential
						.getCredsIdpUri())) ? (MailBoxUtil
						.getEnvironmentProperties()
						.getString("mailbox.global.trustgroup.id"))
						: trustStoreCredential.getCredsIdpUri();
				InputStream instream = fetchTrustStore(trustStoreID);

				if (instream == null) {
					throw new MailBoxServicesException(
							Messages.CERTIFICATE_RETRIEVE_FAILED);
				}

				try {

					trustStore.load(instream, null);

				} finally {
					if (null != instream)
						instream.close();
				}

				ftpsRequest.setTrustStore(trustStore);
			}

		}
		return ftpsRequest;
	}

	/**
	 * Get SFTPRequest with injected configurations.
	 * 
	 * @return configured SFTPRequest
	 * @throws MailBoxServicesException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws LiaisonException
	 * @throws URISyntaxException
	 * @throws SymmetricAlgorithmException
	 * @throws com.liaison.commons.exception.LiaisonException
	 * @throws JSONException
	 * @throws CMSException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws OperatorCreationException
	 * @throws UnrecoverableKeyException
	 * @throws CertificateEncodingException
	 * @throws BootstrapingFailedException
	 */
	@SuppressWarnings("unused")
	protected G2SFTPClient getSFTPClient(Logger logger)
			throws JsonParseException, JsonMappingException, JAXBException,
			IOException, LiaisonException, URISyntaxException,
			MailBoxServicesException, SymmetricAlgorithmException,
			com.liaison.commons.exception.LiaisonException, JSONException,
			CertificateEncodingException, UnrecoverableKeyException,
			OperatorCreationException, KeyStoreException,
			NoSuchAlgorithmException, CMSException, BootstrapingFailedException {

		RemoteProcessorPropertiesDTO properties = MailBoxUtil
				.unmarshalFromJSON(configurationInstance.getProcsrProperties(),
						RemoteProcessorPropertiesDTO.class);

		G2SFTPClient sftpRequest = new G2SFTPClient();
		sftpRequest.setURI(properties.getUrl());
		sftpRequest.setDiagnosticLogger(logger);
		sftpRequest.setCommandLogger(logger);
		sftpRequest.setTimeout(properties.getConnectionTimeout());
		sftpRequest.setStrictHostChecking(false);
		sftpRequest.setRetryInterval(properties.getRetryInterval());
		sftpRequest.setRetryCount(properties.getRetryAttempts());

		Credential loginCredential = getCredentialOfSpecificType(CredentialType.LOGIN_CREDENTIAL);

		String passwordFromKMS = null;
		if ((loginCredential != null)) {

			// get acl manifest response from ACL
			String unsignedData = loginCredential.getCredsPassword();
			String signedData = signRequestData(unsignedData);
			ACLManifestResponse aclManifestFromACL = getACLManifestFromACLClient(
					unsignedData, signedData);
			// setting the request headers in the request to key manager from acl manifest response
			Map <String, String> headerMap = getRequestHeaders(aclManifestFromACL);
			String url = MailBoxUtil.getEnvironmentProperties().getString(
					"kms-base-url")
					+ "secret/" + loginCredential.getCredsPassword();
			String base64EncodedPassword = HTTPClientUtil
					.getHTTPResponseInString(LOGGER, url, headerMap);

			if (base64EncodedPassword != null || base64EncodedPassword != "") {
				String decodeLevel1 = new String(
						Base64.decodeBase64(base64EncodedPassword));
				String base64DecodedPassword = new String(
						Base64.decodeBase64(decodeLevel1));
				passwordFromKMS = base64DecodedPassword;
			} else {
				throw new MailBoxServicesException(Messages.READ_SECRET_FAILED);
			}

			if (!MailBoxUtil.isEmpty(loginCredential.getCredsUsername())) {
				sftpRequest.setUser(loginCredential.getCredsUsername());

			}
			if (!MailBoxUtil.isEmpty(passwordFromKMS)) {
				// String password =
				// MailBoxCryptoUtil.doPasswordEncryption(loginCredential.getCredsPassword(),
				// 2);
				sftpRequest.setPassword(passwordFromKMS);
			}
		}
		Credential sshKeyPairCredential = getCredentialOfSpecificType(CredentialType.SSH_KEYPAIR);

		if (sshKeyPairCredential != null) {

			if (MailBoxUtil.isEmpty(sshKeyPairCredential.getCredsIdpUri())) {

				LOGGER.info("Credential requires file path");
				throw new MailBoxServicesException(
						"Credential requires file path");
			}

			byte[] privateKeyStream = fetchSSHPrivateKey(sshKeyPairCredential
					.getCredsIdpUri());

			if (privateKeyStream == null) {
				throw new MailBoxServicesException(
						Messages.SSHKEY_RETRIEVE_FAILED);
			}

			String privateKeyPath = MailBoxUtil.getEnvironmentProperties()
					.getString("ssh.private.key.temp.location")
					+ sshKeyPairCredential.getCredsUri() + ".txt";
			// write to a file
			FileOutputStream out = new FileOutputStream(privateKeyPath);
			out.write(privateKeyStream);
			out.close();
			sftpRequest.setPrivateKeyPath(privateKeyPath);
			// sftpRequest.setPassphrase(sshKeyPairCredential.getCredsPassword());

		}

		return sftpRequest;
	}

	protected RemoteProcessorPropertiesDTO getRemoteProcessorProperty()
			throws JsonParseException, JsonMappingException, JAXBException,
			IOException {

		RemoteProcessorPropertiesDTO properties = MailBoxUtil
				.unmarshalFromJSON(configurationInstance.getProcsrProperties(),
						RemoteProcessorPropertiesDTO.class);
		return properties;
	}

	/**
	 * Method is used to get the CredentialInfo model for getting login username
	 * & password.
	 * 
	 * @throws SymmetricAlgorithmException
	 * @throws URISyntaxException
	 * @throws MailBoxServicesException
	 */
	protected CredentialInfoModel getLoginCredentials()
			throws URISyntaxException, MailBoxServicesException,
			SymmetricAlgorithmException {

		Credential userCredential = getUserCredential();
		CredentialInfoModel model = null;
		if (userCredential != null) {
			model = new CredentialInfoModel();

			if (!MailBoxUtil.isEmpty(userCredential.getCredsUsername())
					&& !MailBoxUtil.isEmpty(userCredential.getCredsPassword())) {

				model.setUsername(userCredential.getCredsUsername());
				model.setPassword(userCredential.getCredsPassword());

			} else if (!MailBoxUtil.isEmpty(userCredential.getCredsIdpUri())) {

				String[] cred = getUserCredetial(userCredential
						.getCredsIdpUri());
				if (cred != null) {

					if (MailBoxUtil.isEmpty(cred[0])
							|| MailBoxUtil.isEmpty(cred[1])) {
						LOGGER.info("Credential idpuri requires username & password");
						throw new MailBoxServicesException(
								"Credential IDPURI requires username & password");
					}
					model.setUsername(cred[0]);
					model.setPassword(cred[1]);
				}
			} else {

				LOGGER.info("Credentials not configured");
				throw new MailBoxServicesException("Credentials not configured");
			}
		}
		return model;
	}

	/**
	 * Method is used to get the CredentialInfoModel for getting keystore
	 * credentials
	 * 
	 * @throws SymmetricAlgorithmException
	 * @throws URISyntaxException
	 * @throws MailBoxServicesException
	 */
	protected CredentialInfoModel getKeyStoreCredential()
			throws URISyntaxException, MailBoxServicesException,
			SymmetricAlgorithmException {

		Credential credential = getCredential();
		CredentialInfoModel model = null;
		if (credential != null) {

			model = new CredentialInfoModel();
			if (!MailBoxUtil.isEmpty(credential.getCredsPassword())) {

				model.setPassword(credential.getCredsPassword());
				model.setFileURI(credential.getCredsUri());

			} else if (!MailBoxUtil.isEmpty(credential.getCredsIdpUri())) {

				URI uri = new URI(credential.getCredsIdpUri());
				model.setPassword(getUserCredetial(credential.getCredsIdpUri())[1]);
				model.setFileURI(uri.getPath());

			} else {
				LOGGER.info("Credentials not configured for TrustStore/keystore");
				throw new MailBoxServicesException(
						"Credentials not configured TrustStore/keystore");
			}
		}
		return model;
	}

	/**
	 * Method is used to write the file to the response folder for SFTP/FTP(S).
	 * 
	 * @param response
	 *            Bytearray response
	 * @param filename
	 *            The source location
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws FS2Exception
	 * @throws MailBoxServicesException
	 */
	protected void writeSFTPSResponseToMailBox(ByteArrayOutputStream response,
			String filename) throws URISyntaxException, IOException,
			FS2Exception, MailBoxServicesException {

		LOGGER.info("Started writing response");

		String responseLocation = getWriteResponseURI();
		if (MailBoxUtil.isEmpty(responseLocation)) {
			throw new MailBoxServicesException(
					Messages.RESPONSE_LOCATION_NOT_CONFIGURED);
		}

		File directory = new File(responseLocation);
		if (!directory.exists()) {
			Files.createDirectories(directory.toPath());
		}

		if (MailBoxUtil.isEmpty(filename)) {

			LOGGER.info("The given URI {} does not exist.", filename);
			throw new MailBoxServicesException("The given URI '" + filename
					+ "' does not exist.");
		}
		File file = new File(filename);
		Files.write(file.toPath(), response.toByteArray());
		LOGGER.info("Reponse is successfully written" + file.getAbsolutePath());
	}

	/**
	 * Method is used to process the folder path given by user and replace the
	 * mount location with proper value form properties file.
	 * 
	 * @param folderPath
	 *            The folder path given by user
	 * 
	 * @return processedFolderPath The folder path with mount location
	 * 
	 */
	protected String processMountLocation(String folderPath) throws IOException {

		String processedFolderPath = null;

		if (folderPath != null
				&& folderPath.toUpperCase().contains(
						MailBoxConstants.MOUNT_LOCATION)) {
			String mountLocationValue = MailBoxUtil.getEnvironmentProperties()
					.getString("MOUNT_POINT");
			processedFolderPath = folderPath
					.replaceAll(MailBoxConstants.MOUNT_LOCATION_PATTERN,
							mountLocationValue);
		} else {
			return folderPath;
		}
		LOGGER.info("The Processed Folder Path is" + processedFolderPath);
		return processedFolderPath;
	}

	/**
	 * Sent notifications for trigger system failure.
	 * 
	 * @param toEmailAddrList
	 *            The extra receivers. The default receiver will be available in
	 *            the mailbox.
	 * @param subject
	 *            The notification subject
	 * @param emailBody
	 *            The body of the notification
	 * @param type
	 *            The notification type(TEXT/HTML).
	 */

	public void sendEmail(List<String> toEmailAddrList, String subject,
			String emailBody, String type) {

		List<String> configuredEmailAddress = configurationInstance
				.getEmailAddress();
		if ((configuredEmailAddress == null || configuredEmailAddress.isEmpty())
				&& (toEmailAddrList == null || toEmailAddrList.isEmpty())) {
			LOGGER.info("There is no email address configured for this mailbox.");
		}

		if (null != configuredEmailAddress && null != toEmailAddrList) {
			toEmailAddrList.addAll(configuredEmailAddress);
		} else if (null != configuredEmailAddress) {
			toEmailAddrList = configuredEmailAddress;
		}

		EmailNotifier notifier = new EmailNotifier();
		notifier.sendEmail(toEmailAddrList, subject, emailBody, type);
	}

	/**
	 * Sent notifications for trigger system failure.
	 * 
	 * @param toEmailAddrList
	 *            The extra receivers. The default receiver will be available in
	 *            the mailbox.
	 * @param subject
	 *            The notification subject
	 * @param exc
	 *            The exception as body content
	 * @param type
	 *            The notification type(TEXT/HTML).
	 */
	public void sendEmail(List<String> toEmailAddrList, String subject,
			Exception exc, String type) {

		sendEmail(toEmailAddrList, subject, ExceptionUtils.getStackTrace(exc),
				type);
	}

	/**
	 * Get the credential Details configured for a processor
	 * 
	 * 
	 * @return String URI
	 * @throws MailBoxServicesException
	 * @throws SymmetricAlgorithmException
	 */
	protected Credential getCredentialOfSpecificType(CredentialType type)
			throws MailBoxServicesException, SymmetricAlgorithmException {

		if (configurationInstance.getCredentials() != null) {

			for (Credential credential : configurationInstance.getCredentials()) {
				CredentialType foundCredentailType = CredentialType
						.findByCode(credential.getCredsType());
				if (credential.getCredsType() == null) {
					throw new MailBoxServicesException(
							Messages.CREDENTIAL_CONFIGURATION_INVALID);
				} else if (foundCredentailType.equals(type)) {

					if (credential.getCredsType().equalsIgnoreCase(
							MailBoxConstants.SSH_KEYPAIR)
							&& credential.getCredsIdpType().equalsIgnoreCase(
									"PRIVATE")) {
						return credential;
					}
					if (!credential.getCredsIdpType().equalsIgnoreCase(
							MailBoxConstants.SSH_KEYPAIR))
						return credential;
				}
			}
		}
		return null;
	}

	/**
	 * Method is used to process the response location and create folders if not
	 * already exists for SFTP/FTP(S).
	 * 
	 * @param filename
	 *            The source location
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws MailBoxServicesException
	 */
	protected void processResponseLocation(String fileName)
			throws URISyntaxException, IOException, MailBoxServicesException {

		LOGGER.info("Started writing response");

		String responseLocation = getWriteResponseURI();
		if (MailBoxUtil.isEmpty(responseLocation)) {
			throw new MailBoxServicesException(
					Messages.RESPONSE_LOCATION_NOT_CONFIGURED);
		}

		File directory = new File(responseLocation);
		if (!directory.exists()) {
			Files.createDirectories(directory.toPath());
		}

		if (MailBoxUtil.isEmpty(fileName)) {

			LOGGER.info("The given URI {} does not exist.", fileName);
			throw new MailBoxServicesException("The given URI '" + fileName
					+ "' does not exist.");
		}
	}

	/**
	 * Method used to remove the privatekey downloaded from keymanager once
	 * successfully authenticated using key
	 * 
	 * @param fileLocation
	 * @throws IOException
	 * @throws SymmetricAlgorithmException
	 * @throws MailBoxServicesException
	 */
	protected void removePrivateKey() throws IOException,
			MailBoxServicesException, SymmetricAlgorithmException {

		LOGGER.info("Trigerring - Remove privateKey downloaded from keyManager");
		Credential sshKeyPairCredential = getCredentialOfSpecificType(CredentialType.SSH_KEYPAIR);
		if (sshKeyPairCredential.getCredsUri() != null) {
			String fileLocation = MailBoxUtil.getEnvironmentProperties()
					.getString("ssh.private.key.temp.location")
					+ sshKeyPairCredential.getCredsUri() + ".txt";
			File privateKeyFile = new File(fileLocation);
			if (privateKeyFile.exists())
				privateKeyFile.delete();
			LOGGER.info("privateKey downloaded from keyManager removed from local file system");
			return;
		}

		LOGGER.info("Trigerring - The private key file path not configured.");
	}

	/**
	 * Method used to construct the ACLManifestRequest
	 * 
	 * @return ManifestRequestDTO
	 */
	private ManifestRequestDTO constructACLManifestRequest() {
		
		LOGGER.info("Constructing the acl manifest request with default values");
		// Construct Envelope
		EnvelopeDTO envelope = new EnvelopeDTO();
		envelope.setGlobalId("963258741");
		envelope.setParentId("7888");
		envelope.setId("564");

		// Construct Domain
		ManifestRequestDomain domain = new ManifestRequestDomain();
		domain.setName("BOEING");
		domain.setType("ORGANIZATION");
		List<String> roles = new ArrayList<String>();
		roles.add("MailboxAdmin");
		domain.setRoles(roles);

		List<ManifestRequestDomain> domains = new ArrayList<ManifestRequestDomain>();
		domains.add(domain);

		// Construct NestedServiceDependency
		NestedServiceDependencyContraint dependencyConstraint = new NestedServiceDependencyContraint();
		dependencyConstraint.setServiceName("MAILBOX");
		dependencyConstraint.setPrimaryId(configurationInstance
				.getServiceInstance().getName());

		List<NestedServiceDependencyContraint> constraintList = new ArrayList<NestedServiceDependencyContraint>();
		constraintList.add(dependencyConstraint);

		// Construct Platform
		ManifestRequestPlatform platform = new ManifestRequestPlatform();
		platform.setName("SERVICE_BROKER");
		platform.setConstraintList(constraintList);
		platform.setDomains(domains);

		List<ManifestRequestPlatform> platforms = new ArrayList<ManifestRequestPlatform>();
		platforms.add(platform);

		// Construct ManifestRequestACL
		ManifestRequestACL manifestRequestACL = new ManifestRequestACL();
		manifestRequestACL.setEnvelope(envelope);
		manifestRequestACL.setPlatforms(platforms);

		// Construct ManifestRequestDTO
		ManifestRequestDTO manifestRequest = new ManifestRequestDTO();
		manifestRequest.setAcl(manifestRequestACL);

		return manifestRequest;
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
	private String signRequestData(String unsignedData)
			throws CertificateEncodingException, OperatorCreationException,
			CMSException, IOException, KeyStoreException,
			UnrecoverableKeyException, NoSuchAlgorithmException,
			BootstrapingFailedException {

		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		}
		char[] password = null;
		KeyStore ks = null;
		// read keystore from Bootstrap
		// BootstrapRemoteKeystore.bootstrap();
		password = BootstrapRemoteKeystore
				.getDecryptedRemoteKeypairPassphrase().toCharArray();
		LOGGER.debug("Loading keystore from Bootstrap");
		ks = BootstrapRemoteKeystore.getRemoteKeyStore();

		X509Certificate originalSignerCert = KeyStoreUtil
				.getX509Certificate(ks);
		PrivateKey privateKey = (PrivateKey) ks.getKey(
				KeyStoreUtil.getKeyAlias(ks), password);
		List<X509Certificate> listOfOriginalSignerCerts = new ArrayList<>();
		listOfOriginalSignerCerts.add(originalSignerCert);

		// FOR SIGNING
		DigitalSignature sig = new DigitalSignature();
		byte[] MESSAGE_TO_SIGN = unsignedData.getBytes();
		byte[] singedData = sig.sign(new ByteArrayInputStream(MESSAGE_TO_SIGN),
				originalSignerCert, privateKey);

		return Base64.encodeBase64String(singedData);

	}

	/**
	 * Method used to retrieve the acl manifest from ACLClient
	 * 
	 * @param unsignedData
	 * @param signedData
	 * @return String aclManifest
	 * @throws IOException
	 */
	private ACLManifestResponse getACLManifestFromACLClient(String unsignedData,
			String signedData) throws IOException {
		ACLManifestResponse aclManifestResponse = null;
		ACLClient aclClient = new ACLClient();

		LOGGER.debug("Entering the getACLManifestFromACLClient method.");
		// construct acl manifset request
		ManifestRequestDTO manifestRequest = constructACLManifestRequest();

		LOGGER.info("Read public key guid used to sign the unsigned data from properies file");
		// read the public key guid from properties file
		String publicKeyGuid = MailBoxUtil.getEnvironmentProperties()
				.getString("mailbox.signer.public.key.guid");
		LOGGER.info("Retrieving acl manifest form ACL using ACLClient.");
		// get aclManifest response through ACLClient
		aclManifestResponse = aclClient.getACLManifest(unsignedData,
				signedData, publicKeyGuid, manifestRequest);
		return aclManifestResponse;
	}
	
	/**
	 * Method to set the request header for the requests to key manager
	 * 
	 * @param aclManifestResponse
	 * @return requestHeaders in a Map object
	 */
	private Map <String, String> getRequestHeaders(ACLManifestResponse aclManifestFromACL) {
		
		LOGGER.info("setting request headers from acl manifest response to key manager request");
		Map<String, String> headerMap = new HashMap<String, String>();
		String aclManifest = (aclManifestFromACL != null)? aclManifestFromACL.getAclManifest():null;
		String signedACLManifest = (aclManifestFromACL != null)? aclManifestFromACL.getSignature():null;
		String aclSignerPublicKey = (aclManifestFromACL != null)?aclManifestFromACL.getPublicKeyGuid():null;
		headerMap.put("acl-manifest", aclManifest);
		headerMap.put("acl-signature", signedACLManifest);
		headerMap.put("acl_signer_public_key_guid", aclSignerPublicKey);
		headerMap.put("Content-Type", "application/json");
		return headerMap;
	}
	
	/**
     * 
     * @param serializedJson
     * @param clazz
     * 
     * @return json
     * 
     * @throws JAXBException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    private   <T> T unmarshalFromJSON(String serializedJson, Class<T> clazz) throws JAXBException, JsonParseException,
    JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector primary = new JaxbAnnotationIntrospector();
        AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector introspector = new AnnotationIntrospector.Pair(
        primary, secondary);
        // make deserializer use JAXB annotations (only)
        mapper.getDeserializationConfig().setAnnotationIntrospector(introspector);
        T ummarshaledObject = (T) mapper.readValue(serializedJson, clazz);
        return ummarshaledObject;
    }

}
