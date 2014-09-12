package com.liaison.mailbox.service.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.spectrum.client.SpectrumClient;
import com.liaison.spectrum.client.exception.SpectrumClientException;


public class SpectrumClientUtil {
	
	private static final Logger LOGGER = LogManager.getLogger(SpectrumClientUtil.class);
	private static DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();
	
	private static final String PROPERTY_SPECTRUM_IP = "spectrum.client.username.aws";
	private static final String PROPERTY_SPECTRUM_USERNAME = "spectrum.client.username.aws";
	private static final String PROPERTY_SPECTRUM_PASSWORD = "spectrum.client.password.aws";
	private static final String PROPERTY_SPECTRUM_PORT = "spectrum.client.port.aws";
	private static final String PROPERTY_SPECTRUM_SPACE_NAME = "spectrum.client.spacename.aws";
	
	public static InputStream retrievePayloadFromSpectrum(String payloadId)  {
		
		InputStream payload = null; //For storing payload
		
				
		//read spectrum properties from property file
		String spectrumIP = configuration.getString(PROPERTY_SPECTRUM_IP);
		String spectrumUserName = configuration.getString(PROPERTY_SPECTRUM_USERNAME);
		String spectrumPwd = configuration.getString(PROPERTY_SPECTRUM_PASSWORD);
		String spectrumPort = configuration.getString(PROPERTY_SPECTRUM_PORT);
		String spectrumSpaceName = configuration.getString(PROPERTY_SPECTRUM_SPACE_NAME);
		
		SpectrumClient  spectrumClient = new SpectrumClient(spectrumIP, spectrumPort, spectrumUserName, spectrumPwd);
		String spaceId = null;
		try {
			
			/*spaceId = spectrumClient.getDatasapceIdByName(spectrumSpaceName); //get space id of particular user
			payload = spectrumClient.getLargeDataObject(spaceId, payloadId);*/
			payload = new FileInputStream("D:\\opt\\addmailbox.txt");
			
		} catch (/*SpectrumClientException*/ FileNotFoundException e) {
			LOGGER.error("Failed to retrieve payload from spectrum due to error", e);
			throw new RuntimeException("Failed to retrieve payload from spectrum due to error", e);
		}
		return payload;
	}
}
