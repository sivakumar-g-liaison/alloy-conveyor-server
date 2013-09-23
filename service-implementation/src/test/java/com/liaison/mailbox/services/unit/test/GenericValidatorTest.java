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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.validation.GenericValidator;

/**
 * Class which tests GenericValidator functionality.
 * 
 * @author veerasamyn
 */
public class GenericValidatorTest {

	private GenericValidator validator;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		validator = new GenericValidator();
	}

	@Test(expected = MailBoxConfigurationServicesException.class)
	public void testValidateMailBox_WrongStatus_ShouldThrowException() throws MailBoxConfigurationServicesException {

		MailBoxDTO dto = new MailBoxDTO();
		dto.setName("Test");
		dto.setStatus("sss");
		validator.validate(dto);
	}

	@Test(expected = MailBoxConfigurationServicesException.class)
	public void testValidateMailBox_WithoutName_ShouldThrowException() throws MailBoxConfigurationServicesException {

		MailBoxDTO dto = new MailBoxDTO();
		dto.setStatus("sss");
		validator.validate(dto);
	}

	@Test(expected = MailBoxConfigurationServicesException.class)
	public void testValidateMailBox_WithoutStatus_ShouldThrowException() throws MailBoxConfigurationServicesException {

		MailBoxDTO dto = new MailBoxDTO();
		dto.setName("Test");
		validator.validate(dto);
	}

	@Test(expected = MailBoxConfigurationServicesException.class)
	public void testValidateMailBox_Null_ShouldThrowException() throws MailBoxConfigurationServicesException {
		validator.validate(null);
	}

	@Test
	public void testValidateMailBox() throws MailBoxConfigurationServicesException {

		MailBoxDTO dto = new MailBoxDTO();
		dto.setName("MBXNAME");
		dto.setStatus("INACTIVE");

		Assert.assertEquals(true, validator.validate(dto));
	}

	public void testProfile() throws MailBoxConfigurationServicesException {

		ProfileDTO profile = new ProfileDTO();
		profile.setName("oncein5mins");

		Assert.assertEquals(true, validator.validate(profile));
	}

	@Test(expected = MailBoxConfigurationServicesException.class)
	public void testProfile_WithoutName_ShouldThrowException() throws MailBoxConfigurationServicesException {

		ProfileDTO profile = new ProfileDTO();
		validator.validate(profile);
	}

	@Test(expected = MailBoxConfigurationServicesException.class)
	public void testProfile_Null_ShouldThrowException() throws MailBoxConfigurationServicesException {
		validator.validate(null);
	}

}
