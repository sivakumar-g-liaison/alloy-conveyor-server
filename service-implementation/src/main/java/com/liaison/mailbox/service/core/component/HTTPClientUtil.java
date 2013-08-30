/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.commons.util.client.http.HTTPResponse;

/**
 * This class provide the HTTP web content in different format.
 * <ul><li> File </h3>
 * <li> Text <li></ul>
 * 
 * @author sivakumarg
 *
 */

public final class HTTPClientUtil {
	
	/**
	 * Download the given HTTP URL web content and return the web content in text format.
	 * 
	 * @param httpURL - web content URL. The URL protocol should be in http protocol.
	 * @return String - web content in text format
	 * @throws MalformedURLException
	 * @throws LiaisonException
	 */
	public static String getHTTPResponseInString(String httpURL, Map<String, String> headers) throws MalformedURLException, LiaisonException {
		
		String clientContent = null;
		URL url = new URL(httpURL);		
		HTTPRequest request = new HTTPRequest(HTTP_METHOD.GET,url,null);
		OutputStream output = new HTTPStringOutputStream();
		request.setOutputStream(output);
		setHeaders(request, headers);
		HTTPResponse reponse = request.execute();
		if (reponse.getStatusCode() == 200) {
			clientContent = output.toString();
		}
		return clientContent;
	}
	
	/**
	 * Download the given HTTP URL web content and write the web content in file. 
	 * 
	 * @param httpURL - web content URL. The URL protocol should be in http protocol.
	 * @param filePath - destination file path
	 * @return File - Created new file object
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 * @throws LiaisonException
	 */
	public static File getHTTPResponseInFile(String httpURL, String filePath, Map<String, String> headers) throws MalformedURLException, FileNotFoundException, LiaisonException {
		
		URL url = new URL(httpURL);		
		File file = new File(filePath);
		HTTPRequest request = new HTTPRequest(HTTP_METHOD.GET,url,null);
		FileOutputStream output = new FileOutputStream(file);			
		request.setOutputStream(output);
		setHeaders(request, headers);
		HTTPResponse reponse = request.execute();
		if (reponse.getStatusCode() == 200) {
			return file;
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
