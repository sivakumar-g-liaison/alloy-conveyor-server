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

}
