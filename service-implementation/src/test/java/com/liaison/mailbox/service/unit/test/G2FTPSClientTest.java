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

	@Test(enabled=false)
	public void ftps() throws MailBoxServicesException, IOException, LiaisonException, URISyntaxException, FS2Exception {

		G2FTPSClient ftpsRequest = getFTPSClient(LOGGER);
		ftpsRequest.enableSessionReuse(true);
		ftpsRequest.connect();
		ftpsRequest.login();
		ftpsRequest.enableDataChannelEncryption();

		ftpsRequest.setBinary(false);
		ftpsRequest.setPassive(true);

		String path = "/inbox";
		String remotePath = "/tmp";

		ftpsRequest.changeDirectory(path);
		downloadDirectory(ftpsRequest, path, remotePath);
		ftpsRequest.disconnect();


	}

	protected G2FTPSClient getFTPSClient(Logger logger) throws LiaisonException {

		G2FTPSClient ftpsRequest = new G2FTPSClient();
		ftpsRequest.setURI("ftps://10.147.18.253:21");
		ftpsRequest.setDiagnosticLogger(logger);
		ftpsRequest.setCommandLogger(logger);
		ftpsRequest.setConnectionTimeout(60000);

		ftpsRequest.setSocketTimeout(60000);
		ftpsRequest.setRetryCount(1);

		ftpsRequest.setUser("ftpestest");
		ftpsRequest.setPassword("Welcome@123");

		return ftpsRequest;
	}

	public void downloadDirectory(G2FTPSClient ftpsClient, String currentDir, String localFileDir) throws IOException, LiaisonException, URISyntaxException, FS2Exception, MailBoxServicesException {

		String dirToList = "";
		if (!currentDir.equals("")) {
			dirToList += currentDir;
		}

		FTPFile[] files = ftpsClient.getNative().listFiles(dirToList);
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;

		if (files != null) {

			for (FTPFile file : files) {

				if (file.getName().equals(".") || file.getName().equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}

				String currentFileName = file.getName();
				if (file.isFile()) {

					String localDir = localFileDir + File.separatorChar + currentFileName;
					ftpsClient.changeDirectory(dirToList);

					try {// GSB-1337,GSB-1336

						fos = new FileOutputStream(localDir);
						bos = new BufferedOutputStream(fos);
						ftpsClient.getFile(currentFileName, bos);
					} finally {
					    if (bos != null) bos.close();
					    if (fos != null) fos.close();
					}
				} else {

					String localDir = localFileDir + File.separatorChar + currentFileName;
					String remotePath = dirToList + File.separatorChar + currentFileName;
					File directory = new File(localDir);
					if (!directory.exists()) {
						Files.createDirectories(directory.toPath());
					}
					ftpsClient.changeDirectory(remotePath);
					downloadDirectory(ftpsClient, remotePath, localDir);
				}
			}
		}
	}

}
