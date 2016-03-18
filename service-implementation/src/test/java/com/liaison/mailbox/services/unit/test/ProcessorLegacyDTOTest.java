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

import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
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
	
	/**
	 * @throws Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
        System.setProperty("com.liaison.secure.properties.path", "invalid");
        System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
	}
	
	/**
	 * Test method to validate the userId and Password.
	 */
	@Test
	public void testValidateFTPUserIdCredential() {
		
		Processor processor = constructProcessor();
		processor.setProcsrProtocol(Protocol.FTP.name());
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setUserId(USER_NAME);
		credentialDTO.setPassword(PASSWORD);
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
	
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor, credentialDTO);
	}
	
	/**
	 * Test method for FTP to validate the empty userId.
	 */
	@Test (expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Username cannot be Empty." )

	public void testValidateFTPEmptyUserIdCredential() {
		
		Processor processor = constructProcessor();
		processor.setProcsrProtocol(Protocol.FTP.name());
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor, credentialDTO);
	}
	
	/**
	 * Test method for FTP to validate the empty password.
	 */

	@Test (expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Password cannot be Empty." )
	public void testValidateFTPEmptyPasswordCredential() {
		
		Processor processor = constructProcessor();
		processor.setProcsrProtocol(Protocol.FTP.name());
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setUserId(USER_NAME);
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());

		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor, credentialDTO);
	}
	
	/**
	 * Test method for FTPS to validate the empty userId.
	 */

	@Test (expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Username cannot be Empty." )
	public void testValidateFTPSEmptyUserIdCredential() {
		
		Processor processor = constructProcessor();
		processor.setProcsrProtocol(Protocol.FTPS.name());
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor, credentialDTO);
	}
	
	/**
	 * Test method for FTPS to validate the empty password.
	 */
	@Test (expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Password cannot be Empty." )
	public void testValidateFTPSEmptyPasswordCredential() {
		
		Processor processor = constructProcessor();
		processor.setProcsrProtocol(Protocol.FTPS.name());
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setUserId(USER_NAME);
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor, credentialDTO);
	}
	
	/**
	 * Test method for HTTPS to validate the empty userId.
	 */
	@Test (expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Username cannot be Empty." )
	public void testValidateHTTPSEmptyUserIdCredential() {
		
		Processor processor = constructProcessor();
		processor.setProcsrProtocol(Protocol.HTTPS.name());
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor, credentialDTO);
	}
	
	/**
	 * Test method for HTTPS to validate the empty password.
	 */
	@Test (expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Password cannot be Empty." )
	public void testValidateHTTPSEmptyPasswordCredential() {
		
		Processor processor = constructProcessor();
		processor.setProcsrProtocol(Protocol.HTTPS.name());
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setUserId(USER_NAME);
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor, credentialDTO);
	}
	
	/**
	 * Test method for SFTP to validate the empty userId.
	 */
	@Test (expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Username cannot be Empty." )
	public void testValidateSFTPEmptyUserIdCredential() {
		
		Processor processor = constructProcessor();
		processor.setProcsrProtocol(Protocol.SFTP.name());
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor, credentialDTO);
	}
	
	/**
	 * Test method for SFTP to validate the empty password.
	 */
	@Test (expectedExceptions = MailBoxConfigurationServicesException.class, expectedExceptionsMessageRegExp = "Either the Password or the SSH Keypair is Empty." )
	public void testValidateSFTPEmptyPasswordCredential() {
		
		Processor processor = constructProcessor();
		processor.setProcsrProtocol(Protocol.SFTP.name());
		CredentialDTO credentialDTO = new CredentialDTO();
		credentialDTO.setUserId(USER_NAME);
		credentialDTO.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.validateCredentials(processor, credentialDTO);
	}
	
	/**
	 * Test method to check the valid SSH keypair.
	 */
	@Test
	public void testisSSHKeyPairAvailable() {
		
		Set<CredentialDTO> credentials = new HashSet<>();
		CredentialDTO credential = new CredentialDTO();
		credential.setCredentialType(CredentialType.SSH_KEYPAIR.getCode());
		credential.setIdpURI(VALID_SSHKEYPAIR);
		credentials.add(credential);
		
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.setCredentials(credentials);
		Assert.assertTrue(processorDTO.isSSHKeyPairAvailable());
	}
	
	/**
	 * Test method to check the invalid SSH keypair.
	 */
	@Test
	public void testisSSHKeyPairAvailableInvalid() {
		
		Set<CredentialDTO> credentials = new HashSet<>();
		CredentialDTO credential = new CredentialDTO();
		credential.setCredentialType(CredentialType.LOGIN_CREDENTIAL.getCode());
		credentials.add(credential);
		
		ProcessorLegacyDTO processorDTO = new ProcessorLegacyDTO();
		processorDTO.setCredentials(credentials);
		Assert.assertFalse(processorDTO.isSSHKeyPairAvailable());
		
		//Asset by testing the empty idpUri and valid credential type.
		credentials.clear();
		credential.setCredentialType(CredentialType.SSH_KEYPAIR.getCode());
		credentials.add(credential);
		
		processorDTO.setCredentials(credentials);
		Assert.assertFalse(processorDTO.isSSHKeyPairAvailable());
	}
	
	public Processor constructProcessor() {
		ProcessorType foundProcessorType = ProcessorType.findByName(ProcessorType.REMOTEDOWNLOADER.getCode());
		return Processor.processorInstanceFactory(foundProcessorType);
	}
	
}
