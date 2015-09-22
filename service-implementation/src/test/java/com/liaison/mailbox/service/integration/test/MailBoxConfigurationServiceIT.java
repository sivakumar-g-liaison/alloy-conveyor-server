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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.base.test.InitInitialDualDBContext;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.FileInfoDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseMailBoxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetPropertiesValueResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.ui.SearchMailBoxResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
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
		System.setProperty("com.liaison.secure.properties.path", "invalid");
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

	/**
     * Method to check to fail creation of MailBox with Null Value Should fail.
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
    public void testCreateMailBoxWithNull() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
            JAXBException, IOException, SymmetricAlgorithmException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        requestDTO.setMailBox(null);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

        Assert.assertEquals(FAILURE, response.getResponse().getStatus());

    }

    /**
     * Method to check to fail creation of MailBox with Empty ServiceInstanceId Should fail.
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
    public void testCreateMailBoxWithServiceInstanceIdEmpty() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
            JAXBException, IOException, SymmetricAlgorithmException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, "", aclManifest);

        Assert.assertEquals(FAILURE, response.getResponse().getStatus());

    }

    /**
     * Method to check to fail creation of MailBox with null ServiceInstanceId Should fail.
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

    /**
     * Method to check to pass creation of MailBox with new ServiceInstanceId.
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

    /**
     * Method to check to fail creation of MailBox without mailbox name Should fail.
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
    public void testCreateMailBoxWithoutName() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
            JAXBException, IOException, SymmetricAlgorithmException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setStatus("ACTIVE");
        mailBox.setTenancyKey("test Tenancy Key");
        requestDTO.setMailBox(mailBox);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

        Assert.assertEquals(FAILURE, response.getResponse().getStatus());

    }

    /**
     * Method to check to fail creation of MailBox without mailbox status Should fail.
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
    public void testCreateMailBoxWithoutStatus() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
            JAXBException, IOException, SymmetricAlgorithmException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setName("TestMailBox" + System.currentTimeMillis());
        mailBox.setTenancyKey("test Tenancy Key");
        requestDTO.setMailBox(mailBox);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

        Assert.assertEquals(FAILURE, response.getResponse().getStatus());

    }

    /**
     * Method to check to fail creation of MailBox without tenancy key Should fail.
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
	public void testCreateMailBoxWithoutTenancyKey() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
			JAXBException, IOException, SymmetricAlgorithmException {

		// Adding the mailbox
		AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
		MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setName("TestMailBox" + System.currentTimeMillis());
        mailBox.setStatus("ACTIVE");
		requestDTO.setMailBox(mailBox);

		MailBoxConfigurationService service = new MailBoxConfigurationService();
		AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

		Assert.assertEquals(FAILURE, response.getResponse().getStatus());

	}

    /**
     * Method to check to fail creation of MailBox with empty tenancy key Should fail.
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
    public void testCreateMailBoxWithTenancyKeyEmpty() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
            JAXBException, IOException, SymmetricAlgorithmException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setName("TestMailBox" + System.currentTimeMillis());
        mailBox.setStatus("ACTIVE");
        mailBox.setTenancyKey("");
        requestDTO.setMailBox(mailBox);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

        Assert.assertEquals(FAILURE, response.getResponse().getStatus());

    }

    /**
     * Method to check to fail creation of MailBox with null tenancy key Should fail.
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
    public void testCreateMailBoxWithTenancyKeyNull() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
            JAXBException, IOException, SymmetricAlgorithmException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setName("TestMailBox" + System.currentTimeMillis());
        mailBox.setStatus("ACTIVE");
        mailBox.setTenancyKey(null);
        requestDTO.setMailBox(mailBox);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

        Assert.assertEquals(FAILURE, response.getResponse().getStatus());

    }

    /**
     * Method to check to fail creation of MailBox with Existing Mailbox Should fail.
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

    /**
     * Method Get MailBox with valid data.
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
    public void testGetMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
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

    /**
     * Method Get MailBox with valid data.
     *
     * @throws IOException
     * @throws JAXBException
     * @throws JsonMappingException
     * @throws JsonParseException
     * @throws MailBoxConfigurationServicesException
     * @throws SymmetricAlgorithmException
     *
     */
    @Test
    public void testGetMailBoxWithAddConstraint()
            throws MailBoxConfigurationServicesException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), true, serviceInstanceId, aclManifest);

        // Assertion
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
        Assert.assertEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
        Assert.assertEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
        Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

    }

    /**
     * Method Get MailBox with Empty ServiceInstanceId Should fail.
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
    public void testGetMailBoxWithServiceInstanceIdEmpty() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
            JAXBException, IOException, SymmetricAlgorithmException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, "", aclManifest);

        // Assertion
        Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());

    }

    /**
     * Method Get MailBox with null MailBox value Should fail.
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
    public void testGetMailBoxWithMailBoxNull() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
            JAXBException, IOException, SymmetricAlgorithmException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox("dummy", false, serviceInstanceId, aclManifest);

        // Assertion
        Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());

    }

    /**
     * Method Revise MailBox with valid data.
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
    public void testReviseMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
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

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setName("RevisedMailBox" + System.currentTimeMillis());
        mbxDTO.setDescription("RevisedMailBox Desc");
        mbxDTO.setStatus("INACTIVE");
        mbxDTO.setShardKey("RevisedShard");
        mbxDTO.setGuid(getResponseDTO.getMailBox().getGuid());
        reviseRequestDTO.setMailBox(mbxDTO);

        // Assertion
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertNotEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
        Assert.assertNotEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
        Assert.assertNotEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
        Assert.assertNotEquals(requestDTO.getMailBox().getStatus(), getResponseDTO.getMailBox().getStatus());

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), serviceInstanceId, aclManifest);

        Assert.assertEquals(SUCCESS, reviseResponse.getResponse().getStatus());

    }

    /**
     * Method Revise MailBox with Empty ServiceInstanceId Should fail.
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
    public void testReviseMailBoxWithServiceInstanceIdEmpty() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
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

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setName("RevisedMailBox");
        mbxDTO.setDescription("RevisedMailBox Desc");
        mbxDTO.setStatus("INACTIVE");
        mbxDTO.setShardKey("RevisedShard");
        mbxDTO.setGuid(getResponseDTO.getMailBox().getGuid());
        reviseRequestDTO.setMailBox(mbxDTO);

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), "", aclManifest);

        Assert.assertEquals(FAILURE, reviseResponse.getResponse().getStatus());

    }

    /**
     * Method Revise MailBox with dummy ServiceInstanceId Should pass.
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
    public void testReviseMailBoxWithDummyServiceInstanceId() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
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

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setName(mbxDTO.getName());
        mbxDTO.setDescription("RevisedMailBox Desc");
        mbxDTO.setStatus("INACTIVE");
        mbxDTO.setShardKey("RevisedShard");
        mbxDTO.setGuid(getResponseDTO.getMailBox().getGuid());
        reviseRequestDTO.setMailBox(mbxDTO);

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), "dummy" + System.currentTimeMillis(), aclManifest);

        Assert.assertEquals(SUCCESS, reviseResponse.getResponse().getStatus());

    }

    /**
     * Method Revise MailBox with Wrong Guid Should fail.
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
    public void testReviseMailBoxWithWrongGuid() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
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

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setName("RevisedMailBox");
        mbxDTO.setDescription("RevisedMailBox Desc");
        mbxDTO.setStatus("INACTIVE");
        mbxDTO.setShardKey("RevisedShard");
        mbxDTO.setGuid(getResponseDTO.getMailBox().getGuid());
        reviseRequestDTO.setMailBox(mbxDTO);

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid()+"X", serviceInstanceId, aclManifest);

        Assert.assertEquals(FAILURE, reviseResponse.getResponse().getStatus());

    }

    /**
     * Method Revise MailBox with Existing MailBox Should fail.
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
    public void testReviseMailBoxWithExistingMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
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

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setName("MBX_TEST1441230467303");
        mbxDTO.setDescription("RevisedMailBox Desc");
        mbxDTO.setStatus("INACTIVE");
        mbxDTO.setShardKey("RevisedShard");
        mbxDTO.setTenancyKey("G2_DEV_INT_MONIKER");
        mbxDTO.setGuid(getResponseDTO.getMailBox().getGuid());
        reviseRequestDTO.setMailBox(mbxDTO);

        // Assertion
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertNotEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
        Assert.assertNotEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
        Assert.assertNotEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
        Assert.assertNotEquals(requestDTO.getMailBox().getStatus(), getResponseDTO.getMailBox().getStatus());

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), serviceInstanceId, aclManifest);

        Assert.assertEquals(FAILURE, reviseResponse.getResponse().getStatus());

    }

    /**
     * Method Revise MailBox with valid data Should fail.
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
    public void testReviseMailBoxWithNullMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
            JAXBException, IOException, SymmetricAlgorithmException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        mbxDTO.setGuid("dummy");
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setName("RevisedMailBox" + System.currentTimeMillis());
        mbxDTO.setDescription("RevisedMailBox Desc");
        mbxDTO.setStatus("INACTIVE");
        mbxDTO.setShardKey("RevisedShard");
        mbxDTO.setGuid("dummy");
        reviseRequestDTO.setMailBox(mbxDTO);

        // Assertion
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertNotEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
        Assert.assertNotEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
        Assert.assertNotEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
        Assert.assertNotEquals(requestDTO.getMailBox().getStatus(), getResponseDTO.getMailBox().getStatus());

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), serviceInstanceId, aclManifest);

        Assert.assertEquals(FAILURE, reviseResponse.getResponse().getStatus());

    }

    /**
     * Method Revise MailBox with null value Should fail.
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
    public void testReviseMailBoxWithNull() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
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
        Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        reviseRequestDTO.setMailBox(null);

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), serviceInstanceId, aclManifest);

        // Assertion
        Assert.assertEquals(FAILURE, reviseResponse.getResponse().getStatus());

    }

    /**
     * Method Deactivate MailBox with valid data.
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
    public void testDeactivateMailBox() throws LiaisonException, JSONException, JsonParseException, JsonMappingException,
            JAXBException, IOException, SymmetricAlgorithmException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setName("TestMailBox" + System.currentTimeMillis());
        mailBox.setStatus("ACTIVE");
        mailBox.setTenancyKey("test Tenancy Key");
        requestDTO.setMailBox(mailBox);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        // Assertion
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

        DeActivateMailBoxResponseDTO reviseResponse = service.deactivateMailBox(getResponseDTO.getMailBox().getGuid(), aclManifest);

        Assert.assertEquals(SUCCESS, reviseResponse.getResponse().getStatus());
        Assert.assertEquals(reviseResponse.getResponse().getMessage().contains("deactivated successfully."), true);

    }

    /**
     * Method Deactivate MailBox with dummy guid Should fail.
     *
     * @throws IOException
     * @throws JAXBException
     * @throws JsonMappingException
     * @throws JsonParseException
     * @throws MailBoxConfigurationServicesException
     * @throws SymmetricAlgorithmException
     *
     */
    @Test
    public void testDeactivateMailBoxwithDummyGuid()
            throws MailBoxConfigurationServicesException, JsonParseException, JsonMappingException, JAXBException,
            IOException, SymmetricAlgorithmException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setName("TestMailBox" + System.currentTimeMillis());
        mailBox.setStatus("ACTIVE");
        mailBox.setTenancyKey("test Tenancy Key");
        requestDTO.setMailBox(mailBox);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        // Assertion
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

        DeActivateMailBoxResponseDTO reviseResponse = service.deactivateMailBox("dummy", aclManifest);

        Assert.assertEquals(FAILURE, reviseResponse.getResponse().getStatus());

    }

    /**
     * Method Search MailBox with Valid scenario.
     *
     * @throws IOException
     * @throws JAXBException
     * @throws JsonMappingException
     * @throws JsonParseException
     *
     */
    @Test
    public void testSearchMailBox()
            throws JsonParseException, JsonMappingException, JAXBException, IOException {

        // search the mailbox from the given details
        MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setMbxName("MBX_TEST");

        SearchMailBoxResponseDTO serviceResponse = mailbox.searchMailBox(searchFilter, aclManifest);

        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertEquals(serviceResponse.getResponse().getMessage(), Messages.SEARCH_SUCCESSFUL.value().replaceAll("%s", "Mailbox"));
    }

    /**
     * Method Search MailBox with Valid Profile name.
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
    public void testSearchMailBoxWithProfile()
            throws JsonParseException, JsonMappingException, JAXBException, IOException {

        // search the mailbox from the given details
        MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setProfileName("test");

        SearchMailBoxResponseDTO serviceResponse = mailbox.searchMailBox(searchFilter, aclManifest);

        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertEquals(serviceResponse.getResponse().getMessage(), Messages.SEARCH_SUCCESSFUL.value().replaceAll("%s", "Mailbox"));
    }

    /**
     * Method read property file.
     *
     */
    @Test
    public void testReadPropertyFile() {

        MailBoxConfigurationService mailbox = new MailBoxConfigurationService();
        GetPropertiesValueResponseDTO getPropResponseDTO = mailbox.readPropertiesFile();

        Assert.assertEquals(SUCCESS, getPropResponseDTO.getResponse().getStatus());
        Assert.assertEquals(getPropResponseDTO.getResponse().getMessage(), Messages.READ_JAVA_PROPERTIES_SUCCESSFULLY.value().replaceAll("%s", "Mailbox"));
    }

    /**
     * Method Get file detail using mail box.
     *
     * @throws IOException
     *
     */
    @Test
    public void testgetFileDetail()
            throws IOException {

        String jsFileLocation = MailBoxUtil.getEnvironmentProperties().getString("processor.javascript.root.directory");
        File file = new File(jsFileLocation);
        MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

        FileInfoDTO info = mailbox.getFileDetail(file);
        List<FileInfoDTO> infos = new ArrayList<FileInfoDTO>();
        infos.add(info);

        String response = MailBoxUtil.marshalToJSON(infos);
        Response resp = Response.ok(response).header("Content-Type", MediaType.APPLICATION_JSON).build();
        Assert.assertEquals(200, resp.getStatus());
    }

}
