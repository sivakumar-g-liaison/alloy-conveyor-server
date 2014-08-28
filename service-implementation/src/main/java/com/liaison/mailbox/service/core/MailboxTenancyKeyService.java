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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.TenancyKeyDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetTenancyKeysResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;


public class MailboxTenancyKeyService {

	private static final Logger LOG = LogManager.getLogger(MailBoxConfigurationService.class);
	private static final String TENANCY_KEYS = "Tenancy Keys";
	
	
	/**
	 * Method to retrieve all tenancy keys present in acl manifest
	 * 
	 * @param aclManifestJson
	 * 
	 * @return of tenancy keys present in acl-manifest
	 */
	public GetTenancyKeysResponseDTO getAllTenancyKeysFromACLManifest (String aclManifestJson) {
		
		LOG.debug("Entering retrieve all tenancy keys");
		GetTenancyKeysResponseDTO serviceResponse = new GetTenancyKeysResponseDTO();
		List<TenancyKeyDTO> tenancyKeys = null;
		try {
			
			tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifestJson);
			if (tenancyKeys.isEmpty()) {
				LOG.info("Retrieval of Tenancy keys failed");
				throw new MailBoxServicesException(Messages.READ_OPERATION_FAILED, TENANCY_KEYS);
			}
			
			// constructing the response
			serviceResponse.setTenancyKeys(tenancyKeys);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, TENANCY_KEYS, Messages.SUCCESS));
			LOG.debug("Exit from retrieve all tenancy keys.");
			return serviceResponse;
		} catch (IOException | MailBoxServicesException e ) {
			
			LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, TENANCY_KEYS, Messages.FAILURE, e.getMessage()));
			return serviceResponse;
		}
	
		
		
	}
}
