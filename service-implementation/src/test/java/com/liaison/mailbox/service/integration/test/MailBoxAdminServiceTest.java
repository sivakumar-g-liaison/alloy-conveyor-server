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
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.FSMEventDTO;
import com.liaison.mailbox.service.dto.configuration.request.InterruptExecutionEventRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.InterruptExecutionEventResponseDTO;
import com.liaison.mailbox.service.dto.ui.GetExecutingProcessorResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * @author OFS
 *
 */
public class MailBoxAdminServiceTest extends BaseServiceTest {

	private Logger logger;
	private String jsonResponse;
	private String jsonRequest;
	private HTTPRequest request;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		logger = LogManager.getLogger(MailBoxConfigurationServiceTest.class);
	}

	/**
	 * @throws LiaisonException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testGetExecutingProcessors() throws LiaisonException, JsonParseException, 
			JsonMappingException, JAXBException, IOException {
		// Get the executing processors
		String url = getBASE_URL() + "/processoradmin/processor/execution";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		GetExecutingProcessorResponseDTO getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);

		// Assertion
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
	}
	
	/**
	 * Method to get executing processor with status
	 * 
	 * @throws LiaisonException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testGetExecutingProcessorsWithStatusOnly() throws LiaisonException, JsonParseException, 
			JsonMappingException, JAXBException, IOException {
		
		// Get the executing processors with QUEUED status
		GetExecutingProcessorResponseDTO getResponseDTO = null;
		String url = getBASE_URL() + "/processoradmin/processor/execution" + "?status=QUEUED";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		
		// Get the executing processors with PROCESSING status
		url = getBASE_URL() + "/processoradmin/processor/execution" + "?status=PROCESSING";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		
		// Get the executing processors with FAILED status
		url = getBASE_URL() + "/processoradmin/processor/execution" + "?status=FAILED";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		
		// Get the executing processors with COMPLETED status
		url = getBASE_URL() + "/processoradmin/processor/execution" + "?status=COMPLETED";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		
		// Get the executing processors with HANDED_OVER_TO_JS status
		url = getBASE_URL() + "/processoradmin/processor/execution" + "?status=HANDED_OVER_TO_JS";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		
		// Get the executing processors with GRACEFULLY_INTERRUPTED status
		url = getBASE_URL() + "/processoradmin/processor/execution" + "?status=GRACEFULLY_INTERRUPTED";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		
		// Get the executing processors with SKIPPED_SINCE_ALREADY_RUNNING status
		url = getBASE_URL() + "/processoradmin/processor/execution" + "?status=SKIPPED_SINCE_ALREADY_RUNNING";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
	}
	
	/**
	 * Method to get executing processor with date
	 * 
	 * @throws LiaisonException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testGetExecutingProcessorsWithDateOnly() throws LiaisonException, JsonParseException, JsonMappingException, JAXBException, IOException {
		
		// Get the executing processors with Date 2014-04-03 18:02:19.457
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		String frmDate = dateFormat.format(date);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		date = cal.getTime();
		String toDate = dateFormat.format(date);
		
		GetExecutingProcessorResponseDTO getResponseDTO = null;
		String url = getBASE_URL() + "/processoradmin/processor/execution"+"?frmDate=" +URLEncoder.encode(frmDate, "UTF-8")+"&toDate=" +URLEncoder.encode(toDate, "UTF-8")+ "";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
	}
	
	/**
	 * Method to get executing processor with date and status
	 * 
	 * @throws LiaisonException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testGetExecutingProcessors_WithDate_Status() throws LiaisonException, JsonParseException, JsonMappingException, JAXBException, IOException {
		
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String frmDate = dateFormat.format(date);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -2);
		date = cal.getTime();
		String toDate = dateFormat.format(date);
		
		GetExecutingProcessorResponseDTO getResponseDTO = null;
		String url = getBASE_URL() + "/processoradmin/processor/execution" + "?frmDate=" +URLEncoder.encode(frmDate, "UTF-8")+ "&toDate=" +URLEncoder.encode(toDate, "UTF-8")+ "&status=QUEUED";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		
		url = getBASE_URL() + "/processoradmin/processor/execution" + "?frmDate=" +URLEncoder.encode(frmDate, "UTF-8")+ "&toDate=" +URLEncoder.encode(toDate, "UTF-8")+ "&status=PROCESSING";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		
		url = getBASE_URL() + "/processoradmin/processor/execution" + "?frmDate=" +URLEncoder.encode(frmDate, "UTF-8")+ "&toDate=" +URLEncoder.encode(toDate, "UTF-8")+ "&status=FAILED";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		
		url = getBASE_URL() + "/processoradmin/processor/execution" + "?frmDate=" +URLEncoder.encode(frmDate, "UTF-8")+ "&toDate=" +URLEncoder.encode(toDate, "UTF-8")+ "&status=COMPLETED";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		
		url = getBASE_URL() + "/processoradmin/processor/execution" + "?frmDate=" +URLEncoder.encode(frmDate, "UTF-8")+ "&toDate=" +URLEncoder.encode(toDate, "UTF-8")+ "&status=HANDED_OVER_TO_JS";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		
		url = getBASE_URL() + "/processoradmin/processor/execution" + "?frmDate=" +URLEncoder.encode(frmDate, "UTF-8")+ "&toDate=" +URLEncoder.encode(toDate, "UTF-8")+ "&status=GRACEFULLY_INTERRUPTED";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
		
		url = getBASE_URL() + "/processoradmin/processor/execution" + "?frmDate=" +URLEncoder.encode(frmDate, "UTF-8")+ "&toDate=" +URLEncoder.encode(toDate, "UTF-8")+ "&status=SKIPPED_SINCE_ALREADY_RUNNING";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(SUCCESS, getResponseDTO.getResponse().getStatus());
	}
	
	/**
	 * Method to get executing processor with invalid status.
	 * 
	 * @throws LiaisonException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testGetExecutingProcessors_InvalidStatus_ShouldFail() throws LiaisonException, JsonParseException, JsonMappingException, JAXBException, IOException {
		
		// Get the executing processors with INVALID status
		GetExecutingProcessorResponseDTO getResponseDTO = null;
		String url = getBASE_URL() + "/processoradmin/processor/execution" + "?status=INVALID";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());
	}
	
	/**
	 * Method to get executing processor with invalid date
	 * 
	 * @throws LiaisonException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testGetExecutingProcessors_InvalidDate_ShouldFail() throws LiaisonException, JsonParseException, JsonMappingException, JAXBException, IOException {
		
		// Get the executing processors with Date 2014-04-03 18:02:19.457
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss.SSS");
		String frmDate = dateFormat.format(date);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		date = cal.getTime();
		String toDate = dateFormat.format(date);
		
		GetExecutingProcessorResponseDTO getResponseDTO = null;
		
		// ToDate is empty
		
		String url = getBASE_URL() + "/processoradmin/processor/execution" + "?frmDate=&toDate=" +URLEncoder.encode(toDate, "UTF-8")+ "";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());
		
		// FromDate is empty
		
		url = getBASE_URL() + "/processoradmin/processor/execution" + "?frmDate=" +URLEncoder.encode(frmDate, "UTF-8")+ "&toDate=";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());
	}
	
	/**
	 * Method to get executing processor with invalid date and status
	 * 
	 * @throws LiaisonException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Test
	public void testGetExecutingProcessors_InvalidDate_InvalidStatus_ShouldFail() throws LiaisonException, JsonParseException, JsonMappingException, JAXBException, IOException {
		
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss.SSS");
		String frmDate = dateFormat.format(date);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -2);
		date = cal.getTime();
		String toDate = dateFormat.format(date);
		
		GetExecutingProcessorResponseDTO getResponseDTO = null;
		String url = getBASE_URL() + "/processoradmin/processor/execution" + "?frmDate=" +URLEncoder.encode(frmDate, "UTF-8")+ "&toDate=" +URLEncoder.encode(toDate, "UTF-8")+ "&status=INVALID";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());
		
		url = getBASE_URL() + "/processoradmin/processor/execution" + "?frmDate=&toDate=" +URLEncoder.encode(toDate, "UTF-8")+ "&status=QUEUED";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());
		
		url = getBASE_URL() + "/processoradmin/processor/execution" + "?frmDate=" +URLEncoder.encode(frmDate, "UTF-8")+ "&toDate=&status=QUEUED";
		request = constructHTTPRequest(url, HTTP_METHOD.GET, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		getResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, GetExecutingProcessorResponseDTO.class);
		Assert.assertEquals(FAILURE, getResponseDTO.getResponse().getStatus());
	}
	
	/**
	 * Method to interrupt the execution of running processor.
	 * 
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void interruptRunningProcessor() throws JsonParseException, JsonMappingException, JAXBException, IOException, LiaisonException {
		
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/processor/interruptExecutionRequest.json");
		InterruptExecutionEventRequestDTO requestDTO = MailBoxUtil.unmarshalFromJSON(jsonRequest, InterruptExecutionEventRequestDTO.class);

		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);
		request = constructHTTPRequest(getBASE_URL() + "/processoradmin/processor/execution", HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		InterruptExecutionEventResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, InterruptExecutionEventResponseDTO.class);
		Assert.assertEquals(SUCCESS, responseDTO.getResponse().getStatus());
	}
	
	/**
	 * Method to interrupt the execution of running processor with empty executionID.
	 * 
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws LiaisonException
	 */
	@Test
	public void interruptRunningProcessor_EmptyExecutionID_ShouldFail() throws JsonParseException, JsonMappingException, JAXBException, IOException, LiaisonException {
		
		jsonRequest = ServiceUtils.readFileFromClassPath("requests/processor/interruptExecutionRequest.json");
		InterruptExecutionEventRequestDTO requestDTO = MailBoxUtil.unmarshalFromJSON(jsonRequest, InterruptExecutionEventRequestDTO.class);

		FSMEventDTO fsm = new FSMEventDTO();
		fsm.setExecutionID("");
		requestDTO.setFsmEvent(fsm);
		jsonRequest = MailBoxUtil.marshalToJSON(requestDTO);
		request = constructHTTPRequest(getBASE_URL() + "/processoradmin/processor/execution", HTTP_METHOD.POST, jsonRequest, logger);
		request.execute();
		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		InterruptExecutionEventResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, InterruptExecutionEventResponseDTO.class);
		Assert.assertEquals(FAILURE, responseDTO.getResponse().getStatus());
	}
}
