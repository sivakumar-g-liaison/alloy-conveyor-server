package com.liaison.mailbox.service.dropbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.dao.ProfileConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProfileConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.DropBoxProcessor;
import com.liaison.mailbox.dtdm.model.MailBoxProperty;
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
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;
import com.liaison.mailbox.service.util.WorkTicketUtil;

public class DropboxFileTransferService {

	private static final Logger LOG = LogManager.getLogger(DropboxFileTransferService.class);

	public static final String TRANSFER_PROFILE = "Tranfer Profiles";
	private static final DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();

	/**
	 * @param request
	 * @param profileId
	 * @param aclManifest
	 * @return
	 * @throws Exception
	 */
	public DropboxTransferContentResponseDTO uploadContentAsyncToSpectrum(WorkTicket workTicket, ServletInputStream stream, String profileId,
			String aclManifest) throws Exception {

		DropboxTransferContentResponseDTO transferContentResponse = null;
		long startTime = 0;
		long endTime = 0;

		String tenancyKey = null;
		List<Processor> dropboxProcessors = new ArrayList<Processor>();
		LOG.info("Retrieving tenancy keys from acl-manifest");

		// start time to calculate elapsed time for retrieving tenancy keys from manifest
		startTime = System.currentTimeMillis();
		
		// retrieve the tenancy key from acl manifest
		List<TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifest);
		if (tenancyKeys.isEmpty()) {
			LOG.error("retrieval of tenancy key from acl manifest failed");
			throw new MailBoxServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED, Response.Status.BAD_REQUEST);
		}
		
		// end time to calculate elapsed time for getting manifest
		endTime = System.currentTimeMillis();
		LOG.debug("Calculating elapsed time for retrieving tenancy keys from manifest");
		MailBoxUtil.calculateElapsedTime(startTime, endTime);
		
		// start time to calculate elapsed time for retrieving profile name by given Id from DB
		startTime = System.currentTimeMillis();
		
		//for getting profile name
		ProfileConfigurationDAO profileDao  = new ProfileConfigurationDAOBase();
		ScheduleProfilesRef profile = profileDao.find(ScheduleProfilesRef.class, profileId);
		
		// end time to calculate elapsed time for getting manifest
		endTime = System.currentTimeMillis();
		LOG.debug("Calculating elapsed time for retrieving profile name by given Id from DB");
		MailBoxUtil.calculateElapsedTime(startTime, endTime);
		
		for (TenancyKeyDTO tenancyKeyDTO : tenancyKeys) {

			tenancyKey = tenancyKeyDTO.getGuid();
			LOG.debug("The retrieved tenancy key is %s", tenancyKey);
			List<String> specificProcessorTypes = new ArrayList<String>();
			specificProcessorTypes.add(DropBoxProcessor.class.getCanonicalName());
			ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
			
			// start time to calculate elapsed time for retrieving dropbox processors linked to given profile Id and tenancyKey in manifest
			startTime = System.currentTimeMillis();
			
			// retrieve dropbox processors linked to given profile Id and tenancyKey in manifest
			List<Processor> processors = processorDAO.findProcessorsOfSpecificTypeByProfileAndTenancyKey(profileId,
					tenancyKey, specificProcessorTypes);
			
			// end time to calculate elapsed time for dropbox processors linked to given profile Id and tenancyKey in manifest
			endTime = System.currentTimeMillis();
			LOG.debug("Calculating elapsed time for dropbox processors linked to given profile Id and tenancyKey in manifest");
			MailBoxUtil.calculateElapsedTime(startTime, endTime);
			
			dropboxProcessors.addAll(processors);

			// if there are no dropbox processors available for this tenancy key
			// continue to next one.
			if (processors.isEmpty()) {
				LOG.error("There are no processors available for the profile id - {} with tenancykey - {}", profileId,
						tenancyKey);
				continue;
			}
			ProcessorDTO processorDTO = null;
			for (Processor processor : processors) {

				processorDTO = new ProcessorDTO();
				processorDTO.copyFromEntity(processor);

				// retrieving the httplistener pipeline id from remote processor
				// properties
				String pipeLineId = ProcessorPropertyJsonMapper.getProcessorProperty(processorDTO, MailBoxConstants.HTTPLISTENER_PIPELINEID);
				boolean isSecuredPayload = Boolean.getBoolean(ProcessorPropertyJsonMapper.getProcessorProperty(processorDTO, MailBoxConstants.HTTPLISTENER_SECUREDPAYLOAD));
				String mailboxPguid = processor.getMailbox().getPguid();
				String serviceInstanceId = processor.getServiceInstance().getName();
				
				Map <String, String>properties = new HashMap <String, String>();
				if(!MailBoxUtil.isEmpty(pipeLineId)) properties.put(MailBoxConstants.HTTPLISTENER_PIPELINEID, pipeLineId);
				
				workTicket.setPipelineId(WorkTicketUtil.retrievePipelineId(properties));
				
				workTicket.setAdditionalContext("mailboxId", mailboxPguid);
				workTicket.setAdditionalContext(MailBoxConstants.KEY_SERVICE_INSTANCE_ID, serviceInstanceId);
				workTicket.setAdditionalContext(MailBoxConstants.HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(isSecuredPayload));
				workTicket.setAdditionalContext(MailBoxConstants.DBX_WORK_TICKET_PROFILE_NAME, profile.getSchProfName());
				
				//set ttl value from mailbox property or else from property file
				String ttl = configuration.getString(MailBoxConstants.DROPBOX_PAYLOAD_TTL_DAYS, MailBoxConstants.VALUE_FOR_DEFAULT_TTL);
				for(MailBoxProperty mbp : processor.getMailbox().getMailboxProperties()) {
					if(mbp.getMbxPropName().equals(MailBoxConstants.TTL)) {
						ttl = (mbp.getMbxPropValue() == null) ? ttl : mbp.getMbxPropValue();
						LOG.debug("TTL value in uploadContentAsyncToSpectrum() is %s", ttl);
						break;
					}
				}
				
				workTicket.setTtlDays(Integer.parseInt(ttl));
				
				// start time to calculate elapsed time for storing payload in spectrum
				startTime = System.currentTimeMillis();				
				
				//store payload to spectrum
				StorageUtilities.storePayload(stream, workTicket, properties);	
				
				// end time to calculate elapsed time for storing payload in spectrum
				endTime = System.currentTimeMillis();
				LOG.debug("Calculating elapsed time for storing payload in spectrum");
				MailBoxUtil.calculateElapsedTime(startTime, endTime);
				
				workTicket.setProcessMode(ProcessMode.MFT);
				WorkTicketUtil.constructMetaDataJson(workTicket);
			}
		}
		if (dropboxProcessors.isEmpty()) {
			LOG.error("There are no dropbox processors available");
			throw new MailBoxServicesException("There are no Dropbox Processor available", Response.Status.NOT_FOUND);
		}
		transferContentResponse = new DropboxTransferContentResponseDTO();
		transferContentResponse.setResponse(new ResponseDTO(Messages.CONTENT_QUEUED_FOR_TRANSFER_SUCCESSFUL,
				Messages.SUCCESS, ""));

		return transferContentResponse;

	}

	/**
	 * @param request
	 * @param aclManifest
	 * @return
	 * @throws IOException
	 */
	public GetTransferProfilesResponseDTO getTransferProfiles(String aclManifest)
			throws IOException {
		
		LOG.debug("Entering getTransferProfiles");

		GetTransferProfilesResponseDTO serviceResponse = new GetTransferProfilesResponseDTO();
		List<ProfileDTO> transferProfiles = new ArrayList<ProfileDTO>();

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
			LOG.debug("DropboxFileTransferService - retrieved tenancy key is %s", tenancyKey);
			List<String> specificProcessorTypes = new ArrayList<String>();
			specificProcessorTypes.add(DropBoxProcessor.class.getCanonicalName());

			ProfileConfigurationDAO profileDAO = new ProfileConfigurationDAOBase();
			List<ScheduleProfilesRef> scheduleProfiles = profileDAO
					.findTransferProfilesSpecificProcessorTypeByTenancyKey(tenancyKey, specificProcessorTypes);

			// if there are no dropbox processors available for this tenancy key
			// continue to next one.
			if (scheduleProfiles.isEmpty()) {
				LOG.error("There are no transfer profiles available for  tenancykey - {}", tenancyKey);
				continue;
			}

			// constructing transferProfileDTO from scheduleProfiles
			for (ScheduleProfilesRef profile : scheduleProfiles) {

				ProfileDTO transferProfile = new ProfileDTO();
				transferProfile.copyFromEntity(profile);
				transferProfiles.add(transferProfile);
			}
		}
		if (transferProfiles.isEmpty()) {
			LOG.error("There are no transfer profiles available");
		}

		serviceResponse.setResponse(new ResponseDTO(Messages.RETRIEVE_SUCCESSFUL, TRANSFER_PROFILE, Messages.SUCCESS));
		serviceResponse.setTransferProfiles(transferProfiles);
		
		LOG.debug("Exit from getTransferProfiles");

		return serviceResponse;
	}
}
