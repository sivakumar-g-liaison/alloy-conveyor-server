package com.liaison.mailbox.service.integration.test;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.base.test.InitInitialDualDBContext;
import com.liaison.mailbox.service.core.ProfileConfigurationService;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProfileResponseDTO;
import com.liaison.mailbox.service.dto.ui.GetProfileByNameResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;

/**
 * Test class to test profile configuration service.
 * 
 * @author OFS
 */
public class ProfileConfigurationServiceIT extends BaseServiceTest {
	
	/**
	 * @throws Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
		System.setProperty("archaius.deployment.environment", "test");
		InitInitialDualDBContext.init();
	}
	
	/**
	 * Method to create a new profile.
	 */
	@Test
	public void testCreateProfile() {
		
		//Adding a profile
		AddProfileRequestDTO requestDTO = new AddProfileRequestDTO();
		ProfileDTO profileDTO = constructDummyProfileDTO(System.currentTimeMillis());
		requestDTO.setProfile(profileDTO);
		
		ProfileConfigurationService service = new ProfileConfigurationService();
		AddProfileResponseDTO response = service.createProfile(requestDTO);
		
		Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
		
	}
	
	/**
	 * Method to create and retrieve the profile.
	 */
	@Test
	public void testCreateAndRetrieveProfile() {
		
		//Adding a profile
		AddProfileRequestDTO requestDTO = new AddProfileRequestDTO();
		ProfileDTO profileDTO = constructDummyProfileDTO(System.currentTimeMillis());
		requestDTO.setProfile(profileDTO);
		
		ProfileConfigurationService service = new ProfileConfigurationService();
		AddProfileResponseDTO response = service.createProfile(requestDTO);
		
		Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
		
		//Retrieving a profile
		GetProfileByNameResponseDTO getResponseDTO = service.getProfileByName(requestDTO.getProfile().getName());
		
		// Assertion checking
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(requestDTO.getProfile().getName(), getResponseDTO.getProfile().getName());
		
	}
	
	/**
	 * Method to retrieve the non-existing profile.
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */
	@Test
	public void testRetrieveNonExistingProfile() throws MailBoxConfigurationServicesException {
		
		AddProfileRequestDTO requestDTO = new AddProfileRequestDTO();
		ProfileDTO profileDTO = constructDummyProfileDTO(System.currentTimeMillis());
		requestDTO.setProfile(profileDTO);
		
		ProfileConfigurationService service = new ProfileConfigurationService();
		GetProfileByNameResponseDTO getResponseDTO = service.getProfileByName(requestDTO.getProfile().getName());
		
		Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());
		
	}
	
	/**
	 * Method to revise the existing profile.
	 */
	@Test
	public void testReviseProfile() {
		
		//Adding a profile
		AddProfileRequestDTO addRequestDTO = new AddProfileRequestDTO();
		ProfileDTO profileDTO = constructDummyProfileDTO(System.currentTimeMillis());
		addRequestDTO.setProfile(profileDTO);
		String createdProfile = profileDTO.getName();
		
		ProfileConfigurationService service = new ProfileConfigurationService();
		AddProfileResponseDTO addResponse = service.createProfile(addRequestDTO);
		
		Assert.assertEquals(SUCCESS, addResponse.getResponse().getStatus());
		
		//Revising the profile
		ReviseProfileRequestDTO reviseRequestDTO = new ReviseProfileRequestDTO();
		reviseRequestDTO.setProfile(profileDTO);
		reviseRequestDTO.getProfile().setName(constructDummyProfileDTO(System.currentTimeMillis()).getName());
		reviseRequestDTO.getProfile().setId(addResponse.getProfile().getGuId());
		
		ReviseProfileResponseDTO reviseResponse = service.updateProfile(reviseRequestDTO);
		GetProfileByNameResponseDTO reviseResponseDTO = service.getProfileByName(reviseRequestDTO.getProfile().getName());
		
		// Assertion checking
		Assert.assertEquals(SUCCESS, reviseResponse.getResponse().getStatus());
		Assert.assertNotEquals(createdProfile, reviseResponseDTO.getProfile().getName());
		
	}
	
}
