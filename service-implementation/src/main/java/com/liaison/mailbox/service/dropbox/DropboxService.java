package com.liaison.mailbox.service.dropbox;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DropboxService {
	
	private static final Logger LOG = LogManager.getLogger(DropboxService.class);

	/**
	 * Method which will consume request from dropbox queue and 
	 * log a staged event in StagedFiles Table in DB
	 *
	 * @param request
	 */
	public void invokeDropboxQueue(String request) {
		
		LOG.info("#####################----DROPBOX INVOCATION BLOCK-AFTER CONSUMING FROM QUEUE---############################################");
		
	}
}
