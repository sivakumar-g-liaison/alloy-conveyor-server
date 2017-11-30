/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.unit.test;

import java.net.URI;
import java.net.URISyntaxException;

import com.liaison.mailbox.service.base.test.BaseServiceTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dto.configuration.FolderDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.validation.GenericValidator;

/**
 * Class which tests GenericValidator functionality.
 * 
 * @author veerasamyn
 */
public class GenericValidatorTest extends BaseServiceTest {

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
        dto.setClusterType("seCure");

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
	
	/**
	 * Method to validate the given url
	 * 
	 * @throws URISyntaxException 
	 */
	@Test
	public void testValidateURL() throws URISyntaxException {
	    
	    RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
	    GenericValidator validator = new GenericValidator();
	    String host = "10.146.18.25";
	    
	    propertiesDTO.setUrl(new URI(Protocol.FTP.getCode(), null, host, MailBoxConstants.FTPS_PORT, "", null, null).toString());
	    Assert.assertTrue(validator.validate(propertiesDTO));
	    
	    propertiesDTO.setUrl(new URI(Protocol.FTPS.getCode(), null, host, MailBoxConstants.FTPS_PORT, "", null, null).toString());
	    Assert.assertTrue(validator.validate(propertiesDTO));
	    
	    propertiesDTO.setUrl(new URI(Protocol.SFTP.getCode(), null, "lsvlmbox01d.liaison.dev", MailBoxConstants.SFTP_PORT, "", null, null).toString());
        Assert.assertTrue(validator.validate(propertiesDTO));
        
        propertiesDTO.setUrl(new URI(Protocol.HTTP.getCode(), null, "localhost", MailBoxConstants.HTTP_PORT, "", null, null).toString());
        Assert.assertTrue(validator.validate(propertiesDTO));
        
        propertiesDTO.setUrl(new URI(Protocol.HTTPS.getCode(), null, "localhost", MailBoxConstants.HTTPS_PORT, "", null, null).toString());
        Assert.assertTrue(validator.validate(propertiesDTO));
	    
	}
	
	/**
	 * Method to validate the given invalid url
	 * 
	 * @throws URISyntaxException
	 */
	@Test(expectedExceptions = MailBoxConfigurationServicesException.class)
	public void testValidateInvalidURL() throws URISyntaxException {
	    
	    RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        GenericValidator validator = new GenericValidator();
        
        propertiesDTO.setUrl(new URI("tcp", null, "10.146.18.25", MailBoxConstants.FTPS_PORT, "", null, null).toString());
        validator.validate(propertiesDTO);
        
	}
	
	/**
	 *  Method to validate the given invalid url
	 * 
	 * @throws URISyntaxException
	 */
	@Test(expectedExceptions = MailBoxConfigurationServicesException.class)
    public void testValidateURLWithSpace() throws URISyntaxException {
        
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        GenericValidator validator = new GenericValidator();
        
        propertiesDTO.setUrl("ftp://lsvlmbox01d." + " " + "liaison.dev:25");
        validator.validate(propertiesDTO);
        
    }

    @Test
    public void testProcessMode() {

        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        GenericValidator validator = new GenericValidator();

        propertiesDTO.setProcessMode("SYNC");
        validator.validate(propertiesDTO);

    }

    @Test(expectedExceptions = MailBoxConfigurationServicesException.class)
    public void testProcessModeFailure() {

        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        GenericValidator validator = new GenericValidator();

        propertiesDTO.setProcessMode("SYNCS");
        validator.validate(propertiesDTO);

    }
    
    @Test()
    public void testValidTriggerFileIncludingExtension() {

        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        GenericValidator validator = new GenericValidator();

        propertiesDTO.setTriggerFile("trigger.txt");
        Assert.assertTrue(validator.validate(propertiesDTO));
    }
    
    @Test()
    public void testValidTriggerFile() {

        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        GenericValidator validator = new GenericValidator();

        propertiesDTO.setTriggerFile("trigger");
        Assert.assertTrue(validator.validate(propertiesDTO));

    }
    
    @Test(expectedExceptions = MailBoxConfigurationServicesException.class)
    public void testValidTriggerInValidFile() {

        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        GenericValidator validator = new GenericValidator();

        propertiesDTO.setTriggerFile("trigger.inp");
        Assert.assertFalse(validator.validate(propertiesDTO));

    }
    
    @Test(expectedExceptions = MailBoxConfigurationServicesException.class)
    public void testInValidTriggerFiles() {

        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        GenericValidator validator = new GenericValidator();

        propertiesDTO.setTriggerFile("trigger.INP");
        Assert.assertFalse(validator.validate(propertiesDTO));
        
        propertiesDTO.setTriggerFile("trigger.Inp");
        Assert.assertFalse(validator.validate(propertiesDTO));
        
        propertiesDTO.setTriggerFile("trigger.iNp");
        Assert.assertFalse(validator.validate(propertiesDTO));
        
        propertiesDTO.setTriggerFile("trigger.inP");
        Assert.assertFalse(validator.validate(propertiesDTO));
        
        propertiesDTO.setTriggerFile("trigger.INp");
        Assert.assertFalse(validator.validate(propertiesDTO));
        
        propertiesDTO.setTriggerFile("trigger.iNP");
        Assert.assertFalse(validator.validate(propertiesDTO));
        
        propertiesDTO.setTriggerFile("trigger.InP");
        Assert.assertFalse(validator.validate(propertiesDTO));

    }

    @Test(expectedExceptions = MailBoxConfigurationServicesException.class)
    public void testInvaidSocketTimeOut() {

        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        GenericValidator validator = new GenericValidator();

        propertiesDTO.setSocketTimeout(0);
        Assert.assertTrue(validator.validate(propertiesDTO));
        
        propertiesDTO.setSocketTimeout(-1);
        Assert.assertFalse(validator.validate(propertiesDTO));
        
        propertiesDTO.setSocketTimeout(6000000);
        Assert.assertFalse(validator.validate(propertiesDTO));
        
        propertiesDTO.setSocketTimeout(300001);
        Assert.assertFalse(validator.validate(propertiesDTO));
        
    }
    
    @Test()
    public void testValidSocketTimeOut() {
 
        RemoteProcessorPropertiesDTO propertiesDTO = new RemoteProcessorPropertiesDTO();
        GenericValidator validator = new GenericValidator();

        propertiesDTO.setSocketTimeout(1000);
        Assert.assertTrue(validator.validate(propertiesDTO));

        propertiesDTO.setSocketTimeout(29999);
        Assert.assertTrue(validator.validate(propertiesDTO));

    }
}
