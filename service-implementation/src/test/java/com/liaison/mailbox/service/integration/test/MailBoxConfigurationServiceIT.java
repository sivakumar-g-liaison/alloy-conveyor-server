/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.integration.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.base.test.InitInitialDualDBContext;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Test class to test mailbox configuration service.
 * 
 * @author veerasamyn
 */
public class MailBoxConfigurationServiceIT extends BaseServiceTest {

	private String serviceInstanceId = "5D9C3B487184426E9F9629EFEE7C5913";
	private String aclManifest = "H4sIAAAAAAAAAO1YbW/aMBD+K5U/TqRNokACn5aW0EUroaLRJrWqIpMckVfHjpwQlVb973NeKKBWXcdWEVV8QbLvfPfwnO8ewyMCVgDlKaDBIwoF4ByiofxAA6SrWldR+4qq+7o6MPoD1Tg2e8Y16qCY8hmmboQGbEFpB6VYAMs31oKHkGXrjUUGolyhguEYC/wLs6+UYJJxdhxBgWqPERFZ7uEENo9d4O29BuTp0k5TSkKcE85q25NMTHE+5yJBg5vH50V9Gp3rMo3gFE5xBpEdlgjPOMvlVuUe8QQT9uwcDJ0fgev5wWR6Lg/WVn9ZMoXklu2517bvTrxnm8tyEAzTlxHGE8/97kyb9DKZNNrDseuh25IrUhAKMVQgBGR8IcIywJfSv1k2eaeQ5dOVx9pafgu4z6WD5KsgISgzwe9ASJcUREKyrOJIhi8wXaxir01N9J/fXN+5cK989HT71PlnLGXxEizrDYm8HPvFEuuyQnTG7xuC9ovmDpZKW5iJcI4lmDTd93WpZwqUTSRbIoOaoD2DSihNlZCSvZepAlJe3v/JyEnI2ZzEJ7Hgi3QHUCAEFwrjOZmvBvHHYGNypGZ7B1hB3FKJIST8aCJizMhDFV7bRSlGPVMdmYap2n1dNcyRZmjGmTOybN1ynJ7R3dCNAkDgoFq9JR3bo7ehciEqiLvw+N5RXvJxuTa9TWjn/dxvuF4600A3Jmd+oKraDiUb1zoQlDWIxepO/HXNXg+zKtMr1sCOEsI+teC3SdbaJfhtegp9ZsF/2e6nE8f1dnq/1ydfebs3ho95wLfr3h6my6Gf29XPrh/4Ekgof8LrvaMrYBGIrRfYTmJOGpEec0bkRVdCs6tapmYq4XxmKIY615SZZYZKV7Ow1Y90sGawMRH+COog/gfxbwUzn3tYyP5iMjFEV3Xdh5CWvcjCZfU/n2x8mfqm9PwNJYKk5vgUAAA=";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
		System.setProperty("archaius.deployment.environment", "test");
		InitInitialDualDBContext.init();
	}
    
	/**
	 * Method constructs MailBox with valid data.
	 * 
	 * @throws LiaisonException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws SymmetricAlgorithmException 
	 */
	@Test
	public void testCreateMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException, SymmetricAlgorithmException {

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);
		
		MailBoxConfigurationService service = new MailBoxConfigurationService();
		AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

		Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

		// Get the mailbox
		GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

		// Assertion
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		Assert.assertEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
		Assert.assertEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
		Assert.assertEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
		Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

	}
	
	@Test
	public void testCreateMailBoxWithoutServiceInstanceId() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException, SymmetricAlgorithmException {

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);
		
		MailBoxConfigurationService service = new MailBoxConfigurationService();
		AddMailBoxResponseDTO response = service.createMailBox(requestDTO, null, aclManifest);

		Assert.assertEquals(FAILURE, response.getResponse().getStatus());

	}
	
	@Test
	public void testCreateMailBoxWithNewServiceInstanceId() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException, SymmetricAlgorithmException {

		/// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		requestDTO.setMailBox(mbxDTO);
		
		MailBoxConfigurationService service = new MailBoxConfigurationService();
		AddMailBoxResponseDTO response = service.createMailBox(requestDTO, MailBoxUtil.getGUID(), aclManifest);

		Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

	}
	
	@Test
	public void testCreateMailBoxWithoutTenancyKey() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException, SymmetricAlgorithmException {

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		mbxDTO.setTenancyKey("Dummy");
		requestDTO.setMailBox(mbxDTO);
		
		MailBoxConfigurationService service = new MailBoxConfigurationService();
		AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

		Assert.assertEquals(FAILURE, response.getResponse().getStatus());

	}
	
	@Test
	public void testCreateMailBoxWithoutMailboxDTO() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException, SymmetricAlgorithmException {

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		requestDTO.setMailBox(null);
		
		MailBoxConfigurationService service = new MailBoxConfigurationService();
		AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

		Assert.assertEquals(FAILURE, response.getResponse().getStatus());

	}
	
	@Test
	public void testCreateMailBoxWithExistingMailbox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException, SymmetricAlgorithmException {

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
		mbxDTO.setName("MBX_TEST1441230467303");
		mbxDTO.setTenancyKey("G2_DEV_INT_MONIKER");
		requestDTO.setMailBox(mbxDTO);
		
		MailBoxConfigurationService service = new MailBoxConfigurationService();
		AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, null);

		Assert.assertEquals(FAILURE, response.getResponse().getStatus());

	}
    
}
