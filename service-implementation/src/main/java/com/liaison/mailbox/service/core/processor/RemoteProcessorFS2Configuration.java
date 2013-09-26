package com.liaison.mailbox.service.core.processor;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.liaison.framework.fs2.api.FS2Configuration;
import com.liaison.framework.fs2.api.FS2DefaultConfiguration;

public class RemoteProcessorFS2Configuration extends FS2DefaultConfiguration implements FS2Configuration {

	public RemoteProcessorFS2Configuration() throws IOException {

		String mount = String.valueOf(properties.get("mount-point"));
		File defaultMount = new File(mount);
		defaultMount = new File(defaultMount, "fs2");
	}

	@Override
	public String getStorageProvider() {
		return "file";
	}

	@Override
	public Properties getStorageProviderProperties() {
		return properties;
	}

}
