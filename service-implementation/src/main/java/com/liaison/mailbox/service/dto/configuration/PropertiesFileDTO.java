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

/**
 * 
 * @author OFS
 *
 */
public class PropertiesFileDTO {
	
	private String trustStoreId;
	private String trustStoreGroupId;
	private String listJobsIntervalInHours;
	private String fsmEventCheckIntervalInSeconds;
	private String processorSyncUrlDisplayPrefix;
	private String processorAsyncUrlDisplayPrefix;
	private String defaultScriptTemplateName;	
	
	public String getProcessorSyncUrlDisplayPrefix() {
		return processorSyncUrlDisplayPrefix;
	}
	public void setProcessorSyncUrlDisplayPrefix(String processorSyncUrlDisplayPrefix) {
		this.processorSyncUrlDisplayPrefix = processorSyncUrlDisplayPrefix;
	}
	public String getProcessorAsyncUrlDisplayPrefix() {
		return processorAsyncUrlDisplayPrefix;
	}
	public void setProcessorAsyncUrlDisplayPrefix(String processorAsyncUrlDisplayPrefix) {
		this.processorAsyncUrlDisplayPrefix = processorAsyncUrlDisplayPrefix;
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
}
