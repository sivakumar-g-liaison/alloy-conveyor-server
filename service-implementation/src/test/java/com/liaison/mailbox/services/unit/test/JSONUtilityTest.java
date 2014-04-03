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

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Test;

import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * 
 * 
 * @author veerasamyn
 */
public class JSONUtilityTest extends BaseServiceTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/*
	 * @Test public void jsonTest() throws JsonParseException, JsonMappingException, JAXBException,
	 * IOException { String data =
	 * ServiceUtils.readFileFromClassPath("response/mailbox/addmailboxresponse.json");
	 * AddMailBoxResponseDTO responseDTO = MailBoxUtility.unmarshalFromJSON(data,
	 * AddMailBoxResponseDTO.class); Assert.assertEquals(SUCCESS,
	 * responseDTO.getResponse().getStatus()); }
	 */

	@Test
	public void jsonTest() throws JsonParseException, JsonMappingException, JAXBException, IOException {

		//String data = ServiceUtils.readFileFromClassPath("requests/mailbox/revisemailboxrequest.json");
		//ReviseMailBoxRequestDTO responseDTO = MailBoxUtility.unmarshalFromJSON(data, ReviseMailBoxRequestDTO.class);
		
		TriggerProcessorRequestDTO request = new TriggerProcessorRequestDTO("A", "B", "C");
		request.setExecutionId("Test");
		request.setProcessorId("testsfsdf");
		
		System.out.println(MailBoxUtility.marshalToJSON(request));
		
		String s = "{\"triggerProcessorRequest\":{\"executionId\":\"Test\",\"processorId\":\"testsfsdf\"}}";
		TriggerProcessorRequestDTO dto = MailBoxUtility.unmarshalFromJSON(s, TriggerProcessorRequestDTO.class);
		
		System.out.println(dto.getExecutionId());
		System.out.println(dto.getProcessorId());
		

	}

}
