package com.liaison.mailbox.service.integration.test;

import java.io.IOException;

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
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.response.MailboxSLAResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

public class MailboxSLAServiceTest extends BaseServiceTest {
	
	private Logger logger = null;

	private HTTPRequest request;
	private String jsonRequest;
	
	private String jsonResponse;

	@BeforeMethod
	public void setUp() throws Exception {
		logger = LogManager.getLogger(MailBoxServiceTest.class);
	}

	
	@Test
	public void testMailboxWatchDog() throws LiaisonException, JsonParseException, 
			JsonMappingException, JAXBException, IOException {
		// Get the executing processors
		String url = getBASE_URL() + "/sla";
		request = constructHTTPRequest(url, HTTP_METHOD.POST, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		MailboxSLAResponseDTO mailboxSLAResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, MailboxSLAResponseDTO.class);

		// Assertion
		Assert.assertEquals(SUCCESS, mailboxSLAResponseDTO.getResponse().getStatus());
	}
	
	@Test
	public void testInvokeWatchDogQueue() {
		
	}
}
