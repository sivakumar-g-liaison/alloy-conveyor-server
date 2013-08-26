package com.liaison.mailbox.service.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.grammer.dto.ProfileConfigurationRequest;
import com.liaison.mailbox.grammer.dto.ProfileConfigurationResponse;
import com.liaison.mailbox.jpa.dao.MailBoxComponentDAO;
import com.liaison.mailbox.jpa.dao.MailBoxComponentDAOBase;
import com.liaison.mailbox.jpa.model.MailBoxComponent;

/**
 * @author ganeshramr
 *
 */
public class MailBox {
	
	 private static final Logger LOGGER = LoggerFactory.getLogger(MailBox.class);
	
	public void invokeProfileComponents(String profile){
		
		LOGGER.info("Call recived to invoke components for profile ::{}",profile);
		MailBoxComponentDAO componentDao = new MailBoxComponentDAOBase();
		MailBoxComponent mbc = componentDao.find(profile);
		LOGGER.info(mbc.toString());
		
	 }

	/**
	 * Method inserts the analytic config details into the mail box.
	 * 
	 * @param serviceRequest
	 * @return
	 */
	public ProfileConfigurationResponse insertProfileComponents(ProfileConfigurationRequest serviceRequest) {

		LOGGER.info("call receive to insert the profile ::{}", serviceRequest.getProfile());
		MailBoxComponent mbc = new MailBoxComponent();
		mbc.setId(serviceRequest.getId());
		mbc.setName(serviceRequest.getName());
		mbc.setUrl(serviceRequest.getUrl());
		mbc.setProfile(serviceRequest.getProfile());

		MailBoxComponentDAO componenDao = new MailBoxComponentDAOBase();
		componenDao.persist(mbc);
		
		//Temporarily returns the id alone.
		ProfileConfigurationResponse serviceResponse = new ProfileConfigurationResponse();
		serviceResponse.setId(mbc.getId());
		
		return serviceResponse;
	}

}
