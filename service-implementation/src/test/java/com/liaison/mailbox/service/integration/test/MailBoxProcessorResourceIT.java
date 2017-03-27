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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorCredentialPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertyUITemplateDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

public class MailBoxProcessorResourceIT extends BaseServiceTest {

    private String jsonResponse;
    private String jsonRequest;
    private HTTPRequest request;

    private Logger logger = LogManager.getLogger(MailBoxProcessorResourceIT.class);;
    private AddMailBoxResponseDTO responseDTO;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUp() throws Exception {
        // Adding the mailbox
        responseDTO = createMailBox();
    }

    /**
     * Method to test mailbox processor.
     */
    @Test
    public void testCreateProcessorToMailBox() throws Exception {

        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

        // Get Processor
        String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
                + processorResponseDTO.getProcessor().getGuId();
        request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(addProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
        Assert.assertEquals(addProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
        Assert.assertEquals(addProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(addProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor().getJavaScriptURI());
        Assert.assertEquals(false, getResponseDTO.getProcessor().getProfiles().isEmpty());

    }

    /**
     * Method to test mailbox processor without profile.
     */
    @Test
    public void testCreateProcessorToMailBox_WithoutProfile_ShouldPass() throws Exception {

        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", false);
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

        // Get Processor
        String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
                + processorResponseDTO.getProcessor().getGuId();
        request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(addProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
        Assert.assertEquals(addProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
        Assert.assertEquals(addProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(addProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor().getJavaScriptURI());

    }

    /**
     * Method to test mailbox processor without mailbox id.
     */
    @Test
    public void testCreateProcessorToMailBox_WithoutMailBoxId_ShouldFail() throws Exception {

        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", false);
        addProcessorDTO.getProcessor().setLinkedMailboxId(null);
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

    }

    /**
     * Method to test mailbox processor with wrong mailbox id.
     */
    @Test
    public void testCreateProcessorToMailBox_WithWrongMailBoxId_ShouldFail() throws Exception {

        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", false);
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + "1241234123" + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

    }

    /**
     * Method to test mailbox processor with wrong profile.
     */
    @Test
    public void testCreateProcessorToMailBox_WithWrongProfile_ShouldFail() throws Exception {

        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
        addProcessorDTO.getProcessor().getLinkedProfiles().add("dummy profiles");
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

    }

    /**
     * Method to test mailbox processor without protocol.
     */
    @Test
    public void testCreateProcessorToMailBox_WithoutProtocol_ShouldFail() throws Exception {

        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, null, true);
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

    }

    /**
     * Method to test mailbox processor with wrong protocol.
     */
    @Test
    public void testCreateProcessorToMailBox_WithWrongProtocol_ShouldFail() throws Exception {

        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "TEST", true);
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

    }

    /**
     * Method to test mailbox processor with URI as null.
     */
    @Test
    public void testReadProcessor_NullValueInURI_ShouldFail() throws Exception {

        // Get Processor
        String getProcessor = "/" + null + "/processor" + "/" + null;
        request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

        Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());

    }

    /**
     * Method to test mailbox processor with mandotory values only.
     */
    @Test
    public void testCreateProcessorToMailBox_MandotoryValuesOnly_ShouldPass() throws Exception {

        jsonRequest = MailBoxUtil.marshalToJSON(getProcessorRequest("ACTIVE",
                "REMOTEDOWNLOADER", false, "HTTP", true));

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

    }

    /**
     * Method to test mailbox processor with invalid mandotory values only.
     */
    @Test
    public void testCreateProcessorToMailBox_WithInvalidMandatoryValues_ShouldFail() throws Exception {

        jsonRequest = MailBoxUtil
                .marshalToJSON(getProcessorRequest("fdfd", "85964", false, "HTTP", true));

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());
    }

    /**
     * Method to test mailbox processor with mandotory values as null.
     */
    @Test
    public void testCreateProcessorToMailBox_WithMandatoryValuesAsNull__ShouldFail() throws Exception {

        jsonRequest = MailBoxUtil.marshalToJSON(getProcessorRequest(null, null, false, null, true));

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());
    }

    /**
     * Method to test retrieve Processor.
     */
    @Test
    public void testReadProcessor() throws Exception {

        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

        // Get Processor
        String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
                + processorResponseDTO.getProcessor().getGuId();
        request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);
        GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(addProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
        Assert.assertEquals(addProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
        Assert.assertEquals(addProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(addProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor().getJavaScriptURI());
    }

    /**
     * Method to test retrieve Processor with invalid processor id.
     */
    @Test
    public void testReadProcessor_WithInvalidProcessorId_ShouldFail() throws Exception {

        String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/" + "DummyId";
        request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);
        GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

        Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());
    }

    /**
     * Method to test retrieve Processor with invalid mailbox.
     */
    @Test
    public void testReadProcessor_WithInvalidMailBox_ShouldFail() throws Exception {

        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

        AddProcessorToMailboxRequestDTO addSecProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
        jsonRequest = MailBoxUtil.marshalToJSON(addSecProcessorDTO);

        String addProcessorReq = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessorReq, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorSecResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(SUCCESS, processorSecResponseDTO.getResponse().getStatus());

        // Get Processor
        String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
                + processorResponseDTO.getProcessor().getGuId();
        request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);
        GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(addProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
        Assert.assertEquals(addProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
        Assert.assertEquals(addProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(addProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor().getJavaScriptURI());
    }

    /**
     * Method to test revise operation for Processor with valid data.
     */
    @Test
    public void testReviseProcessor() throws Exception {

        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

        ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest("ACTIVE", "REMOTEDOWNLOADER", true, "HTTP", true);
        reviseProcessorDTO.getProcessor().setGuid(processorResponseDTO.getProcessor().getGuId());
        jsonRequest = MailBoxUtil.marshalToJSON(reviseProcessorDTO);

        String reviseProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor/"
                + processorResponseDTO.getProcessor().getGuId();
        request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        Assert.assertEquals(SUCCESS, getResponse(jsonResponse, "reviseProcessorResponse", STATUS));

        // Get Processor
        String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
                + processorResponseDTO.getProcessor().getGuId();
        request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(reviseProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
        Assert.assertEquals(reviseProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
        Assert.assertEquals(reviseProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(reviseProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor()
                .getJavaScriptURI());
    }

    /**
     * Method to test revise operation for Processor without protocol.
     */
    @Test
    public void testReviseProcessor_WithoutProtocol_ShouldFail() throws Exception {

        ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest("ACTIVE", "REMOTEDOWNLOADER", true, null, true);
        reviseProcessorDTO.getProcessor().setGuid("123456");
        jsonRequest = MailBoxUtil.marshalToJSON(reviseProcessorDTO);

        String reviseProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor/"
                + "123456";
        request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        Assert.assertEquals(FAILURE, getResponse(jsonResponse, "reviseProcessorResponse", STATUS));

    }

    /**
     * Method to test revise operation for Processor with invalid protocol.
     */
    @Test
    public void testReviseProcessor_WithInvalidProtocol_ShouldFail() throws Exception {

        ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest("ACTIVE", "REMOTEDOWNLOADER", true, "Test", true);
        reviseProcessorDTO.getProcessor().setGuid("123456");
        jsonRequest = MailBoxUtil.marshalToJSON(reviseProcessorDTO);

        String reviseProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor/"
                + "123456";
        request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        Assert.assertEquals(FAILURE, getResponse(jsonResponse, "reviseProcessorResponse", STATUS));

    }

    /**
     * Method to test revise operation for Processor without mailbox id.
     */
    @Test
    public void testReviseProcessor_WithoutMailBoxId_ShouldFail() throws Exception {

        ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", true, "HTTP", false);
        reviseProcessorDTO.getProcessor().setLinkedMailboxId(null);
        reviseProcessorDTO.getProcessor().setGuid("21412341234");
        jsonRequest = MailBoxUtil.marshalToJSON(reviseProcessorDTO);

        String reviseProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor/"
                + "123456";
        request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        ReviseProcessorResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                ReviseProcessorResponseDTO.class);
        Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

    }

    /**
     * Method to test revise operation for Processor with URI as null.
     */
    @Test
    public void testReviseProcessor_WithNullValueInURI_ShouldFail() throws Exception {

        ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", true, "HTTP", false);
        reviseProcessorDTO.getProcessor().setLinkedMailboxId("null");
        reviseProcessorDTO.getProcessor().setGuid("dfasfasfdafd");
        jsonRequest = MailBoxUtil.marshalToJSON(reviseProcessorDTO);

        String reviseProcessor = "/" + null + "/processor/" + null;
        request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        ReviseProcessorResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                ReviseProcessorResponseDTO.class);
        Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());

    }

    /**
     * Method to test revise operation for Processor with wrong mailbox id.
     */
    @Test
    public void testReviseProcessor_WithWorngMailBoxId_ShouldFail() throws Exception {

        // Add
        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

        // Revise
        ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", true, "HTTP", false);
        jsonRequest = MailBoxUtil.marshalToJSON(reviseProcessorDTO);
        reviseProcessorDTO.getProcessor().setGuid(processorResponseDTO.getProcessor().getGuId());

        String reviseProcessor = "/" + addProcessorDTO.getProcessor().getLinkedMailboxId() + "/processor/"
                + "123456";
        request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        ReviseProcessorResponseDTO revProcessorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                ReviseProcessorResponseDTO.class);
        Assert.assertEquals(FAILURE, revProcessorResponseDTO.getResponse().getStatus());

    }

    /**
     * Method to test revise operation for Processor without profile.
     */
    @Test
    public void testReviseProcessor_WithoutProfile_ShouldFail() throws Exception {

        // AddProcessor
        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

        // Get Processor
        String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
                + processorResponseDTO.getProcessor().getGuId();
        request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);
        GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(false, getResponseDTO.getProcessor().getProfiles().isEmpty());

        // Revise
        ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", true, "HTTP", false);
        reviseProcessorDTO.getProcessor().setGuid(processorResponseDTO.getProcessor().getGuId());

        jsonRequest = MailBoxUtil.marshalToJSON(reviseProcessorDTO);
        String reviseProcessor = "/" + reviseProcessorDTO.getProcessor().getLinkedMailboxId() + "/processor/"
                + processorResponseDTO.getProcessor().getGuId();
        request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        ReviseProcessorResponseDTO revProcessorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                ReviseProcessorResponseDTO.class);
        Assert.assertEquals(SUCCESS, revProcessorResponseDTO.getResponse().getStatus());

        // Get Processor
        request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);
        getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(true, getResponseDTO.getProcessor().getProfiles().isEmpty());
    }

    /**
     * Method to test revise operation for Processor with invalid profile.
     */
    @Test
    public void testReviseProcessor_WithInvaidProfile_ShouldFail() throws Exception {

        // AddProcessor
        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

        // Revise
        ReviseProcessorRequestDTO reviseProcessorDTO = (ReviseProcessorRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", true, "HTTP", true);
        reviseProcessorDTO.getProcessor().setGuid(processorResponseDTO.getProcessor().getGuId());
        reviseProcessorDTO.getProcessor().getLinkedProfiles().add("dummy profiles");

        jsonRequest = MailBoxUtil.marshalToJSON(reviseProcessorDTO);
        String reviseProcessor = "/" + reviseProcessorDTO.getProcessor().getLinkedMailboxId() + "/processor/"
                + processorResponseDTO.getProcessor().getGuId();
        request = constructHTTPRequest(getBASE_URL() + reviseProcessor, HTTP_METHOD.PUT, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        ReviseProcessorResponseDTO revProcessorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                ReviseProcessorResponseDTO.class);
        Assert.assertEquals(FAILURE, revProcessorResponseDTO.getResponse().getStatus());

    }

    /**
     * Method to test deactive processor.
     */
    @Test
    public void testDeactivateProcessor() throws Exception {

        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", true);
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

        // Deactivate the processor
        String deactProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
                + processorResponseDTO.getProcessor().getGuId();
        request = constructHTTPRequest(getBASE_URL() + deactProcessor, HTTP_METHOD.DELETE, null, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        DeActivateProcessorResponseDTO responseDeactivateDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                DeActivateProcessorResponseDTO.class);
        Assert.assertEquals(SUCCESS, responseDeactivateDTO.getResponse().getStatus());

        // Get Processor
        String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
                + processorResponseDTO.getProcessor().getGuId();
        request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetProcessorResponseDTO.class);

        Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());
    }

    /**
     * Method to test deactive processor with wrong processor id.
     */
    @Test
    public void testDeactivateProcessor_WithWrongProcessorId__ShouldFail() throws Exception {

        // Deactivate the processor
        String deactProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
                + "DummyId";
        request = constructHTTPRequest(getBASE_URL() + deactProcessor, HTTP_METHOD.DELETE, null, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        DeActivateProcessorResponseDTO responseDeactivateDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                DeActivateProcessorResponseDTO.class);
        Assert.assertEquals(FAILURE, responseDeactivateDTO.getResponse().getStatus());

    }

    /**
     * Method to test deactive processor with URI as null.
     */
    @Test
    public void testDeactivateProcessor_WithNullValueInURI__ShouldFail() throws Exception {

        // Deactivate the processor
        String deactProcessor = "/" + null + "/processor" + "/" + null;
        request = constructHTTPRequest(getBASE_URL() + deactProcessor, HTTP_METHOD.DELETE, null, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        DeActivateProcessorResponseDTO responseDeactivateDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                DeActivateProcessorResponseDTO.class);
        Assert.assertEquals(FAILURE, responseDeactivateDTO.getResponse().getStatus());

    }

    /**
     * Method to create local folders if not available.
     */
    @Test
    public void testCreateProcessorToMailBox_CreateFolderIfNotAvailable() throws Exception {

        // create processor with valid folder pattern
        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", false);
        addProcessorDTO.getProcessor().setCreateConfiguredLocation(false);
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(SUCCESS, processorResponseDTO.getResponse().getStatus());

        // Get Processor
        String getProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "/"
                + processorResponseDTO.getProcessor().getGuId();
        request = constructHTTPRequest(getBASE_URL() + getProcessor, HTTP_METHOD.GET, null, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        GetProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                GetProcessorResponseDTO.class);

        Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
        Assert.assertEquals(addProcessorDTO.getProcessor().getName(), getResponseDTO.getProcessor().getName());
        Assert.assertEquals(addProcessorDTO.getProcessor().getType(), getResponseDTO.getProcessor().getType());
        Assert.assertEquals(addProcessorDTO.getProcessor().getStatus(), getResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(addProcessorDTO.getProcessor().getJavaScriptURI(), getResponseDTO.getProcessor()
                .getJavaScriptURI());

    }

    /**
     * Method to test create processor with invalid folder.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     */
    @Test
    public void testCreateProcessorToMailBox_WithInValidFolder() throws Exception {

        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", false);
        constructFolderProperties(addProcessorDTO.getProcessor().getProcessorPropertiesInTemplateJson(),
                "\\folderTest\\test\\ftpup\\inbox\\", "/FTPUp/DEVTEST/");
        addProcessorDTO.getProcessor().setCreateConfiguredLocation(true);
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());
    }

    /**
     * Method to test create processor with empty folder.
     *
     * @throws LiaisonException
     * @throws JSONException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     */
    @Test
    public void testCreateProcessorToMailBox_WithEmptyFolder() throws Exception {

        AddProcessorToMailboxRequestDTO addProcessorDTO = (AddProcessorToMailboxRequestDTO) getProcessorRequest(
                "ACTIVE", "REMOTEDOWNLOADER", false, "HTTP", false);
        constructFolderProperties(addProcessorDTO.getProcessor().getProcessorPropertiesInTemplateJson(), "", "");
        addProcessorDTO.getProcessor().setCreateConfiguredLocation(true);
        jsonRequest = MailBoxUtil.marshalToJSON(addProcessorDTO);

        String addProcessor = "/" + responseDTO.getMailBox().getGuid() + "/processor" + "?sid=" + serviceInstanceId;
        request = constructHTTPRequest(getBASE_URL() + addProcessor, HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();

        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProcessorToMailboxResponseDTO processorResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse,
                AddProcessorToMailboxResponseDTO.class);
        Assert.assertEquals(FAILURE, processorResponseDTO.getResponse().getStatus());
    }


    /**
     * Method constructs Mailbox.
     */
    private AddMailBoxResponseDTO createMailBox() throws Exception {

        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);
        request = constructHTTPRequest(getBASE_URL() + "?sid=" + serviceInstanceId, HTTP_METHOD.POST, jsonRequest, logger);
        request.addHeader("acl-manifest", aclManifest);
        request.execute();
        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddMailBoxResponseDTO.class);
        Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());
        return responseDTO;
    }

    /**
     * Method retrieves processor request.
     */
    private Object getProcessorRequest(String processorStatus, String processorType, boolean isRevise,
                                       String protocolType, boolean setProfile) throws Exception {

        ProcessorDTO processorDTO = new ProcessorDTO();
        ProcessorPropertyUITemplateDTO processorProperties = null;

        // constructing ProcessorPropertyUITemplateDTO and staticProperies
        String propertiesJson = ServiceUtils.readFileFromClassPath("processor/properties/REMOTEDOWNLOADER.HTTP.json");
        processorProperties = MailBoxUtil.unmarshalFromJSON(propertiesJson, ProcessorPropertyUITemplateDTO.class);

        processorDTO.setProcessorPropertiesInTemplateJson(processorProperties);

        // constructing folderDTO
        constructFolderProperties(processorDTO.getProcessorPropertiesInTemplateJson(), "\\data\\ftp\\ftpup\\inbox\\",
                "/FTPUp/DEVTEST/");
        // constructing credential dto
        List<ProcessorCredentialPropertyDTO> credentialList = new ArrayList<ProcessorCredentialPropertyDTO>();
        ProcessorCredentialPropertyDTO credentialDTO = new ProcessorCredentialPropertyDTO();
        credentialDTO.setCredentialDisplayType("Login Credential");
        credentialDTO.setCredentialType("LOGIN_CREDENTIAL");
        credentialDTO.setCredentialURI("g2testusr");
        credentialDTO.setUserId("73626AAD0A92650214238B0AFF84EE21");
        credentialDTO.setValueProvided(true);
        credentialList.add(credentialDTO);
        processorDTO.getProcessorPropertiesInTemplateJson().setCredentialProperties(credentialList);

        processorDTO.setStatus(processorStatus);
        processorDTO.setName(System.currentTimeMillis() + "");

        processorDTO.setDescription("CREATING PROCESSOR");
        processorDTO.setClusterType(MailBoxUtil.CLUSTER_TYPE);

        processorDTO.setType(processorType);
        processorDTO.setProtocol(protocolType);
        processorDTO.setLinkedMailboxId(responseDTO.getMailBox().getGuid());

        if (setProfile) {

            String profileName = "TestProfile" + System.nanoTime();
            AddProfileResponseDTO response = addProfile(profileName);
            Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

            Set<String> profiles = new HashSet<>();
            profiles.add(profileName);
            processorDTO.setLinkedProfiles(profiles);
        }

        if (isRevise) {
            ReviseProcessorRequestDTO reviseDTO = new ReviseProcessorRequestDTO();
            reviseDTO.setProcessor(processorDTO);
            return reviseDTO;
        } else {
            AddProcessorToMailboxRequestDTO addProcessorDTO = new AddProcessorToMailboxRequestDTO();
            addProcessorDTO.setProcessor(processorDTO);
            return addProcessorDTO;
        }
    }    

    /**
     * Method to add profile.
     *
     * @param profileName
     * @return AddProfileResponseDTO
     * @throws JAXBException
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     * @throws MalformedURLException
     * @throws FileNotFoundException
     * @throws LiaisonException
     * @throws JsonParseException
     */
    private AddProfileResponseDTO addProfile(String profileName) throws JAXBException, JsonGenerationException,
            JsonMappingException, IOException, MalformedURLException, FileNotFoundException, LiaisonException, JsonParseException {

        ProfileDTO profile = new ProfileDTO();
        profile.setName(profileName);
        AddProfileRequestDTO profileRequstDTO = new AddProfileRequestDTO();
        profileRequstDTO.setProfile(profile);

        jsonRequest = MailBoxUtil.marshalToJSON(profileRequstDTO);
        request = constructHTTPRequest(getBASE_URL() + "/profile", HTTP_METHOD.POST, jsonRequest, logger);
        request.execute();
        jsonResponse = getOutput().toString();
        logger.info(jsonResponse);

        AddProfileResponseDTO profileResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, AddProfileResponseDTO.class);

        return profileResponseDTO;
    }
}
