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

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * 
 * 
 * @author veerasamyn
 */
public class JSONUtilityTest extends BaseServiceTest {

	private static final Logger logger = LogManager.getLogger(JSONUtilityTest.class);
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
	}

	/*
	 * @Test public void jsonTest() throws JsonParseException, JsonMappingException, JAXBException,
	 * IOException { String data =
	 * ServiceUtils.readFileFromClassPath("response/mailbox/addmailboxresponse.json");
	 * AddMailBoxResponseDTO responseDTO = MailBoxUtil.unmarshalFromJSON(data,
	 * AddMailBoxResponseDTO.class); Assert.assertEquals(SUCCESS,
	 * responseDTO.getResponse().getStatus()); }
	 */
	
    /**
     * Method to test un-marshal from JSON.
     * 
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     */
	@Test
	public void jsonTest() throws JsonParseException, JsonMappingException, JAXBException, IOException {

		//String data = ServiceUtils.readFileFromClassPath("requests/mailbox/revisemailboxrequest.json");
		//ReviseMailBoxRequestDTO responseDTO = MailBoxUtil.unmarshalFromJSON(data, ReviseMailBoxRequestDTO.class);
		
		TriggerProcessorRequestDTO request = new TriggerProcessorRequestDTO("A", "B", "C");
		request.setExecutionId("Test");
		request.setProcessorId("testsfsdf");
		
		logger.debug(MailBoxUtil.marshalToJSON(request));
		
		String s = "{\"triggerProcessorRequest\":{\"executionId\":\"Test\",\"processorId\":\"testsfsdf\"}}";
		TriggerProcessorRequestDTO dto = MailBoxUtil.unmarshalFromJSON(s, TriggerProcessorRequestDTO.class);
		
		logger.debug(dto.getExecutionId());
		logger.debug(dto.getProcessorId());
		

	}

}
