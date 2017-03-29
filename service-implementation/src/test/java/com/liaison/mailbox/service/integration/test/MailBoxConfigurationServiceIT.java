/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.integration.test;

import com.liaison.commons.util.UUIDGen;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseMailBoxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ClusterTypeResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetPropertiesValueResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.ui.SearchMailBoxDetailedResponseDTO;
import com.liaison.mailbox.service.dto.ui.SearchMailBoxResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;

import java.io.IOException;

/**
 * Test class to test mailbox configuration service.
 */
public class MailBoxConfigurationServiceIT extends BaseServiceTest {

    /**
     * Method constructs MailBox with valid data.
     */
    @Test
    public void testCreateAndGetMailBox() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        // Assertion
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
        Assert.assertEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
        Assert.assertEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
        Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());
        Assert.assertEquals(requestDTO.getMailBox().getClusterType(), getResponseDTO.getMailBox().getClusterType());

    }

    /**
     * Method constructs MailBox with valid data.
     */
    @Test
    public void testCreateAndGetMailBoxByExternalGuid() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        String guid = UUIDGen.getCustomUUID();
        mbxDTO.setGuid(guid);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        Assert.assertEquals(guid, response.getMailBox().getGuid());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        // Assertion
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
        Assert.assertEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
        Assert.assertEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
        Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());
        Assert.assertEquals(requestDTO.getMailBox().getClusterType(), getResponseDTO.getMailBox().getClusterType());

    }

    /**
     * Method to check to fail creation of MailBox with Null Value Should fail.
     */
    @Test
    public void testCreateMailBoxWithNull() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        requestDTO.setMailBox(null);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, null);

        Assert.assertEquals(FAILURE, response.getResponse().getStatus());

    }

    /**
     * Method to check to fail creation of MailBox with Empty ServiceInstanceId Should fail.
     */
    @Test
    public void testCreateMailBoxWithServiceInstanceIdEmpty() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, "", mbxDTO.getModifiedBy());

        Assert.assertEquals(FAILURE, response.getResponse().getStatus());

    }

    /**
     * Method to check to fail creation of MailBox with null ServiceInstanceId Should fail.
     */
    @Test
    public void testCreateMailBoxWithoutServiceInstanceId() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, null, mbxDTO.getModifiedBy());

        Assert.assertEquals(FAILURE, response.getResponse().getStatus());

    }

    /**
     * Method to check to pass creation of MailBox with new ServiceInstanceId.
     */
    @Test
    public void testCreateMailBoxWithNewServiceInstanceId() throws Exception {

        /// Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, MailBoxUtil.getGUID(), mbxDTO.getModifiedBy());

        Assert.assertEquals(FAILURE, response.getResponse().getStatus());

    }

    /**
     * Method to check to fail creation of MailBox without mailbox name Should fail.
     */
    @Test
    public void testCreateMailBoxWithoutName() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setStatus("ACTIVE");
        mailBox.setTenancyKey("test Tenancy Key");
        mailBox.setModifiedBy("unknown-user");
        mailBox.setClusterType(MailBoxUtil.CLUSTER_TYPE);
        requestDTO.setMailBox(mailBox);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mailBox.getModifiedBy());

        Assert.assertEquals(FAILURE, response.getResponse().getStatus());

    }

    /**
     * Method to check to fail creation of MailBox without mailbox status Should fail.
     */
    @Test
    public void testCreateMailBoxWithoutStatus() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setName("TestMailBox" + System.currentTimeMillis());
        mailBox.setTenancyKey("test Tenancy Key");
        mailBox.setModifiedBy("unknown-user");
        mailBox.setClusterType(MailBoxUtil.CLUSTER_TYPE);
        requestDTO.setMailBox(mailBox);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mailBox.getModifiedBy());

        Assert.assertEquals(FAILURE, response.getResponse().getStatus());

    }

    /**
     * Method to check to fail creation of MailBox without tenancy key Should fail.
     */
    @Test
    public void testCreateMailBoxWithoutTenancyKey() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setName("TestMailBox" + System.currentTimeMillis());
        mailBox.setStatus("ACTIVE");
        mailBox.setModifiedBy("unknown-user");
        mailBox.setClusterType(MailBoxUtil.CLUSTER_TYPE);
        requestDTO.setMailBox(mailBox);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mailBox.getModifiedBy());

        Assert.assertEquals(FAILURE, response.getResponse().getStatus());

    }

    /**
     * Method to check to fail creation of MailBox with empty tenancy key Should fail.
     */
    @Test
    public void testCreateMailBoxWithTenancyKeyEmpty() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setName("TestMailBox" + System.currentTimeMillis());
        mailBox.setStatus("ACTIVE");
        mailBox.setTenancyKey("");
        mailBox.setModifiedBy("unknown-user");
        mailBox.setClusterType(MailBoxUtil.CLUSTER_TYPE);
        requestDTO.setMailBox(mailBox);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mailBox.getModifiedBy());

        Assert.assertEquals(FAILURE, response.getResponse().getStatus());

    }

    /**
     * Method to check to fail creation of MailBox with null tenancy key Should fail.
     */
    @Test
    public void testCreateMailBoxWithTenancyKeyNull() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setName("TestMailBox" + System.currentTimeMillis());
        mailBox.setStatus("ACTIVE");
        mailBox.setTenancyKey(null);
        mailBox.setModifiedBy("unknown-user");
        mailBox.setClusterType(MailBoxUtil.CLUSTER_TYPE);
        requestDTO.setMailBox(mailBox);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mailBox.getModifiedBy());

        Assert.assertEquals(FAILURE, response.getResponse().getStatus());

    }

    /**
     * Method to check to fail creation of MailBox with Existing Mailbox Should fail.
     */
    @Test
    public void testCreateMailBoxWithExistingMailbox() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the mailbox
        requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO addMbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        addMbxDTO.setName(mbxDTO.getName());
        requestDTO.setMailBox(addMbxDTO);

        response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(FAILURE, response.getResponse().getStatus());

    }

    /**
     * Method Get MailBox by unknown organization.
     */
    @Test
    public void testGetMailBoxWithUnknownOrganization() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponse());
        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        String aclManifest_UnknownOrganization = "H4sIAAAAAAAAAM1UTU/jMBD9KysfVw1qA8uuctpAWYhUgtR6tRIIoWkym7XwR2S7EVHV/87YSSkc9sQBFMnKzDy/9zLxeMtQdyhNiyzbssoieKzntLCMpdPZaTI9To6nfJZm306yk/To9PuPWzZhjTRrkEXNMr2RcsJasKj9q9iaCp07JDYObYjYb3rhhqPzHDXoqv8pBQhn9FGNHRuAv4R1vgSFr3cv4G1u9HrW520rRQVeGE381yDk2jyxHZmQ4P8aq1h2t30JBgp2mZKWNRLPwGGdV8HtudGeUhFeGwVCj+DF4PALx+qfNtI0At10RgQDivehe+xmeZmXxW3Oi5vypVZoj1aD3DOtygd+cX4Vdwd5EmP5/LooKXMf+iY6IbHBaMKiMxtbhY1fA34MR70l9XC5Rxyq4SvwyROgBg+JgrZFS3ValXAudom4O5CbPfGhNFL/uSr4xaJYcba7303ebST8PgUaGlR0Sj7WS5Oq4YSQp05E0Ae6ecQ++SydGRuSrK15fN+JoWfCNOlhvRpI59iirpHGPQ4ZDYaPnKPmOB372Y1zoMD2h/vjDVtI3/1fIhSji2dVzm3b3QQAAA==";
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest_UnknownOrganization);

        // Assertion
        Assert.assertNotNull(getResponseDTO);
        Assert.assertNotNull(getResponseDTO.getResponse());
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertNotNull(getResponseDTO.getMailBox());
        Assert.assertEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
        Assert.assertEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
        Assert.assertEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
        Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());
        Assert.assertEquals(getResponseDTO.getMailBox().getTenancyKeyDisplayName(), getResponseDTO.getMailBox().getTenancyKey());
        Assert.assertEquals(getResponseDTO.getMailBox().getClusterType(), getResponseDTO.getMailBox().getClusterType());

    }

    /**
     * Method Get MailBox with valid data.
     */
    @Test
    public void testGetMailBoxWithAddConstraint() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), true, serviceInstanceId, aclManifest);

        // Assertion
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
        Assert.assertEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
        Assert.assertEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
        Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());
        Assert.assertEquals(requestDTO.getMailBox().getClusterType(), getResponseDTO.getMailBox().getClusterType());

    }

    /**
     * Method Get MailBox with Empty ServiceInstanceId Should fail.
     */
    @Test
    public void testGetMailBoxWithServiceInstanceIdEmpty() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), true, "", aclManifest);

        // Assertion
        Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());

    }

    /**
     * Method Get MailBox with null MailBox value Should fail.
     */
    @Test
    public void testGetMailBoxWithMailBoxNull() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox("dummy", false, serviceInstanceId, aclManifest);

        // Assertion
        Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());

    }

    /**
     * Method Revise MailBox with valid data.
     */
    @Test
    public void testReviseMailBox() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setName("RevisedMailBox" + System.currentTimeMillis());
        mbxDTO.setDescription("RevisedMailBox Desc");
        mbxDTO.setStatus("INACTIVE");
        mbxDTO.setShardKey("RevisedShard");
        mbxDTO.setClusterType(MailBoxUtil.CLUSTER_TYPE);
        mbxDTO.setGuid(getResponseDTO.getMailBox().getGuid());
        reviseRequestDTO.setMailBox(mbxDTO);

        // Assertion
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertNotEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
        Assert.assertNotEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
        Assert.assertNotEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
        Assert.assertNotEquals(requestDTO.getMailBox().getStatus(), getResponseDTO.getMailBox().getStatus());
        Assert.assertEquals(requestDTO.getMailBox().getClusterType(), getResponseDTO.getMailBox().getClusterType());

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), serviceInstanceId, true, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, reviseResponse.getResponse().getStatus());

    }

    /**
     * Method Revise MailBox with Empty ServiceInstanceId Should fail.
     */
    @Test
    public void testReviseMailBoxWithServiceInstanceIdEmpty() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setName("RevisedMailBox");
        mbxDTO.setDescription("RevisedMailBox Desc");
        mbxDTO.setStatus("INACTIVE");
        mbxDTO.setShardKey("RevisedShard");
        mbxDTO.setClusterType(MailBoxUtil.CLUSTER_TYPE);
        mbxDTO.setGuid(getResponseDTO.getMailBox().getGuid());
        reviseRequestDTO.setMailBox(mbxDTO);

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), "", true, mbxDTO.getModifiedBy());

        Assert.assertEquals(FAILURE, reviseResponse.getResponse().getStatus());

    }

    /**
     * Method Revise MailBox with dummy ServiceInstanceId Should pass.
     */
    @Test
    public void testReviseMailBoxWithDummyServiceInstanceId() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setName(mbxDTO.getName());
        mbxDTO.setDescription("RevisedMailBox Desc");
        mbxDTO.setStatus("INACTIVE");
        mbxDTO.setShardKey("RevisedShard");
        mbxDTO.setClusterType(MailBoxUtil.CLUSTER_TYPE);
        mbxDTO.setGuid(getResponseDTO.getMailBox().getGuid());
        reviseRequestDTO.setMailBox(mbxDTO);

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), "dummy" + System.currentTimeMillis(), true, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, reviseResponse.getResponse().getStatus());

    }

    /**
     * Method Revise MailBox with Wrong Guid Should fail.
     */
    @Test
    public void testReviseMailBoxWithWrongGuid() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setName("RevisedMailBox");
        mbxDTO.setDescription("RevisedMailBox Desc");
        mbxDTO.setStatus("INACTIVE");
        mbxDTO.setShardKey("RevisedShard");
        mbxDTO.setClusterType(MailBoxUtil.CLUSTER_TYPE);
        mbxDTO.setGuid(getResponseDTO.getMailBox().getGuid());
        reviseRequestDTO.setMailBox(mbxDTO);

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid() + "X", serviceInstanceId, true, mbxDTO.getModifiedBy());

        Assert.assertEquals(FAILURE, reviseResponse.getResponse().getStatus());

    }

    /**
     * Method Revise MailBox with Existing MailBox Should fail.
     */
    @Test
    public void testReviseMailBoxWithExistingMailBox() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the mailbox number 2
        MailBoxDTO secondMailbox = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(secondMailbox);

        AddMailBoxResponseDTO secondResponse = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        Assert.assertEquals(SUCCESS, secondResponse.getResponse().getStatus());

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setName(secondMailbox.getName());
        mbxDTO.setGuid(response.getMailBox().getGuid());
        reviseRequestDTO.setMailBox(mbxDTO);

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), serviceInstanceId, true, mbxDTO.getModifiedBy());

        Assert.assertEquals(FAILURE, reviseResponse.getResponse().getStatus());

    }

    /**
     * Method Revise MailBox with valid data Should fail.
     */
    @Test
    public void testReviseMailBoxWithNullMailBox() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setName("RevisedMailBox" + System.currentTimeMillis());
        mbxDTO.setDescription("RevisedMailBox Desc");
        mbxDTO.setStatus("INACTIVE");
        mbxDTO.setShardKey("RevisedShard");
        mbxDTO.setGuid("dummy");
        mbxDTO.setClusterType(MailBoxUtil.CLUSTER_TYPE);
        reviseRequestDTO.setMailBox(mbxDTO);

        // Assertion
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertNotEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
        Assert.assertNotEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
        Assert.assertNotEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
        Assert.assertNotEquals(requestDTO.getMailBox().getStatus(), getResponseDTO.getMailBox().getStatus());
        Assert.assertEquals(requestDTO.getMailBox().getClusterType(), getResponseDTO.getMailBox().getClusterType());

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), serviceInstanceId, true, mbxDTO.getModifiedBy());

        Assert.assertEquals(FAILURE, reviseResponse.getResponse().getStatus());

    }

    /**
     * Method Revise MailBox with null value Should fail.
     */
    @Test
    public void testReviseMailBoxWithNull() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        // Assertion
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        reviseRequestDTO.setMailBox(null);

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), serviceInstanceId, true, mbxDTO.getModifiedBy());

        // Assertion
        Assert.assertEquals(FAILURE, reviseResponse.getResponse().getStatus());

    }

    /**
     * Method Deactivate MailBox with valid data.
     */
    @Test
    public void testDeactivateMailBox() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setName("TestMailBox" + System.currentTimeMillis());
        mailBox.setStatus("ACTIVE");
        mailBox.setTenancyKey("test Tenancy Key");
        mailBox.setClusterType(MailBoxUtil.CLUSTER_TYPE);
        requestDTO.setMailBox(mailBox);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mailBox.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        // Assertion
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

        DeActivateMailBoxResponseDTO reviseResponse = service.deactivateMailBox(getResponseDTO.getMailBox().getGuid(), mailBox.getModifiedBy());

        Assert.assertEquals(SUCCESS, reviseResponse.getResponse().getStatus());
        Assert.assertEquals(reviseResponse.getResponse().getMessage().contains("deleted successfully."), true);

    }

    /**
     * Method Deactivate MailBox with dummy guid Should fail.
     */
    @Test
    public void testDeactivateMailBoxwithDummyGuid() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailBox = new MailBoxDTO();
        mailBox.setName("TestMailBox" + System.currentTimeMillis());
        mailBox.setStatus("ACTIVE");
        mailBox.setTenancyKey("test Tenancy Key");
        mailBox.setClusterType(MailBoxUtil.CLUSTER_TYPE);
        requestDTO.setMailBox(mailBox);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mailBox.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        // Assertion
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());

        DeActivateMailBoxResponseDTO reviseResponse = service.deactivateMailBox("dummy", mailBox.getModifiedBy());
        Assert.assertEquals(FAILURE, reviseResponse.getResponse().getStatus());

    }

    /**
     * Method Search MailBox with Valid scenario.
     */
    @Test
    public void testSearchMailBox() throws Exception {

        // search the mailbox from the given details
        MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setMbxName("MBX_TEST");
        searchFilter.setMatchMode(GenericSearchFilterDTO.MATCH_MODE_EQUALS_CHR);
        SearchMailBoxDetailedResponseDTO serviceResponse = mailbox.searchMailBox(searchFilter, aclManifest);

        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertEquals(serviceResponse.getResponse().getMessage(), Messages.SEARCH_SUCCESSFUL.value().replaceAll("%s", "Mailbox"));
    }

    /**
     * Method to search mailbox by match mode like with valid scenario. 
     */
    @Test
    public void testSearchMailBoxWithMatchModes() throws Exception {

        // search the mailbox from the given details
        MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setMbxName("MBX_TEST");
        searchFilter.setMatchMode(GenericSearchFilterDTO.MATCH_MODE_LIKE);
        SearchMailBoxDetailedResponseDTO serviceResponse = mailbox.searchMailBox(searchFilter, aclManifest);

        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertEquals(serviceResponse.getResponse().getMessage(), Messages.SEARCH_SUCCESSFUL.value().replaceAll("%s", "Mailbox"));

        searchFilter.setMatchMode(GenericSearchFilterDTO.MATCH_MODE_EQUALS_STR);
        serviceResponse = mailbox.searchMailBox(searchFilter, aclManifest);

        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertEquals(serviceResponse.getResponse().getMessage(), Messages.SEARCH_SUCCESSFUL.value().replaceAll("%s", "Mailbox"));
    }

    /**
     * Method Search MailBox with Valid Profile name.
     */
    @Test
    public void testSearchMailBoxWithProfile() throws Exception {

        // search the mailbox from the given details
        MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setProfileName("test");

        SearchMailBoxDetailedResponseDTO serviceResponse = mailbox.searchMailBox(searchFilter, aclManifest);

        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertEquals(serviceResponse.getResponse().getMessage(), Messages.SEARCH_SUCCESSFUL.value().replaceAll("%s", "Mailbox"));
    }

    /**
     * Method Search MailBox with Valid scenario.
     */
    @Test
    public void testSearchMailBoxMinResponse() throws Exception {

        // search the mailbox from the given details
        MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setMbxName("MBX_TEST");
        searchFilter.setMatchMode(GenericSearchFilterDTO.MATCH_MODE_EQUALS_CHR);
        searchFilter.setMinResponse(true);
        SearchMailBoxResponseDTO serviceResponse = mailbox.searchMailBoxUIResponse(searchFilter, aclManifest);

        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertEquals(serviceResponse.getResponse().getMessage(), Messages.SEARCH_SUCCESSFUL.value().replaceAll("%s", "Mailbox"));
    }

    /**
     * Method Search MailBox with Valid Profile name.
     */
    @Test
    public void testSearchMailBoxMinResponseWithProfile() throws Exception {

        // search the mailbox from the given details
        MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setProfileName("test");
        searchFilter.setMinResponse(true);

        SearchMailBoxResponseDTO serviceResponse = mailbox.searchMailBoxUIResponse(searchFilter, aclManifest);

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
     * Method to test read Mailbox by  Guid.
     */
    @Test
    public void testReadMailboxByGuid() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox by Guid
        GetMailBoxResponseDTO getResponseDTO = service.readMailbox(response.getMailBox().getGuid());

        // Assertion
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
        Assert.assertEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
        Assert.assertEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
        Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());
        Assert.assertEquals(requestDTO.getMailBox().getClusterType(), getResponseDTO.getMailBox().getClusterType());

    }

    /**
     * Method to test read Mailbox by  Name.
     */
    @Test
    public void testReadMailboxByName() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox by Name
        GetMailBoxResponseDTO getResponseDTO = service.readMailbox(mbxDTO.getName());

        // Assertion
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(requestDTO.getMailBox().getName(), getResponseDTO.getMailBox().getName());
        Assert.assertEquals(requestDTO.getMailBox().getDescription(), getResponseDTO.getMailBox().getDescription());
        Assert.assertEquals(requestDTO.getMailBox().getShardKey(), getResponseDTO.getMailBox().getShardKey());
        Assert.assertEquals(EntityStatus.ACTIVE.name(), getResponseDTO.getMailBox().getStatus());
        Assert.assertEquals(requestDTO.getMailBox().getClusterType(), getResponseDTO.getMailBox().getClusterType());
    }

    /**
     * Method to test read Mailbox by  invalid guid or name.
     */
    @Test
    public void testReadMailboxByInvalidGuidOrName() throws Exception {

        // Get the mailbox by Guid
        MailBoxConfigurationService service = new MailBoxConfigurationService();
        GetMailBoxResponseDTO getResponseDTO = service.readMailbox("invalid");

        // Assertion
        Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());
    }

    /**
     * Method to test read Mailbox by  Name.
     */
    @Test
    public void testReadMailboxByGuidOrNameAsNull() throws Exception {

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        // Get the mailbox by Name
        GetMailBoxResponseDTO getResponseDTO = service.readMailbox(null);
        // Assertion
        Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());
    }

    /**
     * Test method to delete the mailbox.
     *
     * @throws JAXBException
     * @throws IOException
     */
    @Test
    public void testDeleteMailBox() throws JAXBException, IOException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponse());
        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        // Assertion
        Assert.assertNotNull(getResponseDTO);
        Assert.assertNotNull(getResponseDTO.getResponse());
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setStatus(EntityStatus.DELETED.value());
        mbxDTO.setGuid(getResponseDTO.getMailBox().getGuid());
        reviseRequestDTO.setMailBox(mbxDTO);

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), serviceInstanceId, true, mbxDTO.getModifiedBy());
        Assert.assertNotNull(reviseResponse);
        Assert.assertNotNull(reviseResponse.getResponse());
        Assert.assertEquals(FAILURE, reviseResponse.getResponse().getStatus());

        DeActivateMailBoxResponseDTO deactivateResponse = service.deactivateMailBox(mbxDTO.getGuid(), "Junit");
        Assert.assertNotNull(deactivateResponse);
        Assert.assertNotNull(deactivateResponse.getResponse());
        Assert.assertEquals(SUCCESS, deactivateResponse.getResponse().getStatus());


    }

    /**
     * Test method to delete the mailbox with processor
     *
     * @throws JAXBException
     * @throws IOException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    @Test
    public void testDeleteMailboxWithProcessor() throws JAXBException, IOException, IllegalAccessException, NoSuchFieldException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponse());
        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = constructDummyProcessorDTO(response.getMailBox().getGuid(), mbxDTO);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId, procRequestDTO.getProcessor().getModifiedBy());

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getName(), procGetResponseDTO.getProcessor().getName());
        Assert.assertEquals(procRequestDTO.getProcessor().getStatus(), procGetResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessor().getType(), procGetResponseDTO.getProcessor().getType());
        Assert.assertEquals(procRequestDTO.getProcessor().getProtocol(), procGetResponseDTO.getProcessor().getProtocol());
        Assert.assertEquals(procRequestDTO.getProcessor().getClusterType(), procGetResponseDTO.getProcessor().getClusterType());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        // Assertion
        Assert.assertNotNull(getResponseDTO);
        Assert.assertNotNull(getResponseDTO.getResponse());
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setStatus(EntityStatus.DELETED.value());
        mbxDTO.setGuid(getResponseDTO.getMailBox().getGuid());
        reviseRequestDTO.setMailBox(mbxDTO);

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), serviceInstanceId, true, mbxDTO.getModifiedBy());
        Assert.assertNull(reviseResponse.getMailBox());
        Assert.assertEquals(FAILURE, reviseResponse.getResponse().getStatus());

        DeActivateMailBoxResponseDTO deactivateResponse = service.deactivateMailBox(mbxDTO.getGuid(), "Junit");
        Assert.assertNotNull(deactivateResponse);
        Assert.assertNotNull(deactivateResponse.getResponse());
        Assert.assertEquals(FAILURE, deactivateResponse.getResponse().getStatus());
        Assert.assertTrue(deactivateResponse.getResponse().getMessage().contains(Messages.MBX_NON_DELETED_PROCESSOR.value()));
    }

    /**
     * Test method to read the deleted mailbox
     *
     * @throws JAXBException
     * @throws IOException
     */
    @Test
    public void testReadDeletedMailbox() throws JAXBException, IOException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponse());
        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        // Assertion
        Assert.assertNotNull(getResponseDTO);
        Assert.assertNotNull(getResponseDTO.getResponse());
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setStatus(EntityStatus.DELETED.value());
        mbxDTO.setGuid(getResponseDTO.getMailBox().getGuid());
        reviseRequestDTO.setMailBox(mbxDTO);

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), serviceInstanceId, true, mbxDTO.getModifiedBy());
        Assert.assertNotNull(reviseResponse);
        Assert.assertNotNull(reviseResponse.getResponse());
        Assert.assertEquals(FAILURE, reviseResponse.getResponse().getStatus());

        DeActivateMailBoxResponseDTO deactivateResponse = service.deactivateMailBox(mbxDTO.getGuid(), "Junit");
        Assert.assertNotNull(deactivateResponse);
        Assert.assertNotNull(deactivateResponse.getResponse());
        Assert.assertEquals(SUCCESS, deactivateResponse.getResponse().getStatus());

        GetMailBoxResponseDTO readMailboxDTO = service.readMailbox(response.getMailBox().getGuid());

        Assert.assertNotNull(readMailboxDTO);
        Assert.assertNull(readMailboxDTO.getMailBox());
        Assert.assertEquals(FAILURE, readMailboxDTO.getResponse().getStatus());
    }

    /**
     * Test method to list the deleted mailbox
     *
     * @throws JAXBException
     * @throws IOException
     */
    @Test
    public void testListDeletedMailbox() throws JAXBException, IOException {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponse());
        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Get the mailbox
        GetMailBoxResponseDTO getResponseDTO = service.getMailBox(response.getMailBox().getGuid(), false, serviceInstanceId, aclManifest);

        // Assertion
        Assert.assertNotNull(getResponseDTO);
        Assert.assertNotNull(getResponseDTO.getResponse());
        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());

        ReviseMailBoxRequestDTO reviseRequestDTO = new ReviseMailBoxRequestDTO();
        mbxDTO.setStatus(EntityStatus.DELETED.value());
        mbxDTO.setGuid(getResponseDTO.getMailBox().getGuid());
        reviseRequestDTO.setMailBox(mbxDTO);

        ReviseMailBoxResponseDTO reviseResponse = service.reviseMailBox(reviseRequestDTO, mbxDTO.getGuid(), serviceInstanceId, true, mbxDTO.getModifiedBy());

        Assert.assertNotNull(reviseResponse);
        Assert.assertNotNull(reviseResponse.getResponse());
        Assert.assertEquals(FAILURE, reviseResponse.getResponse().getStatus());

        DeActivateMailBoxResponseDTO deactivateResponse = service.deactivateMailBox(mbxDTO.getGuid(), "Junit");
        Assert.assertNotNull(deactivateResponse);
        Assert.assertNotNull(deactivateResponse.getResponse());
        Assert.assertEquals(SUCCESS, deactivateResponse.getResponse().getStatus());

        GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
        searchFilter.setMbxName(requestDTO.getMailBox().getName());
        searchFilter.setMatchMode(GenericSearchFilterDTO.MATCH_MODE_EQUALS_STR);

        SearchMailBoxDetailedResponseDTO serviceResponse = service.searchMailBox(searchFilter, aclManifest);

        int count = 0;
        Assert.assertNotNull(serviceResponse);
        Assert.assertEquals(SUCCESS, serviceResponse.getResponse().getStatus());
        Assert.assertTrue(serviceResponse.getTotalItems() == count);
        Assert.assertTrue(serviceResponse.getMailBox().size() == count);
    }
    
    /**
     * Method to read cluster type using mailbox guid.
     */
    @Test
    public void testReadClusterTypeByMailBoxId() throws Exception {
        
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);
        
        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        
        ClusterTypeResponseDTO clusterTypeResponseDTO = service.getClusterType(response.getMailBox().getGuid());
        
        Assert.assertEquals(requestDTO.getMailBox().getClusterType(), clusterTypeResponseDTO.getClusterType());
        Assert.assertEquals(SUCCESS, clusterTypeResponseDTO.getResponse().getStatus());
        Assert.assertTrue(clusterTypeResponseDTO.getResponse().getMessage().contains(Messages.READ_SUCCESSFUL.value().replaceAll("%s", MailBoxConstants.CLUSTER_TYPE)));
    }
    
    /**
     * Method to read cluster type using invalid mailbox guid.
     */
    @Test
    public void testReadClusterTypeByInvalidMailBoxId() throws Exception {
        
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);
        
        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, mbxDTO.getModifiedBy());
        
        ClusterTypeResponseDTO clusterTypeResponseDTO = service.getClusterType(response.getMailBox().getGuid()+ "1");
        
        Assert.assertEquals(FAILURE, clusterTypeResponseDTO.getResponse().getStatus());
        Assert.assertTrue(clusterTypeResponseDTO.getResponse().getMessage().contains(Messages.READ_OPERATION_FAILED.value().replaceAll("%s", MailBoxConstants.CLUSTER_TYPE)));
    }

    /**
     * Method to read cluster type using invalid mailbox guid.
     */
    @Test
    public void testReadClusterTypeByEmptyMailBoxId() throws Exception {
        
        MailBoxConfigurationService service = new MailBoxConfigurationService();
        
        ClusterTypeResponseDTO clusterTypeResponseDTO = service.getClusterType("");
        
        Assert.assertEquals(FAILURE, clusterTypeResponseDTO.getResponse().getStatus());
        Assert.assertTrue(clusterTypeResponseDTO.getResponse().getMessage().contains(Messages.READ_OPERATION_FAILED.value().replaceAll("%s", MailBoxConstants.CLUSTER_TYPE)));
    }
    
}
