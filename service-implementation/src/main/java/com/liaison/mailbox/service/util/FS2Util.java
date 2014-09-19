package com.liaison.mailbox.service.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	
	private static final String PROPERTY_FS2_IP = "spectrum.client.serverip.aws";
	private static final String PROPERTY_FS2_USERNAME = "spectrum.client.username.aws";
	private static final String PROPERTY_FS2_PASSWORD = "spectrum.client.password.aws";
	private static final String PROPERTY_FS2_PORT = "spectrum.client.port.aws";
	private static final String PROPERTY_FS2_SPACE_NAME = "spectrum.client.spacename.aws";
	private static final String PROPERTY_FS2_SOURCE_NAME = "spectrum.client.source.aws";
	
	public static InputStream retrievePayloadFromSpectrum(String payloadURL) throws MailBoxServicesException  {
		
		InputStream payload = null; //For storing payload
		
		//read FS2 properties from property file
		String fs2IP = configuration.getString(PROPERTY_FS2_IP);
		String fs2UserName = configuration.getString(PROPERTY_FS2_USERNAME);
		String fs2Pwd = configuration.getString(PROPERTY_FS2_PASSWORD);
		String fs2Port = configuration.getString(PROPERTY_FS2_PORT);
		String fs2SpaceName = configuration.getString(PROPERTY_FS2_SPACE_NAME);
		String fs2SourceName =  configuration.getString(PROPERTY_FS2_SOURCE_NAME);
		
		final SpectrumConfig spectrumConfig = new SpectrumConfigBuilder()
												.ip(fs2IP)
												.port(fs2Port)
												.username(fs2UserName)
												.password(fs2Pwd)
												.source(fs2SourceName)
												.spaceName(fs2SpaceName)
												.dataDefinition("Payload")
												.dataRetentionTTL(500)
												.build();

		FlexibleStorageSystem FS2 = FS2Factory.newInstance(new SpectrumStorageConfig(spectrumConfig));
		try {
			URI spectrumURI = new URI(payloadURL);
			//payload = FS2.getFS2PayloadInputStream(spectrumURI);
			payload = new FileInputStream("D:\\opt\\addmailbox.txt");
		} catch (/*FS2PayloadNotFoundException |*/ URISyntaxException | FileNotFoundException e) {
			LOGGER.error("Failed to retrieve payload from spectrum due to error", e);
			throw new MailBoxServicesException("Failed to retrieve payload from spectrum due to error");
		}
		return payload;
	}
}
