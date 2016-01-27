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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.mailbox.service.dto.configuration.FolderDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.validation.GenericValidator;

/**
 * Class which tests GenericValidator functionality.
 * 
 * @author veerasamyn
 */
public class GenericValidatorTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
        System.setProperty("com.liaison.secure.properties.path", "invalid");
        System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
        System.setProperty("archaius.deployment.environment", "test");
	}
    
	/**
	 * Method to validate mailbox with wrong status.
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */
	@Test(expectedExceptions = MailBoxConfigurationServicesException.class)
	public void testValidateMailBox_WrongStatus_ShouldThrowException() throws MailBoxConfigurationServicesException {

		MailBoxDTO dto = new MailBoxDTO();
		dto.setName("Test");
		dto.setStatus("sss");
		dto.setTenancyKey("Tenancykey");
		GenericValidator validator = new GenericValidator();
		validator.validate(dto);
	}
    
	/**
	 * Method to validate mailbox without name.
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */
	@Test(expectedExceptions = MailBoxConfigurationServicesException.class)
	public void testValidateMailBox_WithoutName_ShouldThrowException() throws MailBoxConfigurationServicesException {

		MailBoxDTO dto = new MailBoxDTO();
		dto.setStatus("sss");
		GenericValidator validator = new GenericValidator();
		validator.validate(dto);
	}
    
	/**
	 * Method to validate mailbox without status.
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */

	@Test(expectedExceptions = MailBoxConfigurationServicesException.class)
	public void testValidateMailBox_WithoutStatus_ShouldThrowException() throws MailBoxConfigurationServicesException {

		MailBoxDTO dto = new MailBoxDTO();
		dto.setName("Test");
		GenericValidator validator = new GenericValidator();
		validator.validate(dto);
	}
    
	/**
	 * Method to validate mailbox with null.
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */
	@Test(expectedExceptions = MailBoxConfigurationServicesException.class)
	public void testValidateMailBox_Null_ShouldThrowException() throws MailBoxConfigurationServicesException {
		GenericValidator validator = new GenericValidator();
		validator.validate(null);
	}
    
	/**
	 * Method to validate mailbox.
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */
	@Test
	public void testValidateMailBox() throws MailBoxConfigurationServicesException {

		MailBoxDTO dto = new MailBoxDTO();
		dto.setName("MBXNAME");
		dto.setStatus("INACTIVE");
		dto.setTenancyKey("TENANCY_KEY");
		
		GenericValidator validator = new GenericValidator();
		Assert.assertEquals(true, validator.validate(dto));
	}
    
	/**
	 * Method to test profile.
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */

	@Test
	public void testProfile() throws MailBoxConfigurationServicesException {

		ProfileDTO profile = new ProfileDTO();
		profile.setName("oncein5mins");

		GenericValidator validator = new GenericValidator();
		Assert.assertEquals(true, validator.validate(profile));
	}
    
	/**
	 * Method to test profile without name.
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */

	@Test(expectedExceptions = MailBoxConfigurationServicesException.class)
	public void testProfile_WithoutName_ShouldThrowException() throws MailBoxConfigurationServicesException {

		ProfileDTO profile = new ProfileDTO();
		GenericValidator validator = new GenericValidator();
		validator.validate(profile);
	}
    
	/**
	 * Method to test profile with null.
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */

	@Test(expectedExceptions = MailBoxConfigurationServicesException.class)
	public void testProfile_Null_ShouldThrowException() throws MailBoxConfigurationServicesException {
		GenericValidator validator = new GenericValidator();
		validator.validate(null);
	}
    /**
     * Method to validate processor.
     *  
     * @throws MailBoxConfigurationServicesException
     */
	@Test
	public void testValidateProcessor() throws MailBoxConfigurationServicesException {

		ProcessorDTO dto = new ProcessorDTO();
		dto.setType("SWEEPER");
		dto.setStatus("ACTIVE");
		dto.setProtocol("HTTP");
		dto.setLinkedMailboxId("DUMMYID");
		GenericValidator validator = new GenericValidator();
		validator.validate(dto);
	}
    
	/**
	 * Method to validate processor with empty.
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */

	@Test(expectedExceptions = MailBoxConfigurationServicesException.class)
	public void testValidateProcessor_WithEmptySpace_ShouldThrowException() throws MailBoxConfigurationServicesException {

		ProcessorDTO dto = new ProcessorDTO();
		dto.setType(" ");
		dto.setStatus(" ");
		GenericValidator validator = new GenericValidator();
		validator.validate(dto);
	}
    
	/**
	 * Method to validate processor with invalid value.
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */

	@Test(expectedExceptions = MailBoxConfigurationServicesException.class)
	public void testValidateProcessor_WithInvalidValue_ShouldThrowException() throws MailBoxConfigurationServicesException {

		ProcessorDTO dto = new ProcessorDTO();
		dto.setType("sdsd");
		dto.setStatus("dsddd");
		GenericValidator validator = new GenericValidator();
		validator.validate(dto);
	}
    
	/**
	 * Method to validate processor without status.
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */
	@Test(expectedExceptions = MailBoxConfigurationServicesException.class)
	public void testValidateProcessor_WithoutStatus_ShouldThrowException() throws MailBoxConfigurationServicesException {

		ProcessorDTO dto = new ProcessorDTO();
		dto.setType("SWEEPER");
		GenericValidator validator = new GenericValidator();
		validator.validate(dto);
	}
    
	/**
	 * Method to validate processor without type.
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */
	@Test(expectedExceptions = MailBoxConfigurationServicesException.class)
	public void testValidateProcessor_WithoutType_ShouldThrowException() throws MailBoxConfigurationServicesException {

		ProcessorDTO dto = new ProcessorDTO();
		dto.setStatus("ACTIVE");
		GenericValidator validator = new GenericValidator();
		validator.validate(dto);
	}
    
	/**
	 * Method to validate Folder.
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */
	@Test
	public void testValidateFolder() throws MailBoxConfigurationServicesException {

		FolderDTO dto = new FolderDTO();
		dto.setFolderType("PAYLOAD_LOCATION");
		dto.setFolderURI("/SAMPLE");
		GenericValidator validator = new GenericValidator();
		validator.validate(dto);
	}
    
	/**
	 * Method to validate folder with invalid value.
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */
	@Test(expectedExceptions = MailBoxConfigurationServicesException.class)
	public void testValidateFolder_WithInvalidValue_ShouldThrowException() throws MailBoxConfigurationServicesException {

		FolderDTO dto = new FolderDTO();
		dto.setFolderType("payload_location");
		dto.setFolderURI(null);
		GenericValidator validator = new GenericValidator();
		validator.validate(dto);
	}

}
