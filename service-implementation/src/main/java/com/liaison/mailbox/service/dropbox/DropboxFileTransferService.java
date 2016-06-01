/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dropbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.liaison.commons.logging.LogTags;
import com.liaison.commons.message.glass.dom.GatewayType;
import com.liaison.commons.message.glass.dom.StatusType;
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
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ScheduleProfilesRef;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.DropboxProcessorPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.response.DropboxTransferContentResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetTransferProfilesResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.FileTransferMetaDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.glass.util.TransactionVisibilityClient;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;
import com.liaison.mailbox.service.util.WorkTicketUtil;

/**
 * Class which has Dropbox File Transfer related operations.
 *
 * @author OFS
 */
public class DropboxFileTransferService {

	private static final Logger LOG = LogManager.getLogger(DropboxFileTransferService.class);

	public static final String TRANSFER_PROFILE = "Tranfer Profiles";
	private static final DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();

	/**
	 *
	 * @param workTicket
	 * @param fileTransferDTO
	 * @param aclManifestJson
	 * @return
     * @throws Exception
     */
	public DropboxTransferContentResponseDTO transferFile(WorkTicket workTicket, FileTransferMetaDTO fileTransferDTO, String aclManifestJson)
			throws Exception {

		try {

			DropboxTransferContentResponseDTO transferContentResponse = null;
			long startTime = 0;
			long endTime = 0;

			List<Processor> dropboxProcessors = new ArrayList<Processor>();
			LOG.info(MailBoxUtil.constructMessage(null, fileTransferDTO.getTransferProfileName(), "Retrieving tenancy keys from acl-manifest"));

			// start time to calculate elapsed time for retrieving tenancy keys
			// from manifest
			startTime = System.currentTimeMillis();

			// retrieve the tenancy key from acl manifest
			List<String> tenancyKeys = MailBoxUtil.getTenancyKeyGuids(aclManifestJson);
			if (tenancyKeys.isEmpty()) {
				LOG.error(MailBoxUtil.constructMessage(null, fileTransferDTO.getTransferProfileName(), "retrieval of tenancy key from acl manifest failed"));
				throw new MailBoxServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED, Response.Status.BAD_REQUEST);
			}

			// end time to calculate elapsed time for getting manifest
			endTime = System.currentTimeMillis();
			LOG.debug("Calculating elapsed time for retrieving tenancy keys from manifest");
			MailBoxUtil.calculateElapsedTime(startTime, endTime);

			// start time to calculate elapsed time for retrieving profile name
			// by given Id from DB
			startTime = System.currentTimeMillis();

			// for getting profile name
			String profileId = fileTransferDTO.getTransferProfileId();
			ProfileConfigurationDAO profileDao = new ProfileConfigurationDAOBase();
			ScheduleProfilesRef profile = profileDao.find(ScheduleProfilesRef.class, profileId);
			if (null == profile) {
				throw new MailBoxServicesException(Messages.PROFILE_DOES_NOT_EXIST, profileId,
						Response.Status.NOT_FOUND);
			}
			fileTransferDTO.setTransferProfileName(profile.getSchProfName());

			// end time to calculate elapsed time for getting manifest
			endTime = System.currentTimeMillis();
			LOG.debug("Calculating elapsed time for retrieving profile name by given Id from DB");
			MailBoxUtil.calculateElapsedTime(startTime, endTime);

			for (String tenancyKey : tenancyKeys) {

				List<Processor> processors = getDropboxProcessors(profileId, tenancyKey);
				fileTransferDTO.setTenancyKey(tenancyKey);

				dropboxProcessors.addAll(processors);
				// if there are no dropbox processors available for this tenancy
				// key continue to next one.
				if (processors.isEmpty()) {
					LOG.error(MailBoxUtil.constructMessage(null, fileTransferDTO.getTransferProfileName(),
							"There are no processors available for the profile id - {} with tenancykey - {}"),
							profileId, tenancyKey);
					continue;
				}

				//Constrcuts new workticket for each processor
				WorkTicket ticket = null;
				String gpid = null;
				for (Processor processor : processors) {

					gpid = MailBoxUtil.getGUID();
					//Fish tag global process id
					ThreadContext.clearMap(); //set new context after clearing
					ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, gpid);

				    ticket = new WorkTicket();
				    ticket.getAdditionalContext().putAll(workTicket.getAdditionalContext());
				    ticket.getHeaders().putAll(workTicket.getHeaders());
				    ticket.setCreatedTime(new Date());
				    ticket.setGlobalProcessId(gpid);
					transferPayloadAndPostWorkticket(processor, ticket, fileTransferDTO);
				}
			}

