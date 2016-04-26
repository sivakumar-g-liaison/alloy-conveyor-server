/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.services.unit.test;

import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorLegacyDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;

/**
 * Class to test the ProcessorLegacyDTO.
 * 
 * @author OFS
 */
public class ProcessorLegacyDTOTest {

	private final static String USER_NAME = "g2testusr";
	
	private final static String PASSWORD = "Password@12345";
	
	private final static String VALID_SSHKEYPAIR = "254AD0C664B44B198EE736BD89509444";
	
	private final static String VALID_TRUSTSTORE = "4463C90421854F23876C11A7A39FA41F";
	
	private final static String INVALID_GUID = "1B639D47BB734094AE7B26B122C00282";
	
	/**
	 * @throws Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
        System.setProperty("com.liaison.secure.properties.path", "invalid");
        System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
        System.setProperty("archaius.deployment.environment", "dev-int");
	}
	
	/**
	 * Test method to validate the userId and Password.
	 */
	@Test(enabled=false)
	public void testValidateFTPUserIdCredential() {
		
		Processor processor = constructProcessor();
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setUserId(USER_NAME);
		credentialDTO.setPassword(PASSWORD);
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		
		List<CredentialDTO> credentials = new ArrayList<CredentialDTO>();
		credentials.add(credentialDTO);
		ProcessorDTO processorDTO = new ProcessorDTO();
		processorDTO.validateCredentials(processor.getProcessorType(), Protocol.FTP.name(), credentialDTO, credentials);
	}
	
	/**
	 * Test method for FTP to validate the empty userId.
	 */
	@Test (expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Username cannot be Empty." )

	public void testValidateFTPEmptyUserIdCredential() {
		
		Processor processor = constructProcessor();
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		
		List<CredentialDTO> credentials = new ArrayList<CredentialDTO>();
		credentials.add(credentialDTO);
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor.getProcessorType(), Protocol.FTP.name(), credentialDTO, credentials);
	}
	
	/**
	 * Test method for FTP to validate the empty password.
	 */

