package com.liaison.mailbox.service.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * @param name
	 * @param profile
	 * @param url
	 * @param id
	 */
	public void insertProfileComponents(String name, String profile, String url, String id) {

		LOGGER.info("call receive to insert the profile ::{}", profile);
		MailBoxComponent mbc = new MailBoxComponent();
		mbc.setId(id);
		mbc.setName(name);
		mbc.setUrl(url);
		mbc.setProfile(profile);

		MailBoxComponentDAO componenDao = new MailBoxComponentDAOBase();
		componenDao.persist(mbc);
	}

}
