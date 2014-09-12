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
		
		final String PROPERTY_FS2_IP = "spectrum.client.serverip.aws";
		final String PROPERTY_FS2_USERNAME = "spectrum.client.username.aws";
		final String PROPERTY_FS2_PASSWORD = "spectrum.client.password.aws";
		final String PROPERTY_FS2_PORT = "spectrum.client.port.aws";
		final String PROPERTY_FS2_SPACE_NAME = "spectrum.client.spacename.aws";
		final String PROPERTY_FS2_SOURCE_NAME = "spectrum.client.source.aws";
		
		DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();
		
		//read spectrum properties from property file
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
			URI spectrumUri = new URI("hardcode the URI");
			FS2.getFS2PayloadInputStream(spectrumUri);
		} catch (FS2PayloadNotFoundException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

}