			if (dropboxProcessors.isEmpty()) {
				LOG.error(MailBoxUtil.constructMessage(null, null, "There are no dropbox processors available"));
				throw new MailBoxServicesException("There are no Dropbox Processor available",
						Response.Status.NOT_FOUND);
			}
			transferContentResponse = new DropboxTransferContentResponseDTO();
			transferContentResponse.setResponse(new ResponseDTO(Messages.CONTENT_QUEUED_FOR_TRANSFER_SUCCESSFUL,
					Messages.SUCCESS, ""));

			return transferContentResponse;
		} finally {
			// close stream once we are done
			if (null != fileTransferDTO.getFileContent()) {
				fileTransferDTO.getFileContent().close();
			}
			ThreadContext.clearMap(); //set new context after clearing
		}
	}

	private List<Processor> getDropboxProcessors(String profileId, String tenancyKey) {

		long startTime = 0;
		long endTime = 0;

		LOG.debug("The retrieved tenancy key is %s", tenancyKey);
		List<String> specificProcessorTypes = new ArrayList<String>();
		specificProcessorTypes.add(DropBoxProcessor.class.getCanonicalName());
		ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();

		// start time to calculate elapsed time for retrieving dropbox
		// processors linked to given profile Id and tenancyKey in
		// manifest
		startTime = System.currentTimeMillis();

		// retrieve dropbox processors linked to given profile Id and tenancyKey
		// in manifest
		List<Processor> processors = processorDAO.findProcessorsOfSpecificTypeByProfileAndTenancyKey(profileId,
				tenancyKey, specificProcessorTypes);

		// end time to calculate elapsed time for dropbox processors linked
		// to given profile Id and tenancyKey in manifest
		endTime = System.currentTimeMillis();
		LOG.debug("Calculating elapsed time for dropbox processors linked to given profile Id and tenancyKey in manifest");
		MailBoxUtil.calculateElapsedTime(startTime, endTime);

		return processors;
	}

	private void transferPayloadAndPostWorkticket(Processor processor, WorkTicket workTicket,
			FileTransferMetaDTO fileTransferDTO) throws Exception {

	    GlassMessage glassMessage = null;
	    TransactionVisibilityClient transactionVisibilityClient = null;
	    try {

	        //LENS LOGGING
	        //Caller should set GPID in the workticket
	        glassMessage = new GlassMessage();
	        glassMessage.setGlobalPId(workTicket.getGlobalProcessId());
	        glassMessage.setCategory(ProcessorType.DROPBOXPROCESSOR);
	        glassMessage.setProtocol(Protocol.DROPBOXPROCESSOR.getCode());
	        glassMessage.setStatus(ExecutionState.PROCESSING);
	        glassMessage.setInAgent(GatewayType.REST);
	        glassMessage.setProcessId(MailBoxUtil.getGUID());
	        glassMessage.setSenderId(fileTransferDTO.getLoginId());

    		long startTime = 0;
    		long endTime = 0;

    		ProcessorDTO processorDTO = null;
    		processorDTO = new ProcessorDTO();
    		processorDTO.copyFromEntity(processor, false);

    		DropboxProcessorPropertiesDTO dropboxProcessorStaticProperties = (DropboxProcessorPropertiesDTO) ProcessorPropertyJsonMapper.getProcessorBasedStaticPropsFromJson(
    				processor.getProcsrProperties(), processor);
    		String pipeLineId = dropboxProcessorStaticProperties.getHttpListenerPipeLineId();
    		boolean securedPayload = dropboxProcessorStaticProperties.isSecuredPayload();
    		String mailboxPguid = processor.getMailbox().getPguid();
    		String serviceInstanceId = processor.getServiceInstance().getName();
    		boolean lensVisibility = dropboxProcessorStaticProperties.isLensVisibility();

    		Map<String, String> properties = new HashMap<String, String>();
    		properties.put(MailBoxConstants.LOGIN_ID, fileTransferDTO.getLoginId());
    		properties.put(MailBoxConstants.KEY_TENANCY_KEY, fileTransferDTO.getTenancyKey());
    		properties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(securedPayload));
    		properties.put(MailBoxConstants.PROPERTY_LENS_VISIBILITY, String.valueOf(lensVisibility));
			properties.put(MailBoxConstants.STORAGE_IDENTIFIER_TYPE, MailBoxUtil.getStorageType(processor.getDynamicProperties()));

    		workTicket.setPipelineId(pipeLineId);
    		workTicket.setAdditionalContext(MailBoxConstants.MAILBOX_ID, mailboxPguid);
    		workTicket.setAdditionalContext(MailBoxConstants.KEY_SERVICE_INSTANCE_ID, serviceInstanceId);
    		workTicket.setAdditionalContext(MailBoxConstants.DBX_WORK_TICKET_PROFILE_NAME,
    				fileTransferDTO.getTransferProfileName());
    		workTicket.setAdditionalContext(MailBoxConstants.KEY_WORKTICKET_TENANCYKEY, fileTransferDTO.getTenancyKey());
    		workTicket.setAdditionalContext(MailBoxConstants.KEY_WORKTICKET_PROCESSOR_ID, processor.getPguid());

    		// set ttl value from mailbox property or else from property file
    		String ttl = configuration.getString(MailBoxConstants.DROPBOX_PAYLOAD_TTL_DAYS,
    				MailBoxConstants.VALUE_FOR_DEFAULT_TTL);
    		String ttlUnit = MailBoxConstants.TTL_UNIT_DAYS;

    		Map<String,String> ttlMap = processor.getTTLUnitAndTTLNumber();
            if (!ttlMap.isEmpty()) {
    			ttl = ttlMap.get(MailBoxConstants.TTL_NUMBER);
    			ttlUnit = ttlMap.get(MailBoxConstants.CUSTOM_TTL_UNIT);
    		}

    		Integer ttlNumber = Integer.parseInt(ttl);
    		workTicket.setTtlDays(MailBoxUtil.convertTTLIntoDays(ttlUnit, ttlNumber));
    		workTicket.setFileName(fileTransferDTO.getFileName());
    		workTicket.setProcessMode(ProcessMode.ASYNC);

    		// start time to calculate elapsed time for storing payload in spectrum
    		startTime = System.currentTimeMillis();

    		// GMB-385 Fix stream modified into closeshield inputstream in order to
    		// avoid Stream Closed IOException during iteration
    		CloseShieldInputStream clsInputStream = new CloseShieldInputStream(fileTransferDTO.getFileContent());

    		// store payload to spectrum
    		StorageUtilities.storePayload(clsInputStream, workTicket, properties, true);

    		// Log time stamp
            glassMessage.logBeginTimestamp(MailBoxConstants.DROPBOX_FILE_TRANSFER);

            // Log running status
            glassMessage.logProcessingStatus(StatusType.RUNNING, MailBoxConstants.DROPBOX_SERVICE_NAME + ": User " + fileTransferDTO.getLoginId() + " file upload", MailBoxConstants.DROPBOXPROCESSOR, null);

    		// end time to calculate elapsed time for storing payload in spectrum
    		endTime = System.currentTimeMillis();
    		LOG.debug("TIME SPENT ON UPLOADING FILE TO SPECTRUM + OTHER MINOR FUNCTIONS");
    		MailBoxUtil.calculateElapsedTime(startTime, endTime);

    		// set the glassmessage details once workTicket construction is complete with all details
            glassMessage.setMailboxId((String) workTicket.getAdditionalContextItem(MailBoxConstants.MAILBOX_ID));
            glassMessage.setProcessorId((String) workTicket.getAdditionalContextItem(MailBoxConstants.KEY_WORKTICKET_PROCESSOR_ID));
            glassMessage.setTenancyKey((String) workTicket.getAdditionalContextItem(MailBoxConstants.KEY_WORKTICKET_TENANCYKEY));
            glassMessage.setServiceInstandId((String) workTicket.getAdditionalContextItem(MailBoxConstants.KEY_SERVICE_INSTANCE_ID));
            glassMessage.setInboundPipelineId(workTicket.getPipelineId());
            glassMessage.setInSize(workTicket.getPayloadSize());
            glassMessage.setTransferProfileName((String) workTicket.getAdditionalContextItem(MailBoxConstants.DBX_WORK_TICKET_PROFILE_NAME));

    		WorkTicketUtil.postWrkTcktToQ(workTicket);

    		LOG.info(MailBoxUtil.constructMessage(processor, fileTransferDTO.getTransferProfileName(),
    						"GLOBAL PID",
    						MailBoxUtil.seperator,
    						workTicket.getGlobalProcessId(),
    						"Posted workticket to Service Broker for file ",
    						MailBoxUtil.seperator,
    						fileTransferDTO.getFileName()));
    		// Log TVA status
            transactionVisibilityClient = new TransactionVisibilityClient();
            transactionVisibilityClient.logToGlass(glassMessage);

            // log activity status before posting to queue
            glassMessage.logProcessingStatus(StatusType.QUEUED, MailBoxConstants.FILE_QUEUED_SUCCESSFULLY, MailBoxConstants.DROPBOXPROCESSOR, null);
            // log time stamp before posting to queue
            glassMessage.logEndTimestamp(MailBoxConstants.DROPBOX_FILE_TRANSFER);
	    } catch (Exception e) {
	        LOG.error(MailBoxUtil.constructMessage(processor, fileTransferDTO.getTransferProfileName(), e.getMessage()), e);
	        // Log error status
	        // DO NOT THROW ERROR TVAPI FROM GATEWAY BCS IT ISN"T USEFUL
	        /*if (null != glassMessage) {
    	        glassMessage.logProcessingStatus(StatusType.ERROR, MailBoxConstants.DROPBOX_SERVICE_NAME + ": User " + fileTransferDTO.getLoginId() + " file upload failed due to error : " + e.getMessage());
    	        glassMessage.setStatus(ExecutionState.FAILED);
    	        new TransactionVisibilityClient().logToGlass(glassMessage);
	        }*/
	        throw e;
	    }
	}

	/**
	 *
	 * @param aclManifest
	 * @return
	 * @throws IOException
     */
	public GetTransferProfilesResponseDTO getTransferProfiles(String aclManifest)
			throws IOException {

		LOG.debug("Entering getTransferProfiles");

		GetTransferProfilesResponseDTO serviceResponse = new GetTransferProfilesResponseDTO();
		List<ProfileDTO> transferProfiles = new ArrayList<ProfileDTO>();

		LOG.info("Retrieving tenancy keys from acl-manifest");

		// retrieve the tenancy key from acl manifest
		List<String> tenancyKeys = MailBoxUtil.getTenancyKeyGuids(aclManifest);
		if (tenancyKeys.isEmpty()) {
			LOG.error("retrieval of tenancy key from acl manifest failed");
			throw new MailBoxServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED, Response.Status.BAD_REQUEST);
		}
		for (String tenancyKey : tenancyKeys) {

			LOG.debug("DropboxFileTransferService - retrieved tenancy key is %s", tenancyKey);
			List<String> specificProcessorTypes = new ArrayList<String>();
			specificProcessorTypes.add(DropBoxProcessor.class.getCanonicalName());

			ProfileConfigurationDAO profileDAO = new ProfileConfigurationDAOBase();
			List<ScheduleProfilesRef> scheduleProfiles = profileDAO.findTransferProfilesSpecificProcessorTypeByTenancyKey(
					tenancyKey, specificProcessorTypes);

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
