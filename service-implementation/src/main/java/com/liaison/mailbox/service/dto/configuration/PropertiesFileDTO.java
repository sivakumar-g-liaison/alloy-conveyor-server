/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

/**
 * 
 * 
 * @author OFS
 */

package com.liaison.mailbox.service.dto.configuration;

import java.util.List;

/**
 * Data Transfer Object to hold the properties.
 * 
 * @author OFS
 */
public class PropertiesFileDTO {
	
	private String listJobsIntervalInHours;
	private String fsmEventCheckIntervalInSeconds;
	private String processorSecureSyncUrlDisplayPrefix;
	private String processorSecureAsyncUrlDisplayPrefix;
	private String processorLowSecureSyncUrlDisplayPrefix;
    private String processorLowSecureAsyncUrlDisplayPrefix;
	private String defaultScriptTemplateName;
	private boolean deployAsDropbox;
    private List<String> clusterTypes;
    
    public String getProcessorSecureSyncUrlDisplayPrefix() {
        return processorSecureSyncUrlDisplayPrefix;
    }
    public void setProcessorSecureSyncUrlDisplayPrefix(String processorSecureSyncUrlDisplayPrefix) {
        this.processorSecureSyncUrlDisplayPrefix = processorSecureSyncUrlDisplayPrefix;
    }
    public String getProcessorSecureAsyncUrlDisplayPrefix() {
        return processorSecureAsyncUrlDisplayPrefix;
    }
    public void setProcessorSecureAsyncUrlDisplayPrefix(String processorSecureAsyncUrlDisplayPrefix) {
        this.processorSecureAsyncUrlDisplayPrefix = processorSecureAsyncUrlDisplayPrefix;
    }
    public String getProcessorLowSecureSyncUrlDisplayPrefix() {
        return processorLowSecureSyncUrlDisplayPrefix;
    }
    public void setProcessorLowSecureSyncUrlDisplayPrefix(String processorLowSecureSyncUrlDisplayPrefix) {
        this.processorLowSecureSyncUrlDisplayPrefix = processorLowSecureSyncUrlDisplayPrefix;
    }
    public String getProcessorLowSecureAsyncUrlDisplayPrefix() {
        return processorLowSecureAsyncUrlDisplayPrefix;
    }
    public void setProcessorLowSecureAsyncUrlDisplayPrefix(String processorLowSecureAsyncUrlDisplayPrefix) {
        this.processorLowSecureAsyncUrlDisplayPrefix = processorLowSecureAsyncUrlDisplayPrefix;
    }
    public String getDefaultScriptTemplateName() {
		return defaultScriptTemplateName;
	}
	public void setDefaultScriptTemplateName(String defaultScriptTemplateName) {
		this.defaultScriptTemplateName = defaultScriptTemplateName;
	}
	public String getFsmEventCheckIntervalInSeconds() {
		return fsmEventCheckIntervalInSeconds;
	}
	public void setFsmEventCheckIntervalInSeconds(
			String fsmEventCheckIntervalInSeconds) {
		this.fsmEventCheckIntervalInSeconds = fsmEventCheckIntervalInSeconds;
	}
	public String getListJobsIntervalInHours() {
		return listJobsIntervalInHours;
	}
	public void setListJobsIntervalInHours(String listJobsIntervalInHours) {
		this.listJobsIntervalInHours = listJobsIntervalInHours;
	}
	public boolean isDeployAsDropbox() {
	    return deployAsDropbox;
	}
	public void setDeployAsDropbox(boolean deployAsDropbox) {
	    this.deployAsDropbox = deployAsDropbox;
	}

    public List<String> getClusterTypes() {
        return clusterTypes;
    }

    public void setClusterTypes(List<String> clusterTypes) {
        this.clusterTypes = clusterTypes;
    }
}
