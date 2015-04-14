/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.persistence.exceptions.JAXBException;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabCommit;

import com.google.gson.Gson;
import com.liaison.commons.jaxb.JAXBUtility;

/**
 * Handles all script related operations
 * 
 * @author OFS
 */

public class GitLabService {

	public static final String DEFAULT_SCRIPT_PACKAGE_NAME = "com.liaison.gitlab";
	public static final String DEFAULT_BRANCH_NAME = "master";
	private static final String DEFAULT_SERVICE_NAME = "MailBox";
	private static final String GITLAB_BASE_SCHEME = "gitlab:/";

	private GitlabAPI api;
	private String projectId;
	private String serviceIp;
	private String userId;


	public GitLabService(String url, String privateToken, String projectId, String serviceIp, String userId) {
		this.api = GitlabAPI.connect(url, privateToken);
		this.projectId = projectId;
		this.serviceIp = serviceIp;
		this.userId = userId;
	}

	/**
	 * Creates script file
	 * 
	 * @param filePath
	 * @param branchName
	 * @param content
	 * @param commitMessage
	 * @return String
	 * @throws IOException
	 * @throws JAXBException
	 * @throws javax.xml.bind.JAXBException
	 */
	public String createNewFile(String filePath, String branchName, String content, String commitMessage)
			throws IOException, JAXBException, javax.xml.bind.JAXBException {

		String commitMessageWithAuditInfo = constructCommitMessage(commitMessage);
		Object obj = api.dispatch()
				.with("file_path", parseUri(filePath))
				.with("branch_name", branchName)
				.with("content", content)
				.with("commit_message", commitMessageWithAuditInfo)
				.to(String.format("/projects/%s/repository/files", projectId), Object.class);
		return JAXBUtility.marshalToJSON(obj);
	}

	/**
	 * Method revise script file
	 * 
	 * @param filePath
	 * @param branchName
	 * @param content
	 * @param commitMessage
	 * @return String
	 * @throws IOException
	 * @throws JAXBException
	 * @throws javax.xml.bind.JAXBException
	 */
	public String updateFile(String filePath, String branchName, String content, String commitMessage)
			throws IOException, JAXBException, javax.xml.bind.JAXBException {
		String commitMessageWithAuditInfo = constructCommitMessage(commitMessage);
		Object obj = api.dispatch()
				.method("PUT")
				.with("file_path", parseUri(filePath))
				.with("branch_name", branchName)
				.with("content", content)
				.with("commit_message", commitMessageWithAuditInfo)
				.to(String.format("/projects/%s/repository/files", projectId), Object.class);
		return JAXBUtility.marshalToJSON(obj);
	}

	/**
	 * Get the script using git uri.
	 * 
	 * @param sha
	 * @param filePath
	 * @return String
	 * @throws IOException
	 */
	public String getFileContent(String sha, String filePath)
			throws IOException {
		return api.retrieve().to(
				String.format("/projects/%s/repository/commits/master/blob?filepath=%s", projectId, parseUri(filePath)));
	}

	/**
	 * get gitlab commit history.
	 * 
	 * @return List<GitlabCommit>
	 * @throws IOException
	 * @throws JAXBException
	 */
	@SuppressWarnings("rawtypes")
	public List<GitlabCommit> getCommitHistory()
			throws IOException, JAXBException {
		LinkedHashMap[] mapArr = api.retrieve().to(String.format("/projects/%s/repository/commits", projectId),
				LinkedHashMap[].class);

		Gson gson = new Gson();
		List<GitlabCommit> list = new ArrayList<GitlabCommit>();
		for (LinkedHashMap map : mapArr) {
			// map needs to be converted to Json first
			String mapString = gson.toJson(map);
			GitlabCommit commit = GitlabAPI.MAPPER.readValue(mapString, GitlabCommit.class);
			list.add(commit);
		}
		return list;
	}

	public void setProjectId(String id) {
		this.projectId = id;
	}

	public String getProjectId() {
		return this.projectId;
	}

	private String parseUri(String uri) {
		return uri.replace(GITLAB_BASE_SCHEME, "");
	}

	private String constructCommitMessage(String message) {
		StringBuilder sb = new StringBuilder();
		if (serviceIp == null) {
			sb.append(String.format("[%s]", DEFAULT_SERVICE_NAME));
		} else {
			sb.append(String.format("[%s:%s]", DEFAULT_SERVICE_NAME, serviceIp));
		}

		if (userId != null) {
			sb.append(String.format("[%s] ", userId));
		} else {
			sb.append(String.format("[%s] ", "Unknown User"));
		}

		sb.append(message);

		return sb.toString();
	}

}
