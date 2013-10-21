package com.liaison.mailbox.service.core.processor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.liaison.framework.util.ServiceUtils;
import com.liaison.fs2.api.FS2Configuration;
import com.liaison.fs2.api.FS2DefaultConfiguration;
import com.netflix.config.ConfigurationManager;

public class RemoteProcessorFS2Configuration extends FS2DefaultConfiguration implements FS2Configuration {
	public final static Properties properties = new Properties();

	public RemoteProcessorFS2Configuration() throws IOException {
		Object env = ConfigurationManager.getDeploymentContext().getDeploymentEnvironment();
		String propertyFileName = "g2mailboxservice-" + env + ".properties";
		String props = ServiceUtils.readFileFromClassPath(propertyFileName);
		InputStream is = new ByteArrayInputStream(props.getBytes("UTF-8"));
		properties.load(is);
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
