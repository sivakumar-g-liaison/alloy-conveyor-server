package com.liaison.mailbox.service.dropbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.dao.ProfileConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProfileConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.DropBoxProcessor;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ScheduleProfilesRef;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.TenancyKeyDTO;
import com.liaison.mailbox.service.dto.configuration.response.DropboxTransferContentResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetTransferProfilesResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.WorkTicketUtil;

public class DropboxFileTransferService {

	private static final Logger LOG = LogManager.getLogger(DropboxFileTransferService.class);
	
	public static final String TRANSFER_PROFILE = "Tranfer Profiles";
	
	/**
	 * @param request
	 * @param profileId
	 * @param aclManifest
	 * @return
	 * @throws Exception
	 */
	public DropboxTransferContentResponseDTO uploadContentAsyncToSpectrum(HttpServletRequest request, String profileId, String aclManifest) throws Exception {
		
		LOG.debug("Entering into uploadContentAsyncToSpectrum service.");
		DropboxTransferContentResponseDTO transferContentResponse = null;
				
		String tenancyKey = null;
		List <Processor> dropboxProcessors = new ArrayList<Processor>();
		LOG.info("Retrieving tenancy keys from acl-manifest");
		
		// retrieve the tenancy key from acl manifest
		List<TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifest);
		if (tenancyKeys.isEmpty()) {
			LOG.error("retrieval of tenancy key from acl manifest failed");
			throw new MailBoxServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED, Response.Status.BAD_REQUEST);
		}
		for (TenancyKeyDTO tenancyKeyDTO : tenancyKeys) {
			
			tenancyKey = tenancyKeyDTO.getGuid();
			List <String> specificProcessorTypes = new ArrayList<String>();
			specificProcessorTypes.add(DropBoxProcessor.class.getCanonicalName());
			ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
			List <Processor> processors = processorDAO.findProcessorsOfSpecificTypeByProfileAndTenancyKey(profileId, tenancyKey, specificProcessorTypes);
			dropboxProcessors.addAll(processors);
			
			// if there are no dropbox processors available for this tenancy key continue to next one.
			if (processors.isEmpty()) {
				LOG.error("There are no processors available for the profile id - {} with tenancykey - {}", profileId, tenancyKey);
				continue;
			}
			ProcessorDTO processorDTO = null;
			for (Processor processor : processors) {
				
				processorDTO = new ProcessorDTO();
				processorDTO.copyFromEntity(processor);
				
				// retrieving the httplistener pipeline id from remote processor properties
				String pipeLineId = processorDTO.getRemoteProcessorProperties().getHttpListenerPipeLineId();
				boolean isSecuredPayload = processorDTO.getRemoteProcessorProperties().isSecuredPayload();
				String mailboxPguid = processor.getMailbox().getPguid();
				String serviceInstanceId = processor.getServiceInstance().getName();
			
				generateWorkTicketAndPostToQueue(request, mailboxPguid, isSecuredPayload, pipeLineId, serviceInstanceId);	
			}
		}
		if (dropboxProcessors.isEmpty()) {
			LOG.error("There are no dropbox processors available");
			throw new MailBoxServicesException("There are no Dropbox Processor available", Response.Status.NOT_FOUND);
		}
		transferContentResponse = new DropboxTransferContentResponseDTO();
		transferContentResponse.setResponse(new ResponseDTO(Messages.CONTENT_QUEUED_FOR_TRANSFER_SUCCESSFUL, Messages.SUCCESS, ""));
		
		return transferContentResponse;
		
	}
	
	/**
	 * @param request
	 * @param aclManifest
	 * @return
	 * @throws IOException
	 */
	public GetTransferProfilesResponseDTO getTransferProfiles(HttpServletRequest request, String aclManifest) throws IOException {
		
		GetTransferProfilesResponseDTO serviceResponse = new GetTransferProfilesResponseDTO();
		List <ProfileDTO> transferProfiles = new ArrayList<ProfileDTO>();
		
		String tenancyKey = null;
		LOG.info("Retrieving tenancy keys from acl-manifest");
		
		// retrieve the tenancy key from acl manifest
		List<TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifest);
		if (tenancyKeys.isEmpty()) {
			LOG.error("retrieval of tenancy key from acl manifest failed");
			throw new MailBoxServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED, Response.Status.BAD_REQUEST);
		}
		for (TenancyKeyDTO tenancyKeyDTO : tenancyKeys) {
			
			tenancyKey = tenancyKeyDTO.getGuid();
			List <String> specificProcessorTypes = new ArrayList<String>();
			specificProcessorTypes.add(DropBoxProcessor.class.getCanonicalName());
			
			ProfileConfigurationDAO profileDAO = new ProfileConfigurationDAOBase();
			List <ScheduleProfilesRef> scheduleProfiles = profileDAO.findTransferProfilesSpecificProcessorTypeByTenancyKey(tenancyKey, specificProcessorTypes);
			
			// if there are no dropbox processors available for this tenancy key continue to next one.
			if (scheduleProfiles.isEmpty()) {
				LOG.error("There are no transfer profiles available for  tenancykey - {}", tenancyKey);
				continue;
			}
			
			// constructing transferProfileDTO from scheduleProfiles
			for (ScheduleProfilesRef profile : scheduleProfiles)  {
				
				ProfileDTO transferProfile = new ProfileDTO();
				transferProfile.copyFromEntity(profile);
				transferProfiles.add(transferProfile);
			}
		}
		if (transferProfiles.isEmpty()) {
			LOG.error("There are no transfer profiles available");
			throw new MailBoxServicesException("There are no Transfer Profiles available", Response.Status.NOT_FOUND);
		}
		
		// Dummy json holding 4 records
		/*for (int i = 0; i < 5; i++)  {
			ProfileDTO transferProfile = new ProfileDTO();
			transferProfile.setId("Dummy Profile id" + i);
			transferProfile.setName("Dummy Profile Name" +  i);
			transferProfiles.add(transferProfile);
		}*/
		
		serviceResponse.setResponse(new ResponseDTO(Messages.RETRIEVE_SUCCESSFUL, TRANSFER_PROFILE, Messages.SUCCESS));
		serviceResponse.setTransferProfiles(transferProfiles);
		return serviceResponse;
	}
	
	/**
	 * @param request
	 * @param mailboxPguid
	 * @param isSecurePayload
	 * @param pipeLineId
	 * @param serviceInstanceId
	 * @throws Exception
	 */
	private void generateWorkTicketAndPostToQueue(HttpServletRequest request, String mailboxPguid, boolean isSecurePayload, String pipeLineId, String serviceInstanceId) throws Exception {
		
		Map <String, String>properties = new HashMap <String, String>();

		properties.put(MailBoxConstants.KEY_SERVICE_INSTANCE_ID, serviceInstanceId);
		properties.put(MailBoxConstants.HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(isSecurePayload));
		if(!MailBoxUtil.isEmpty(pipeLineId)) properties.put(MailBoxConstants.HTTPLISTENER_PIPELINEID, pipeLineId);

		// generate workticket
		WorkTicket workTicket = WorkTicketUtil.createWorkTicket(request, mailboxPguid, properties);
		//store payload to spectrum
		WorkTicketUtil.storePayload(request, workTicket, properties);	
		workTicket.setProcessMode(ProcessMode.MFT);
		WorkTicketUtil.constructMetaDataJson(workTicket);
	}

}
