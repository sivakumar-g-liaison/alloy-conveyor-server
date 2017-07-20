/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.unit.test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.ftps.G2FTPSClient;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.service.exception.MailBoxServicesException;

/**
 * @author VNagarajan
 *
 */
public class G2FTPSClientTest {

	private static final Logger LOGGER = LogManager.getLogger(G2FTPSClientTest.class);

	@Test(enabled=true)
	public void main() throws MailBoxServicesException, IOException, LiaisonException, URISyntaxException, FS2Exception {

		G2FTPSClient ftpsRequest = getFTPSClient(LOGGER);
		ftpsRequest.enableSessionReuse(true);
		ftpsRequest.connect();
		ftpsRequest.login();
		ftpsRequest.enableDataChannelEncryption();

		ftpsRequest.setBinary(false);
		ftpsRequest.setPassive(true);

		ftpsRequest.disconnect();
	}

	protected G2FTPSClient getFTPSClient(Logger logger) throws LiaisonException {

		G2FTPSClient ftpsRequest = new G2FTPSClient();
		ftpsRequest.setURI("ftps://idwftp.idea4industry.com:990");
		ftpsRequest.setDiagnosticLogger(logger);
		ftpsRequest.setCommandLogger(logger);
		ftpsRequest.setConnectionTimeout(60000);

		ftpsRequest.setSocketTimeout(60000);

		ftpsRequest.setUser("liason");
		ftpsRequest.setPassword("VXh%HxyM");

		return ftpsRequest;
	}

}
