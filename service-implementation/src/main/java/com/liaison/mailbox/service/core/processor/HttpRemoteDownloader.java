package com.liaison.mailbox.service.core.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.commons.util.client.http.HTTPResponse;

public class HttpRemoteDownloader implements MailBoxProcessor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectorySweeper.class);

	@Override
	public  void invoke() {
		

		// initiator
		try {
			HTTPRequest.get("https://www.googleapis.com/analytics/v3/management/accounts", null).addHeader("Authorization", "");
		} catch (LiaisonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
public  static void invokeTest() throws MalformedURLException, LiaisonException, FileNotFoundException {
		

		URL url = new URL("http://in.rediff.com/");		
		HTTPRequest request = new HTTPRequest(HTTP_METHOD.GET,url,null);
		request.setOutputStream(new FileOutputStream(new File("output.txt")));
		HTTPResponse reponse = request.execute();
		System.out.println(reponse.getStatusCode());
		
		
	}
	
	public static void main (String argsv[]){
		try {
			invokeTest();
		} catch (MalformedURLException | LiaisonException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

