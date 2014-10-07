package com.liaison.mailbox.service.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.exception.BootstrapingFailedException;
import com.liaison.commons.util.bootstrap.BootstrapRemoteKeystore;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.fs2.api.FS2Factory;
import com.liaison.fs2.api.FlexibleStorageSystem;
import com.liaison.fs2.api.exceptions.FS2PayloadNotFoundException;
import com.liaison.fs2.storage.spectrum.SpectrumConfig;
import com.liaison.fs2.storage.spectrum.SpectrumConfigBuilder;
import com.liaison.fs2.storage.spectrum.SpectrumStorageConfig;
import com.liaison.mailbox.service.exception.MailBoxServicesException;


public class FS2Util {
	
	private static final Logger LOGGER = LogManager.getLogger(FS2Util.class);
	private static DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();
	
	private static FlexibleStorageSystem FS2 = null;
	
	private static final String PROPERTY_FS2_IP = "fs2.storage.spectrum.ip";
	private static final String PROPERTY_FS2_USERNAME = "fs2.storage.spectrum.username";
	private static final String PROPERTY_FS2_PASSWORD = "fs2.storage.spectrum.password";
	private static final String PROPERTY_FS2_PORT = "fs2.storage.spectrum.port";
	private static final String PROPERTY_FS2_SPACE_NAME = "fs2.storage.spectrum.spaceName";
	private static final String PROPERTY_FS2_SOURCE_NAME = "fs2.storage.spectrum.source";
	private static final String PROPERTY_FS2_DATA_DEFINITION = "fs2.storage.spectrum.dataDefinition";
	
	public static InputStream retrievePayloadFromSpectrum(String payloadURL) throws MailBoxServicesException  {
		
		InputStream payload = null; //For storing payload
		
		//read FS2 properties from property file
		/*String fs2IP = configuration.getString(PROPERTY_FS2_IP);
		String fs2UserName = configuration.getString(PROPERTY_FS2_USERNAME);
		String fs2Pwd = configuration.getString(PROPERTY_FS2_PASSWORD);
		String fs2Port = configuration.getString(PROPERTY_FS2_PORT);
		String fs2SpaceName = configuration.getString(PROPERTY_FS2_SPACE_NAME);
		String fs2SourceName =  configuration.getString(PROPERTY_FS2_SOURCE_NAME);
		String fs2DataDefinition = configuration.getString(PROPERTY_FS2_DATA_DEFINITION);
		
		final SpectrumConfig spectrumConfig = new SpectrumConfigBuilder()
												.ip(fs2IP)
												.port(fs2Port)
												.username(fs2UserName)
												.password(fs2Pwd)
												.source(fs2SourceName)
												.spaceName(fs2SpaceName)
												.dataDefinition(fs2DataDefinition)
												.dataRetentionTTL(500)
												.build();

		FlexibleStorageSystem FS2 = FS2Factory.newInstance(new SpectrumStorageConfig(spectrumConfig));*/
		try {
			getFS2Instance();
			URI spectrumURI = new URI(payloadURL);
			payload = FS2.getFS2PayloadInputStream(spectrumURI);
			//payload = new FileInputStream("D:\\opt\\addmailbox.txt");
		} catch (FS2PayloadNotFoundException | URISyntaxException  e) {
			LOGGER.error("Failed to retrieve payload from spectrum due to error", e);
			throw new MailBoxServicesException("Failed to retrieve payload from spectrum due to error"+e.getMessage(), Response.Status.BAD_REQUEST);
		}
		return payload;
	}
	
	 /**
	 * Method which creates an FS2 instance
	 * 
	 */
	public static void getFS2Instance() {
		 
        if(FS2 == null) {
            synchronized(FlexibleStorageSystem.class) {
                   if(FS2 == null) {
                	 //read FS2 properties from property file
               		String fs2IP = configuration.getString(PROPERTY_FS2_IP);
               		String fs2UserName = configuration.getString(PROPERTY_FS2_USERNAME);
               		String fs2Pwd = configuration.getString(PROPERTY_FS2_PASSWORD);
               		String fs2Port = configuration.getString(PROPERTY_FS2_PORT);
               		String fs2SpaceName = configuration.getString(PROPERTY_FS2_SPACE_NAME);
               		String fs2SourceName =  configuration.getString(PROPERTY_FS2_SOURCE_NAME);
               		String fs2DataDefinition = configuration.getString(PROPERTY_FS2_DATA_DEFINITION);
               		
               		final SpectrumConfig spectrumConfig = new SpectrumConfigBuilder()
               												.ip(fs2IP)
               												.port(fs2Port)
               												.username(fs2UserName)
               												.password(fs2Pwd)
               												.source(fs2SourceName)
               												.spaceName(fs2SpaceName)
               												.dataDefinition(fs2DataDefinition)
               												.dataRetentionTTL(500)
               												.build();
                    FS2 =  FS2Factory.newInstance(new SpectrumStorageConfig(spectrumConfig));
                 }
            }
       }
	 }        
}
