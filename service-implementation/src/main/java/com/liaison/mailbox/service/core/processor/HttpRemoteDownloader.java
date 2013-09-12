package com.liaison.mailbox.service.core.processor;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.framework.fs2.api.FS2Exception;
import com.liaison.framework.fs2.api.FS2Factory;
import com.liaison.framework.fs2.api.FS2MetaSnapshot;
import com.liaison.framework.fs2.api.FlexibleStorageSystem;
import com.liaison.mailbox.jpa.model.Processor;

public class HttpRemoteDownloader implements MailBoxProcessor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectorySweeper.class);
	private Processor configurationInstance ;
	
	@SuppressWarnings("unused")
	private HttpRemoteDownloader() {
		//to force creation of instance only by passing the processor entity
	}
	
	public HttpRemoteDownloader(Processor processor){
		this.configurationInstance = processor;
	}

	
	
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
	
public  static void invokeTest() throws MalformedURLException, LiaisonException, FileNotFoundException, FS2Exception, URISyntaxException {
		

		URL url = new URL("http://in.rediff.com/");		
		HTTPRequest request = new HTTPRequest(HTTP_METHOD.GET,url,LOGGER);
		//request.setOutputStream(new FileOutputStream(new File("output.txt")));
		ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
		request.setOutputStream(responseStream);
		request.execute();
		
		//WRITE RESPOSNSE TO A FILE
		FlexibleStorageSystem FS2 = FS2Factory.newInstance(new RemoteProcessorFS2Configuration());
		 URI fileLoc = new URI("fs2:"+"/response2.txt");
		FS2MetaSnapshot metaSnapShot =  FS2.createObjectEntry(fileLoc);
		FS2.writePayloadFromBytes(metaSnapShot.getURI(),responseStream.toByteArray());
		System.out.println("Wrote to "+metaSnapShot.getURI());
		
		//COPY A FILE
		
		URI copyFileLoc = new URI("fs2:"+"/responseCopy.txt");
		FS2.copy(fileLoc, copyFileLoc);	
		
		//RENAME A FILE
		  //sample data preparation
		URI originalFileTobeRenamed = new URI("fs2:"+"/filetoBeRenamed.txt");
		FS2.copy(fileLoc, originalFileTobeRenamed);	
		  //Begins
		URI renamedFileLoc = new URI("fs2:"+"/fileRenamed.txt");
		FS2.move(originalFileTobeRenamed, renamedFileLoc); 
		
		//READ A FILE
		String opt  = new String(FS2.readPayloadToBytes(renamedFileLoc));
		System.out.println(opt);
		
		//DELETE A FILE
		  //FS2.delete(renamedFileLoc);
				
		
		
	}
	
	public static void main (String argsv[]){
		try {
			try {
				invokeTest();
			} catch (FS2Exception | URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (MalformedURLException | LiaisonException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

