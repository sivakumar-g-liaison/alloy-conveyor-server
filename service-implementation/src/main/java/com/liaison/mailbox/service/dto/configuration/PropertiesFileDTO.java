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
	private String gitlabHost;
	private String gitlabPort;
	private String gitlabProjectName;
	private String gitlabBranchName;
	private String listJobsIntervalInHours;
	private String fsmEventCheckIntervalInSeconds;
	
	public String getFsmEventCheckIntervalInSeconds() {
		return fsmEventCheckIntervalInSeconds;
	}
	public void setFsmEventCheckIntervalInSeconds(
			String fsmEventCheckIntervalInSeconds) {
		this.fsmEventCheckIntervalInSeconds = fsmEventCheckIntervalInSeconds;
	}
	public String getGitlabHost() {
		return gitlabHost;
	}
	public void setGitlabHost(String gitlabHost) {
		this.gitlabHost = gitlabHost;
	}
	public String getGitlabPort() {
		return gitlabPort;
	}
	public void setGitlabPort(String gitlabPort) {
		this.gitlabPort = gitlabPort;
	}
	public String getGitlabProjectName() {
		return gitlabProjectName;
	}
	public void setGitlabProjectName(String gitlabProjectName) {
		this.gitlabProjectName = gitlabProjectName;
	}
	public String getGitlabBranchName() {
		return gitlabBranchName;
	}
	public void setGitlabBranchName(String gitlabBranchName) {
		this.gitlabBranchName = gitlabBranchName;
	}
	public String getTrustStoreId() {
		return trustStoreId;
	}
	public void setTrustStoreId(String trustStoreId) {
		this.trustStoreId = trustStoreId;
	}
	public String getTrustStoreGroupId() {
		return trustStoreGroupId;
	}
	public void setTrustStoreGroupId(String trustStoreGroupId) {
		this.trustStoreGroupId = trustStoreGroupId;
	}
	public String getListJobsIntervalInHours() {
		return listJobsIntervalInHours;
	}
	public void setListJobsIntervalInHours(String listJobsIntervalInHours) {
		this.listJobsIntervalInHours = listJobsIntervalInHours;
	}
}
