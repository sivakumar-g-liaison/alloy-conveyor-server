package com.liaison.mailbox.service.util;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.framework.fs2.KeyManagerKEKProvider;
import com.liaison.framework.fs2.PayloadEncryptionProvider;
import com.liaison.fs2.api.FS2Factory;
import com.liaison.fs2.api.FlexibleStorageSystem;
import com.liaison.fs2.api.encryption.FS2EncryptionProvider;
import com.liaison.fs2.api.encryption.FS2KEKProvider;
import com.liaison.fs2.api.exceptions.FS2PayloadNotFoundException;
import com.liaison.fs2.storage.spectrum.SpectrumConfigBuilder;
import com.liaison.fs2.storage.spectrum.SpectrumStorageConfig;
import com.liaison.mailbox.service.exception.MailBoxServicesException;


public class FS2Util {

	private static final Logger LOGGER = LogManager.getLogger(FS2Util.class);
	private static FlexibleStorageSystem FS2 = null;

	public static InputStream retrievePayloadFromSpectrum(String payloadURL) throws MailBoxServicesException  {

		InputStream payload = null; //For storing payload

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
	public static synchronized void getFS2Instance() {

		if (FS2 == null) {

			FS2EncryptionProvider encryptionProvider = new PayloadEncryptionProvider();
			FS2KEKProvider kekProvider = new KeyManagerKEKProvider();
			FS2 = FS2Factory.newInstance(new SpectrumStorageConfig(SpectrumConfigBuilder.buildFromConfiguration(LiaisonConfigurationFactory.getConfiguration()), encryptionProvider, kekProvider));
		}
	 }

}
