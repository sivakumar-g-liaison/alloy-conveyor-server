/*
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.hornetq.util;

import java.io.IOException;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class HornetQMessageListnerServlet extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(HornetQMessageListnerServlet.class);
	private static boolean isInitialized = false;

	@Override
	public void init(ServletConfig config) throws ServletException {

		if (!isInitialized) {
			try {

				HornetQMessageListner.init();
				isInitialized = true;

			} catch (NamingException| JMSException | IOException  e) {
				logger.error(e.getMessage(), e);
				throw new ServletException("Initialization of Message Listner Failed", e);
			}
		}
	}

}
