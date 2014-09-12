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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.fs2.api.FS2MetaSnapshot;
import com.liaison.fs2.api.FlexibleStorageSystem;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ServerListenerResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.FS2InstanceCreator;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Class to receive the payload and persist to the folder.
 * 
 * @author veerasamyn
 */
public class HTTPServerListenerService {

	private static final Logger LOG = LogManager.getLogger(HTTPServerListenerService.class);

	private static final String PAYLOAD = "Payload";

	/**
	 * Server listener to receive the payload and persist to the file.
	 * 
	 * @param payload
	 *            The payload data
	 * @param folderPath
	 *            The directory location to persist the payload
	 * @param filename
	 *            The file name of the payload
	 * @return ServerListenerResponseDTO
	 * @throws FS2Exception
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public ServerListenerResponseDTO serverListener(String payload, String folderPath, String filename) throws FS2Exception,
			IOException, URISyntaxException {

		ServerListenerResponseDTO serviceResponse = new ServerListenerResponseDTO();
		FlexibleStorageSystem fs2 = null;

		try {

			if (MailBoxUtil.isEmpty(folderPath)) {
				throw new MailBoxServicesException(Messages.EMPTY_VALUE, "Path");
			}

			if (MailBoxUtil.isEmpty(filename)) {
				throw new MailBoxServicesException(Messages.EMPTY_VALUE, "FileName");
			}

			if (MailBoxUtil.isEmpty(payload)) {
				throw new MailBoxServicesException(Messages.EMPTY_VALUE, "Payload");
			}

			if (folderPath.startsWith("fs2:")) {

				fs2 = FS2InstanceCreator.getFS2Instance();
				FS2MetaSnapshot folder = fs2.createObjectEntry(new URI(folderPath));
				FS2MetaSnapshot file = fs2.createObjectEntry(new URI(folder.getURI().toString() + "/" + filename));
				fs2.writePayloadFromBytes(file.getURI(), payload.getBytes());
				LOG.info("Successfully persisted the payload:" + file.getURI());

			} else {

				Path path = Paths.get(folderPath + File.separator + filename);
				Files.createDirectories(path.getParent());
				Files.createFile(path);
				Files.write(path, payload.getBytes());
				LOG.info("Successfully persisted the payload:" + path.toAbsolutePath());
			}

			serviceResponse.setResponse(new ResponseDTO(Messages.PERSIST_SUCCESS, PAYLOAD, Messages.SUCCESS));

		} catch (MailBoxServicesException e) {

			LOG.error(Messages.PERSIST_FAILURE.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.PERSIST_FAILURE, PAYLOAD, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;

		}

		return serviceResponse;
	}
}
