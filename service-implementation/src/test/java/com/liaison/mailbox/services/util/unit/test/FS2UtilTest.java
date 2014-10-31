/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.services.util.unit.test;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.fs2.api.FS2Factory;
import com.liaison.fs2.api.FlexibleStorageSystem;
import com.liaison.fs2.api.exceptions.FS2PayloadNotFoundException;
import com.liaison.fs2.storage.spectrum.SpectrumConfig;
import com.liaison.fs2.storage.spectrum.SpectrumConfigBuilder;
import com.liaison.fs2.storage.spectrum.SpectrumStorageConfig;

/**
 * 
 * @author OFS
 *
 */
public class FS2UtilTest {
	
	private static final Logger logger = LogManager.getLogger(FS2UtilTest.class);
	
	/**
	 * Method to test execute JavaScript.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReadPayload() {
		
		System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
		System.setProperty("archaius.deployment.environment", "ci");
		
		final String PROPERTY_FS2_IP = "fs2.storage.spectrum.ip";
		final String PROPERTY_FS2_USERNAME = "fs2.storage.spectrum.username";
		final String PROPERTY_FS2_PASSWORD = "fs2.storage.spectrum.password";
		final String PROPERTY_FS2_PORT = "fs2.storage.spectrum.port";
		final String PROPERTY_FS2_SPACE_NAME = "fs2.storage.spectrum.spaceName";
		final String PROPERTY_FS2_SOURCE_NAME = "fs2.storage.spectrum.source";
		final String PROPERTY_FS2_DATA_DEFINITION = "fs2.storage.spectrum.dataDefinition";
		
		DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();
		
		//read spectrum properties from property file
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
		//.dataRetentionTTL(500)
		.build();

		FlexibleStorageSystem FS2 = FS2Factory.newInstance(new SpectrumStorageConfig(spectrumConfig));
		try {
			URI spectrumUri = new URI("fs2:/mllp/payload/1.0/A067FB260A0A11A611857541B17AC518");
			FS2.getFS2PayloadInputStream(spectrumUri);
		} catch (FS2PayloadNotFoundException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

}
