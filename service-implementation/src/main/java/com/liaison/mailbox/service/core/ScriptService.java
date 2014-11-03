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
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.request.ScriptServiceDTO;
import com.liaison.mailbox.service.dto.configuration.response.ScriptServiceResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.validation.GenericValidator;

/**
 * Handles all script related operations
 *
 * @author OFS
 */
public class ScriptService {
	
	private static final Logger LOGGER = LogManager.getLogger(ScriptService.class);
	private GitLabService gitlab;
	private static final Map<String, String> cache = new HashMap<>();
	private static final String SCRIPT = "Script File";
	
	public ScriptService() throws IOException {

		String gitlabServerHost = MailBoxUtil.getEnvironmentProperties().getString(
				MailBoxConstants.PROPERTY_GITLAB_ACTIVITY_SERVER_HOST);
		String gitlabPrivateToken = MailBoxUtil.getEnvironmentProperties().getString(
				MailBoxConstants.PROPERTY_GITLAB_ACTIVITY_PRIVATE_TOKEN);
		String gitlabProjectId = MailBoxUtil.getEnvironmentProperties().getString(
				MailBoxConstants.PROPERTY_GITLAB_ACTIVITY_PROJECT_ID);

		gitlab = new GitLabService(gitlabServerHost, gitlabPrivateToken, gitlabProjectId);
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
		LOGGER.info("The retrieve script uri is  ", scriptRequest.getScriptFileUri());
		ScriptServiceResponseDTO serviceResponse = new ScriptServiceResponseDTO();
		GenericValidator validator = new GenericValidator();
		String uri = scriptRequest.getScriptFileUri();
		
		try {
			
			validator.validate(scriptRequest);			
			uri = checkGitLabUrl(uri);
			
			gitlab.createNewFile(uri, "master", scriptRequest.getData(), "File [" + uri + "] created by user " + scriptRequest.getCreatedBy());
			// response message construction
			serviceResponse.setResponse(new ResponseDTO(Messages.CREATED_SUCCESSFULLY, SCRIPT, Messages.SUCCESS));	
			
			// refresh cache during update
			String[] urlParts = null;
			urlParts = uri.split(":/");
			if ("gitlab".equalsIgnoreCase(urlParts[0])) {
				uri = urlParts[1];
			}
			
			cache.put(uri, scriptRequest.getData());
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
	public ScriptServiceResponseDTO getScript(String uri, String commitSha) {
		
		LOGGER.debug("Entered into getScript() method");
		LOGGER.info("The retrieve script uri is  ", uri);
		ScriptServiceResponseDTO serviceResponse = new ScriptServiceResponseDTO();
		String script;
		String[] urlParts = null;  
		
		try {
			
			if (StringUtil.isNullOrEmptyAfterTrim(uri)) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);		
			}
			
            uri = checkGitLabUrl(uri);			
			urlParts = uri.split(":/");
			if ("gitlab".equalsIgnoreCase(urlParts[0])) {
				uri = urlParts[1];
			}			
			script = cache.get(uri);
			if (MailBoxUtil.isEmpty(script)) {
				script = gitlab.getFileContent(commitSha, uri);
				cache.put(uri, script);
				LOGGER.info("Not available in cache, so loading it from GIT.");
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
		LOGGER.info("The retrieve script uri is  ", scriptRequest.getScriptFileUri());
		ScriptServiceResponseDTO serviceResponse = new ScriptServiceResponseDTO();
		GenericValidator validator = new GenericValidator();
		
		try {
			validator.validate(scriptRequest);
			String uri = scriptRequest.getScriptFileUri();
			uri = checkGitLabUrl(uri);
			
			gitlab.updateFile(uri, "master", scriptRequest.getData(),
					"File [" + uri + "] updated by user " + scriptRequest.getCreatedBy());
			
		    serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY, SCRIPT, Messages.SUCCESS));
		    // refresh cache during update
			String[] urlParts = null;
			urlParts = uri.split(":/");
			if ("gitlab".equalsIgnoreCase(urlParts[0])) {
				uri = urlParts[1];
			}
			cache.put(uri, scriptRequest.getData());			
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
	 private String checkGitLabUrl(String uri) throws IOException {
		  
	   if (!uri.toLowerCase().contains("gitlab:")) {    	    	
		  String gitlabDirectory = (String) MailBoxUtil.getEnvironmentProperties().getProperty(
				  MailBoxConstants.PROPERTY_GITLAB_ACTIVITY_SERVER_FOLDER );
		  uri = gitlabDirectory+"/"+uri;
	   }	
	  return uri;
	  
	 }	

}
