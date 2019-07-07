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

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gitlab.api.models.GitlabCommit;

import com.liaison.commons.util.StringUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.request.ScriptServiceDTO;
import com.liaison.mailbox.service.dto.configuration.response.GitlabCommitResponse;
import com.liaison.mailbox.service.dto.configuration.response.ScriptServiceResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.validation.GenericValidator;
import com.liaison.util.gitlab.GitLabService;

/**
 * Handles all script related operations
 *
 * @author OFS
 */
public class ScriptService {

	private static final Logger LOGGER = LogManager.getLogger(ScriptService.class);
	private GitLabService gitlab;
	private static final String SCRIPT = "Script File";

	public ScriptService(String serviceIp, String userId)
			throws IOException {

		String gitlabServerHost = MailBoxUtil.getEnvironmentProperties().getString(
				MailBoxConstants.PROPERTY_GITLAB_ACTIVITY_SERVER_HOST);
		String gitlabPrivateToken = MailBoxUtil.getEnvironmentProperties().getString(
				MailBoxConstants.PROPERTY_GITLAB_ACTIVITY_PRIVATE_TOKEN);
		String gitlabProjectId = MailBoxUtil.getEnvironmentProperties().getString(
				MailBoxConstants.PROPERTY_GITLAB_ACTIVITY_PROJECT_ID);

		gitlab = new GitLabService(gitlabServerHost, gitlabPrivateToken, gitlabProjectId, serviceIp, userId);
	}

	/**
	 * Creates User script
	 *
	 * @param scriptRequest
	 * @return ScriptServiceResponseDTO
	 * @throws JAXBException
	 */
	public ScriptServiceResponseDTO createScript(ScriptServiceDTO scriptRequest) {

		LOGGER.debug("Entered into createScript() method");
        LOGGER.debug("The script uri is  {}", scriptRequest.getScriptFileUri());
		ScriptServiceResponseDTO serviceResponse = new ScriptServiceResponseDTO();
		GenericValidator validator = new GenericValidator();
		String uri = scriptRequest.getScriptFileUri();

		try {

			validator.validate(scriptRequest);

			if (StringUtil.isNullOrEmptyAfterTrim(scriptRequest.getData())) {
				scriptRequest.setData(MailBoxConstants.DEFAULT_SCRIPT_TMPLATE_CONTENT);
			}
			uri = checkGitLabUrl(uri);

			gitlab.createNewFile(uri, "master", scriptRequest.getData(), "File [" + uri + "] created by user "
					+ scriptRequest.getCreatedBy());
			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, SCRIPT, Messages.SUCCESS));
			LOGGER.debug("Exit from createScript() method");

			return serviceResponse;

		} catch (Exception e) {
			LOGGER.error(Messages.CREATE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATE_OPERATION_FAILED, SCRIPT, Messages.FAILURE,
					e.getMessage()));

			return serviceResponse;
		}
	}

	/**
	 * Get the script file from git by given git uri.
	 *
	 * @param uri
	 * @param commitSha
	 * @return ScriptServiceResponseDTO
	 */
	public ScriptServiceResponseDTO getScript(String encodedUri, String commitSha) {

		LOGGER.debug("Entered into getScript() method");
        LOGGER.debug("The retrieve script uri is {} ", encodedUri);
		ScriptServiceResponseDTO serviceResponse = new ScriptServiceResponseDTO();
		String script = null;
		String[] urlParts = null;
		String uri = null;

		try {

			//decoding the encoded uri
		    uri = new String(Base64.decodeBase64(encodedUri));
			if (StringUtil.isNullOrEmptyAfterTrim(uri)) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
			}

			uri = checkGitLabUrl(uri);
			urlParts = uri.split(":/");
			if ("gitlab".equalsIgnoreCase(urlParts[0])) {
				uri = urlParts[1];
			}
			if (MailBoxUtil.isEmpty(script)) {
				// if specific commit is chosen
				if (commitSha != null && !commitSha.isEmpty()) {
					script = gitlab.getFileContent(commitSha, uri);
				}
				// else get latest file
				else {
					String latestCommit = gitlab.getCommitHistory().get(0).getShortId();
					script = gitlab.getFileContent(latestCommit, uri);
				}
				script = gitlab.getFileContent(commitSha, uri);
			}

			// response message construction
			serviceResponse.setScript(script);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, SCRIPT, Messages.SUCCESS));
			LOGGER.debug("Exit from getScript() method");

			return serviceResponse;

		} catch (Exception e) {

			LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, SCRIPT, Messages.FAILURE,
					e.getMessage()));

			return serviceResponse;
		}
	}


	/**
	 * method revise script file.
	 *
	 * @param scriptRequest
	 * @return
	 */
	public ScriptServiceResponseDTO updateScript(ScriptServiceDTO scriptRequest) {

		LOGGER.debug("Entered into updateScript() method");
        LOGGER.debug("The retrieve script uri is  ", scriptRequest.getScriptFileUri());
		ScriptServiceResponseDTO serviceResponse = new ScriptServiceResponseDTO();
		GenericValidator validator = new GenericValidator();

		try {
			validator.validate(scriptRequest);
			String uri = scriptRequest.getScriptFileUri();
			uri = checkGitLabUrl(uri);

			gitlab.updateFile(uri, "master", scriptRequest.getData(), "File [" + uri + "] updated by user "
					+ scriptRequest.getCreatedBy());

			serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY, SCRIPT, Messages.SUCCESS));
			LOGGER.debug("Exit from updateScript() method.");

			return serviceResponse;

		} catch (Exception e) {

			LOGGER.error(Messages.REVISE_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.REVISE_OPERATION_FAILED, SCRIPT, Messages.FAILURE,
					e.getMessage()));
			return serviceResponse;
		}

	}

	/**
	 * This method to check script uri contains gitlab or not
	 *
	 * @param uri
	 * @return String
	 * @throws IOException
	 */
	private String checkGitLabUrl(String uri)
			throws IOException {

		if (!uri.toLowerCase().contains("gitlab:")) {
			String gitlabDirectory = (String) MailBoxUtil.getEnvironmentProperties().getProperty(
					MailBoxConstants.PROPERTY_GITLAB_ACTIVITY_SERVER_FOLDER);
			uri = gitlabDirectory + "/" + uri;
		}
		return uri;

	}
	
	/**
     * Expects format of gitlab:/somepath/somefile
     *
     * @param url
     * @return
     */
    public GitlabCommitResponse getFileLastCommitHistory(String url) {
        
        GitlabCommit gitlabResponse = null;
        GitlabCommitResponse serviceResponse = new GitlabCommitResponse();

        url = url.substring(8); // strip off "gitlab:/"

        try {
            gitlabResponse = gitlab.getFileLastCommitHistory(url);
            serviceResponse.setGitlabCommit(gitlabResponse);
            serviceResponse.setStatus(Response.Status.OK.getStatusCode());
            serviceResponse.setMessage("Commit history retrived successcully");
        } catch (IOException e) {
            serviceResponse.setStatus(Response.Status.NOT_FOUND.getStatusCode());
        }
        
        return serviceResponse;
    }

}