	@Test (expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Password cannot be Empty." )
	public void testValidateFTPEmptyPasswordCredential() {
		
		Processor processor = constructProcessor();
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setUserId(USER_NAME);
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());

		List<CredentialDTO> credentials = new ArrayList<CredentialDTO>();
		credentials.add(credentialDTO);
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor.getProcessorType(), Protocol.FTP.name(), credentialDTO ,credentials);
	}
	
	/**
	 * Test method for FTPS to validate the empty userId.
	 */

	@Test (expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Username cannot be Empty." )
	public void testValidateFTPSEmptyUserIdCredential() {
		
		Processor processor = constructProcessor();
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		
		List<CredentialDTO> credentials = new ArrayList<CredentialDTO>();
		credentials.add(credentialDTO);
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor.getProcessorType(), Protocol.FTPS.name(), credentialDTO, credentials);
	}
	
	/**
	 * Test method for FTPS to validate the empty password.
	 */
	@Test (expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Password cannot be Empty." )
	public void testValidateFTPSEmptyPasswordCredential() {
		
		Processor processor = constructProcessor();
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setUserId(USER_NAME);
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		
		List<CredentialDTO> credentials = new ArrayList<CredentialDTO>();
		credentials.add(credentialDTO);
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor.getProcessorType(), Protocol.FTPS.name(), credentialDTO, credentials);
	}
	
	/**
	 * Test method for HTTPS to validate the empty userId.
	 */
	@Test (expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Username cannot be Empty." )
	public void testValidateHTTPSEmptyUserIdCredential() {
		
		Processor processor = constructProcessor();
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		
		List<CredentialDTO> credentials = new ArrayList<CredentialDTO>();
		credentials.add(credentialDTO);
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor.getProcessorType(), Protocol.HTTPS.name(), credentialDTO, credentials);
	}
	
	/**
	 * Test method for HTTPS to validate the empty password.
	 */
	@Test (expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Password cannot be Empty." )
	public void testValidateHTTPSEmptyPasswordCredential() {
		
		Processor processor = constructProcessor();
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setUserId(USER_NAME);
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		
		List<CredentialDTO> credentials = new ArrayList<CredentialDTO>();
		credentials.add(credentialDTO);
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor.getProcessorType(), Protocol.HTTPS.name(), credentialDTO, credentials);
	}
	
	/**
	 * Test method for SFTP to validate the empty userId.
	 */
	@Test (expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Username cannot be Empty." )
	public void testValidateSFTPEmptyUserIdCredential() {
		
		Processor processor = constructProcessor();
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		
		List<CredentialDTO> credentials = new ArrayList<CredentialDTO>();
		credentials.add(credentialDTO);
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor.getProcessorType(), Protocol.SFTP.name(), credentialDTO, credentials);
	}
	
	/**
	 * Test method for SFTP to validate the empty password.
	 */
	@Test (expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Either the Password or the SSH Keypair is Empty." )
	public void testValidateSFTPEmptyPasswordCredential() {
		
		Processor processor = constructProcessor();
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setUserId(USER_NAME);
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		
		List<CredentialDTO> credentials = new ArrayList<CredentialDTO>();
		credentials.add(credentialDTO);
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor.getProcessorType(), Protocol.SFTP.name(), credentialDTO, credentials);
	}
	
	/**
	 * Test method to check the valid SSH keypair.
	 */
	@Test
	public void testisSSHKeyPairAvailable() {
		
		List<CredentialDTO> credentials = new ArrayList<CredentialDTO>();
		CredentialDTO credential = new CredentialDTO();
		credential.setCredentialType(CredentialType.SSH_KEYPAIR.getCode());
		credential.setIdpURI(VALID_SSHKEYPAIR);
		credentials.add(credential);
		
		ProcessorDTO processorDTO = new ProcessorDTO();
		Assert.assertTrue(processorDTO.isSSHKeyPairAvailable(credentials));
	}
	
	/**
	 * Test method to check the invalid SSH keypair.
	 */
	@Test
	public void testisSSHKeyPairAvailableInvalid() {
		
		List<CredentialDTO> credentials = new ArrayList<CredentialDTO>();
		CredentialDTO credential = new CredentialDTO();
		credential.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		credentials.add(credential);
		
		ProcessorDTO processorDTO = new ProcessorDTO();
		Assert.assertFalse(processorDTO.isSSHKeyPairAvailable(credentials));
		
		//Asset by testing the empty idpUri and valid credential type.
		credentials.clear();
		credential.setCredentialType(CredentialType.SSH_KEYPAIR.getCode());
		credentials.add(credential);
		
		Assert.assertFalse(processorDTO.isSSHKeyPairAvailable(credentials));
	}
	
	/**
	 * Test method to check the invalid SSH keypair
	 */
	@Test (enabled=false, expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "The given SSH key pair group guid does not exist in key management system." )
	public void testSSHKeyPairInvalid() {
		ProcessorDTO processorDTO = new ProcessorDTO();
		processorDTO.validateSSHKeypair(INVALID_GUID);
	}
	
	/**
	 * Test method to check the valid SSH keypair
	 */
	@Test (enabled=false, expectedExceptions = {})
	public void testSSHKeyPairValid() {
		ProcessorDTO processorDTO = new ProcessorDTO();
		processorDTO.validateSSHKeypair(VALID_SSHKEYPAIR);
	}
	
	/**
	 * Test method to check the empty Truststore Certificate
	 */
	@Test (enabled=false, expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Trust store Certificate cannot be Empty." )
	public void testTruststoreCertificateEmpty() {
		ProcessorDTO processorDTO = new ProcessorDTO();
		processorDTO.validateTruststoreCertificate(null);
	}
	
	/**
	 * Test method to check the invalid Truststore Certificate
	 */
	@Test (enabled=false, expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "The given trust store group guid does not exist in key management system." )
	public void testTruststoreCertificateInvalid() {
		ProcessorDTO processorDTO = new ProcessorDTO();
		processorDTO.validateTruststoreCertificate(INVALID_GUID);
	}
	
	/**
	 * Test method to check the valid Truststore Certificate
	 */
	@Test (enabled=false, expectedExceptions = {})
	public void testTruststoreCertificateValid() {
		ProcessorDTO processorDTO = new ProcessorDTO();
		processorDTO.validateTruststoreCertificate(VALID_TRUSTSTORE);
	}
	
	public Processor constructProcessor() {
		ProcessorType foundProcessorType = ProcessorType.findByName(ProcessorType.REMOTEDOWNLOADER.getCode());
		return Processor.processorInstanceFactory(foundProcessorType);
	}
	
}
