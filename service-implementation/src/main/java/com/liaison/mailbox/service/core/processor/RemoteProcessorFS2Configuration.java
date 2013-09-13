package com.liaison.mailbox.service.core.processor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.liaison.framework.fs2.api.FS2Configuration;
import com.liaison.framework.fs2.api.FS2DefaultConfiguration;
import com.liaison.framework.util.ServiceUtils;

public class RemoteProcessorFS2Configuration extends FS2DefaultConfiguration implements FS2Configuration {

	final Properties props;

	public RemoteProcessorFS2Configuration() throws IOException {
		File defaultMount = FileUtils.getTempDirectory();
		defaultMount = new File(defaultMount, "fs2");
		props = new Properties();
		String properties = ServiceUtils.readFileFromClassPath("g2mailboxservice-dev.properties");
		InputStream is = new ByteArrayInputStream(properties.getBytes("UTF-8"));
		props.load(is);
	}

	@Override
	public String getStorageProvider() {
		return "file";
	}

	@Override
	public Properties getStorageProviderProperties() {
		return props;
	}

}
