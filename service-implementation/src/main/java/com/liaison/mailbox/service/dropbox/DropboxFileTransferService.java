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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.ws.rs.core.Response;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import com.liaison.mailbox.dtdm.model.MailBoxProperty;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ScheduleProfilesRef;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.DropboxProcessorPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.response.DropboxTransferContentResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetTransferProfilesResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.GlassMessage;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;
import com.liaison.mailbox.service.util.TransactionVisibilityClient;
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
	 * @param request
	 * @param profileId
	 * @param aclManifest
	 * @return DropboxTransferContentResponseDTO
	 * @throws Exception
	 */
	public DropboxTransferContentResponseDTO transferFile(WorkTicket workTicket, ServletInputStream stream,
			String profileId, String aclManifest, String fileName, String loginId, GlassMessage glassMessage)
			throws Exception {

		try {

			DropboxTransferContentResponseDTO transferContentResponse = null;
			long startTime = 0;
			long endTime = 0;

			List<Processor> dropboxProcessors = new ArrayList<Processor>();
			LOG.info("Retrieving tenancy keys from acl-manifest");

			// start time to calculate elapsed time for retrieving tenancy keys
			// from manifest
			startTime = System.currentTimeMillis();

			// retrieve the tenancy key from acl manifest
			List<String> tenancyKeys = MailBoxUtil.getTenancyKeyGuids(aclManifest);
			if (tenancyKeys.isEmpty()) {
				LOG.error("retrieval of tenancy key from acl manifest failed");
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
			ProfileConfigurationDAO profileDao = new ProfileConfigurationDAOBase();
			ScheduleProfilesRef profile = profileDao.find(ScheduleProfilesRef.class, profileId);

			// end time to calculate elapsed time for getting manifest
			endTime = System.currentTimeMillis();
			LOG.debug("Calculating elapsed time for retrieving profile name by given Id from DB");
			MailBoxUtil.calculateElapsedTime(startTime, endTime);

			for (String tenancyKey : tenancyKeys) {

				List<Processor> processors = getDropboxProcessors(profileId, tenancyKey);

				dropboxProcessors.addAll(processors);
				// if there are no dropbox processors available for this tenancy
				// key continue to next one.
				if (processors.isEmpty()) {
					LOG.error("There are no processors available for the profile id - {} with tenancykey - {}",
							profileId, tenancyKey);
					continue;
				}
				for (Processor processor : processors) {

					transferPayloadAndPostWorkticket(processor, workTicket, loginId, tenancyKey, profile, fileName,
							stream, glassMessage);
				}
			}
			if (dropboxProcessors.isEmpty()) {
				LOG.error("There are no dropbox processors available");
				throw new MailBoxServicesException("There are no Dropbox Processor available",
						Response.Status.NOT_FOUND);
			}
			transferContentResponse = new DropboxTransferContentResponseDTO();
			transferContentResponse.setResponse(new ResponseDTO(Messages.CONTENT_QUEUED_FOR_TRANSFER_SUCCESSFUL,
					Messages.SUCCESS, ""));

			return transferContentResponse;
		} finally {
			// close stream once we are done
			if (null != stream)
				stream.close();
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

	private void transferPayloadAndPostWorkticket(Processor processor, WorkTicket workTicket, String loginId,
			String tenancyKey, ScheduleProfilesRef profile, String fileName, ServletInputStream stream,
			GlassMessage glassMessage)
			throws Exception {

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

		Map<String, String> properties = new HashMap<String, String>();
		properties.put(MailBoxConstants.LOGIN_ID, loginId);
		properties.put(MailBoxConstants.KEY_TENANCY_KEY, tenancyKey);
		properties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(securedPayload));

		workTicket.setPipelineId(pipeLineId);
		workTicket.setAdditionalContext(MailBoxConstants.MAILBOX_ID, mailboxPguid);
		workTicket.setAdditionalContext(MailBoxConstants.KEY_SERVICE_INSTANCE_ID, serviceInstanceId);
		workTicket.setAdditionalContext(MailBoxConstants.DBX_WORK_TICKET_PROFILE_NAME, profile.getSchProfName());
		workTicket.setAdditionalContext(MailBoxConstants.KEY_WORKTICKET_TENANCYKEY, tenancyKey);
		workTicket.setAdditionalContext(MailBoxConstants.KEY_WORKTICKET_PROCESSOR_ID, processor.getPguid());
		// set ttl value from mailbox property or else from property file
		String ttl = configuration.getString(MailBoxConstants.DROPBOX_PAYLOAD_TTL_DAYS,
				MailBoxConstants.VALUE_FOR_DEFAULT_TTL);
		String ttlUnit = MailBoxConstants.TTL_UNIT_DAYS;

		for (MailBoxProperty mbp : processor.getMailbox().getMailboxProperties()) {
			if (mbp.getMbxPropName().equals(MailBoxConstants.TTL)) {
				ttl = (mbp.getMbxPropValue() == null) ? ttl : mbp.getMbxPropValue();
				LOG.debug("TTL value in uploadContentAsyncToSpectrum() is %s", ttl);
			}
			if (mbp.getMbxPropName().equals(MailBoxConstants.TTL_UNIT)) {
				ttlUnit = (mbp.getMbxPropValue() == null) ? ttlUnit : mbp.getMbxPropValue();
				LOG.debug("TTL Unit in uploadContentAsyncToSpectrum() is %s", ttlUnit);
			}
		}

		Integer ttlNumber = Integer.parseInt(ttl);

		workTicket.setTtlDays(MailBoxUtil.convertTTLIntoSeconds(ttlUnit, ttlNumber));
		workTicket.setFileName(fileName);
		workTicket.setProcessMode(ProcessMode.ASYNC);
		workTicket.setGlobalProcessId(MailBoxUtil.getGUID());

		// start time to calculate elapsed time for storing payload in spectrum
		startTime = System.currentTimeMillis();

		// GMB-385 Fix stream modified into closeshield inputstream in order to
		// avoid Stream Closed IOException during iteration
		CloseShieldInputStream clsInputStream = new CloseShieldInputStream(stream);

		// store payload to spectrum
		StorageUtilities.storePayload(clsInputStream, workTicket, properties, true);

		// end time to calculate elapsed time for storing payload in spectrum
		endTime = System.currentTimeMillis();
		LOG.debug("TIME SPENT ON UPLOADING FILE TO SPECTRUM + OTHER MINOR FUNCTIONS");
		MailBoxUtil.calculateElapsedTime(startTime, endTime);

		// set the glassmessage details once workTicket construction is complete with all details

        glassMessage.setMailboxId((String) workTicket.getAdditionalContextItem(MailBoxConstants.MAILBOX_ID));  
        glassMessage.setGlobalPId(workTicket.getGlobalProcessId());
        glassMessage.setProcessorId((String) workTicket.getAdditionalContextItem(MailBoxConstants.KEY_WORKTICKET_PROCESSOR_ID));
        glassMessage.setTenancyKey((String) workTicket.getAdditionalContextItem(MailBoxConstants.KEY_WORKTICKET_TENANCYKEY));
        glassMessage.setServiceInstandId((String) workTicket.getAdditionalContextItem(MailBoxConstants.KEY_SERVICE_INSTANCE_ID));
        glassMessage.setPipelineId(workTicket.getPipelineId());
        glassMessage.setInSize(workTicket.getPayloadSize().intValue());
        glassMessage.setTransferProfileName((String) workTicket.getAdditionalContextItem(MailBoxConstants.DBX_WORK_TICKET_PROFILE_NAME));
        
        // Log TVA status
        TransactionVisibilityClient transactionVisibilityClient = new TransactionVisibilityClient(MailBoxUtil.getGUID());
        transactionVisibilityClient.logToGlass(glassMessage);
                
        // log activity status before posting to queue
        glassMessage.logProcessingStatus(StatusType.QUEUED, MailBoxConstants.FILE_QUEUED_SUCCESSFULLY);     
        //log time stamp before posting to queue
        glassMessage.logEndTimestamp(MailBoxConstants.DROPBOX_FILE_TRANSFER);

        WorkTicketUtil.postWrkTcktToQ(workTicket);
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
