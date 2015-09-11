/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.swagger;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.SwaggerConfig;

/**
 * Class that is responsible for bootstrapping swagger.
 * 
 * @author OFS
 */
public class SwaggerBootstrap extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1212764286594977168L;
	
	private static final Logger logger = LogManager.getLogger(SwaggerBootstrap.class);

	static {

		SwaggerConfig config = new SwaggerConfig();
		String basePath = config.getBasePath();
		String[] basePathParts = null;

		if (null == basePath) {

			InetAddress ip = null;
			try {
				ip = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				logger.error("unknown host", e);
			}
			config.setBasePath("http://" + ip.getHostAddress().toString() + ":8989/g2mailboxservice");

		} else {

			basePathParts = basePath.split("/");

			InetAddress ip = null;
			try {
				ip = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				logger.error("unknown host", e);
			}

			if (!basePathParts[2].equals("localhost:8989")) {
				config.setBasePath("http://" + ip.getHostAddress().toString() + ":8989/g2mailboxservice");
			} else {
				config.setBasePath("http://localhost:8989/g2mailboxservice");
			}
		}

		ConfigFactory.setConfig(config);
	}
}