/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.grammer.ResponseDTO;
import com.liaison.mailbox.grammer.dto.AddMailBoxResponseDTO;
import com.liaison.mailbox.grammer.dto.AddMailboxRequestDTO;
import com.liaison.mailbox.grammer.dto.GetMailBoxResponseDTO;
import com.liaison.mailbox.grammer.dto.MailBoxDTO;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.jpa.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.MailBox;

/**
 * Class does all configuration related stuffs.
 *
 * @author veerasamyn
 */
public class MailBoxConfigurationService {

	private static final Logger LOG = LoggerFactory.getLogger(MailBoxConfigurationService.class);

	public AddMailBoxResponseDTO createMailBox(AddMailboxRequestDTO request) {

		LOG.info("Entering into create mailbox.");
		MailBox mailBox = new MailBox();
		request.copyToEntity(mailBox);

		MailBoxConfigurationDAO config = new MailBoxConfigurationDAOBase();
		config.persist(mailBox);

		//Temporarily returns the id alone.
		AddMailBoxResponseDTO serviceResponse = new AddMailBoxResponseDTO();

		ResponseDTO response = new ResponseDTO();
		response.setMessage("Mailbox created successfully.");
		response.setStatus("Success");

		MailBoxDTO dto = new MailBoxDTO();
		dto.setGuid(String.valueOf(mailBox.getPrimaryKey()));
		serviceResponse.setMailBox(dto);

		LOG.info("Exit from create mailbox.");
		return serviceResponse;
	}

	public GetMailBoxResponseDTO getMailBox(String guid) {

		LOG.info("Exit from create mailbox.");
		MailBoxConfigurationDAO config = new MailBoxConfigurationDAOBase();
		MailBox mailBox = config.find(MailBox.class, guid);

		//Temporarily returns the id alone.
		GetMailBoxResponseDTO serviceResponse = new GetMailBoxResponseDTO();

		ResponseDTO response = new ResponseDTO();
		response.setMessage("Mailbox retrieved successfully.");
		response.setStatus("Success");

		MailBoxDTO dto = new MailBoxDTO();
		dto.copyFromEntity(mailBox);
		serviceResponse.setMailBox(dto);

		LOG.info("Exit from create mailbox.");
		return serviceResponse;
	}



}
