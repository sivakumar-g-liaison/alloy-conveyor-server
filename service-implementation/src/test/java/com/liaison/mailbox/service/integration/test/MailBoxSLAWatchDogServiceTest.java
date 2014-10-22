package com.liaison.mailbox.service.integration.test;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

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
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.rtdm.dao.FSMStateDAO;
import com.liaison.mailbox.rtdm.dao.FSMStateDAOBase;
import com.liaison.mailbox.rtdm.model.FSMState;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.dto.configuration.response.MailboxSLAResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

public class MailBoxSLAWatchDogServiceTest extends BaseServiceTest {
	
	private Logger logger = null;

	private HTTPRequest request;
	private String jsonRequest;
	
	private String jsonResponse;

	@BeforeMethod
	public void setUp() throws Exception {
		logger = LogManager.getLogger(MailBoxSLAWatchDogServiceTest.class);
		System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
		System.setProperty("archaius.deployment.environment", "ci");
	}

	
	@Test
	public void testMailboxSLAWatchDog() throws LiaisonException, JsonParseException, 
			JsonMappingException, JAXBException, IOException {
		// Get the executing processors
		String url = getBASE_URL() + "/mailboxsla";
		request = constructHTTPRequest(url, HTTP_METHOD.POST, null, logger);
		request.execute();

		jsonResponse = getOutput().toString();
		logger.info(jsonResponse);

		MailboxSLAResponseDTO mailboxSLAResponseDTO = MailBoxUtil.unmarshalFromJSON(jsonResponse, MailboxSLAResponseDTO.class);

		// Assertion
		Assert.assertEquals(SUCCESS, mailboxSLAResponseDTO.getResponse().getStatus());
	}
	
	@Test
	public void testCustomerSLAWatchDog() throws LiaisonException, JsonParseException, 
			JsonMappingException, JAXBException, IOException {
		// Get the executing processors
		String url = getBASE_URL() + "/customersla";
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
	
	@Test
	public void testRetrievalOfFileStagedEvents() {
		
		FSMStateDAO procDAO = new FSMStateDAOBase();
		long lastexecution = 1411051479753L;
		Timestamp time = new Timestamp(lastexecution);
		List<FSMState> nonSLAVerifiedFileStagedEvents = procDAO.findNonSLAVerifiedFSMEventsByValue("65153E63NULL0166NULLD6CFBAAB5FE0", time, ExecutionState.STAGED.value());
		
	}
}
