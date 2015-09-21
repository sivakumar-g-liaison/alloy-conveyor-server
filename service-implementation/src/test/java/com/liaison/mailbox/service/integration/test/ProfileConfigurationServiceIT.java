package com.liaison.mailbox.service.integration.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.liaison.mailbox.dtdm.dao.FilterObject;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.base.test.InitInitialDualDBContext;
import com.liaison.mailbox.service.core.ProfileConfigurationService;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProfileResponseDTO;
import com.liaison.mailbox.service.dto.ui.GetProfileByNameResponseDTO;
import com.liaison.mailbox.service.dto.ui.GetProfileResponseDTO;

/**
 * Test class to test profile configuration service.
 * 
 * @author OFS
 */
public class ProfileConfigurationServiceIT extends BaseServiceTest {
	
	private static final String PAGE = "1";
	
	private static final String PAGE_SIZE = "25";
	
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
	 */
	@Test
	public void testRetrieveNonExistingProfile() {
		
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
	
	/**
	 * Method to create already existing profile
	 */
	@Test
	public void testCreateDuplicateProfile() {
		
		//Adding a profile
		AddProfileRequestDTO requestDTO = new AddProfileRequestDTO();
		ProfileDTO profileDTO = constructDummyProfileDTO(System.currentTimeMillis());
		requestDTO.setProfile(profileDTO);
		
		ProfileConfigurationService service = new ProfileConfigurationService();
		AddProfileResponseDTO response = service.createProfile(requestDTO);
		
		Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
		
		//Adding the profile which is created above
		AddProfileResponseDTO profileResponse = service.createProfile(requestDTO);
		
		Assert.assertEquals(FAILURE, profileResponse.getResponse().getStatus());

	}
	
	/**
	 * Method to search a profile
	 */
	@Test
	public void testSearchProfile() {
		
		//Adding a profile
		AddProfileRequestDTO requestDTO = new AddProfileRequestDTO();
		ProfileDTO profileDTO = constructDummyProfileDTO(System.currentTimeMillis());
		requestDTO.setProfile(profileDTO);
		
		ProfileConfigurationService service = new ProfileConfigurationService();
		AddProfileResponseDTO response = service.createProfile(requestDTO);
		
		Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

		//search the created profile
		Map<String, List<FilterObject>> searchObjectList = new HashMap<>();
		List<FilterObject> objectList = new ArrayList<FilterObject>();
		FilterObject object = new FilterObject();
		object.setField("name");
		object.setText(profileDTO.getName());
		objectList.add(object);
		searchObjectList.put("filterText", objectList);
		Gson gsonObject = new Gson();

		String searchText = gsonObject.toJson(searchObjectList);
		GetProfileResponseDTO profileResponse = service.getProfiles(PAGE, PAGE_SIZE, "", searchText);
		
		Assert.assertEquals(SUCCESS, profileResponse.getResponse().getStatus());
		
	}
	
}
