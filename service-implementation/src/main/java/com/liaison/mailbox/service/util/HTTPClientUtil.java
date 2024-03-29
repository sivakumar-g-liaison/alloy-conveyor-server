/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.util;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.commons.util.client.http.HTTPResponse;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

import static com.liaison.mailbox.MailBoxConstants.BYTE_ARRAY_INITIAL_SIZE;

/**
 * This class provide the HTTP web content in different format.
 * <ul>
 * <li>File </h3>
 * <li>Text
 * <li>
 * </ul>
 * 
 * @author OFS
 * 
 */

public final class HTTPClientUtil {

	/**
	 * Download the given HTTP URL web content and return the web content in text format.
	 * 
	 * @param httpURL - web content URL. The URL protocol should be in http protocol.
	 * @return String - web content in text format
	 * @throws IOException
	 * @throws LiaisonException
	 */
	public static String getHTTPResponseInString(Logger logger, String httpURL, Map<String, String> headers)
			throws IOException, LiaisonException {

		try (OutputStream output = new ByteArrayOutputStream(BYTE_ARRAY_INITIAL_SIZE)) {

			String clientContent = null;
			URL url = new URL(httpURL);
			HTTPRequest request = new HTTPRequest(HTTP_METHOD.GET, url);
			request.setLogger(logger);
			request.setOutputStream(output);
			setHeaders(request, headers);
			int connectionTimeout = Integer.parseInt(MailBoxUtil.getEnvironmentProperties().getString(
					"kms.connection.timout.interval"));
			request.setConnectionTimeout(connectionTimeout);
			HTTPResponse reponse = request.execute();
			if (reponse.getStatusCode() == 200) {
				clientContent = output.toString();
			}
			return clientContent;
		}
	}

	/**
	 * Download the given HTTP URL web content and write the web content in file.
	 * 
	 * @param httpURL - web content URL. The URL protocol should be in http protocol.
	 * @param filePath - destination file path
	 * @return File - Created new file object
	 * @throws LiaisonException
	 * @throws IOException
	 */
	public static File getHTTPResponseInFile(Logger logger, String httpURL, String filePath, Map<String, String> headers)
			throws LiaisonException, IOException {

		URL url = new URL(httpURL);
		File file = new File(filePath);
		HTTPRequest request = new HTTPRequest(HTTP_METHOD.GET, url);
		request.setLogger(logger);
		try (FileOutputStream output = new FileOutputStream(file)) {
			request.setOutputStream(output);
			setHeaders(request, headers);
			HTTPResponse reponse = request.execute();
			if (reponse.getStatusCode() == 200) {
				return file;
			}
		}
		return null;
	}

	private static void setHeaders(HTTPRequest request, Map<String, String> headers) {

		if (headers != null && !headers.isEmpty()) {
			for (String key : headers.keySet()) {
				request.addHeader(key, headers.get(key));
			}
		}
	}
}
