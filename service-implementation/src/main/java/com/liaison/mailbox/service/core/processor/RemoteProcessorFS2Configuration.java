package com.liaison.mailbox.service.core.processor;

import java.io.File;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.liaison.framework.fs2.api.FS2Configuration;
import com.liaison.framework.fs2.api.FS2DefaultConfiguration;

public class RemoteProcessorFS2Configuration extends FS2DefaultConfiguration implements FS2Configuration{
	
	final Properties props;

	  public RemoteProcessorFS2Configuration() {
	    File defaultMount = FileUtils.getTempDirectory();
	    defaultMount = new File(defaultMount, "fs2");
	    props = new Properties();
	  //TODO read it from properties file say g2mailboxservice-dev.properties
	    props.setProperty("mount-point", "C:/fs2test");
	    props.setProperty("delete-existing-data","true");
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
