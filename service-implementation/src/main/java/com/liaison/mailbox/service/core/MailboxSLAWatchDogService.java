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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.codehaus.jettison.json.JSONException;

import com.google.gson.JsonParseException;
import com.liaison.commons.exception.BootstrapingFailedException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.security.pkcs12.SymmetricAlgorithmException;
import com.liaison.commons.util.ISO8601Util;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.FolderType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.SLAVerificationStatus;
import com.liaison.mailbox.jpa.dao.FSMStateDAO;
import com.liaison.mailbox.jpa.dao.FSMStateDAOBase;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.FSMState;
import com.liaison.mailbox.jpa.model.FSMStateValue;
import com.liaison.mailbox.jpa.model.Folder;
import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.jpa.model.MailBoxProperty;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.fsm.ProcessorStateDTO;
import com.liaison.mailbox.service.core.processor.FTPSRemoteUploader;
import com.liaison.mailbox.service.core.processor.MailBoxProcessor;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorFactory;
import com.liaison.mailbox.service.core.processor.SFTPRemoteDownloader;
import com.liaison.mailbox.service.core.processor.SFTPRemoteUploader;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.PayloadTicketRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.MailboxSLAResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.FS2Util;

public class MailboxSLAWatchDogService {

	private static final Logger LOG = LogManager.getLogger(MailboxSLAWatchDogService.class);
	
	private static String SLA_VIOLATION_NOTIFICATION = "Mailbox %s does not adhere to SLA";
	private static String SLA_VIOLATION_NOTIFICATION_MESSAGE = "Mailbox %s does not adhere to SLA Rule \"%s - %s\".";
	private static String SLA_NOTIFICATION_FAILURE_INFO = "\n\n The last execution dated %s got failed.";
	private static String MAILBOX_SLA_RULE = "Time to pick up file posted to mailbox";
	private static String CUSTOMER_SLA_RULE = "Time to pick up file posted by mailbox";
	private static final String MAILBOX = "Mailbox";
	
	
	/**
	 * Iterate all Mailboxes and check whether Mailbox satisfies the SLA Rules
	 * @throws IOException 
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */
	public MailboxSLAResponseDTO validateMailboxSLARule() throws IOException {
		
		MailboxSLAResponseDTO serviceResponse = new MailboxSLAResponseDTO();
		LOG.debug("Entering into validateMailboxSLARules.");
		List <String> slaViolatedMailboxes = new ArrayList<String>();

			
		// get all processors of type sweeper
		ProcessorConfigurationDAO procConfigDAO = new ProcessorConfigurationDAOBase();
		List <Processor> processors = procConfigDAO.findProcessorByType(ProcessorType.SWEEPER);
		
		String timeToPickUpFilePostedToMailbox = null;
		for (Processor procsr : processors) {
			
			// get the mailbox of this processor to retrieve sla properties			
			MailBox mailbox = procsr.getMailbox();
			List <MailBoxProperty> mailboxProps = mailbox.getMailboxProperties();
			for (MailBoxProperty property : mailboxProps) {
				if (property .getMbxPropName().equals(MailBoxConstants.TIME_TO_PICK_UP_FILE_POSTED_TO_MAILBOX)) {
					timeToPickUpFilePostedToMailbox = property.getMbxPropValue();
					break;
				}
			}
			
			// if the sla configuration is not available in mailbox continue to next mailbox
			if (timeToPickUpFilePostedToMailbox == null) continue;
			
			Timestamp timeStmp = getSLAConfigurationAsTimeStamp(timeToPickUpFilePostedToMailbox);
			
			FSMStateDAO procDAO = new FSMStateDAOBase();
			
			List<FSMStateValue> listfsmStateVal = null;
						
			listfsmStateVal = procDAO.findProcessorsExecutingByProcessorId(procsr.getPguid(), timeStmp);
			
			String mailboxName = null;
			String emailSubject = null;
			StringBuilder emailBody = null;
			// If the list is empty then the processor is not executed at all during the specified sla time.
			if (null == listfsmStateVal || listfsmStateVal.isEmpty()) {
				
				mailboxName = procsr.getMailbox().getMbxName();
				slaViolatedMailboxes.add(mailboxName);
				emailSubject = String.format(SLA_VIOLATION_NOTIFICATION, mailboxName);
				emailBody = new StringBuilder(String.format(SLA_VIOLATION_NOTIFICATION_MESSAGE, mailboxName, MAILBOX_SLA_RULE, timeToPickUpFilePostedToMailbox));
				sendEmail(procsr.getEmailAddress(), emailSubject, emailBody.toString(), "HTML");
				continue;
			}
			
			// If the processor is executed during the speicified sla time but got failed.
			if(null != listfsmStateVal && !listfsmStateVal.isEmpty()) {
				for (FSMStateValue fsmStateVal : listfsmStateVal) {
					
					if (fsmStateVal.getValue().equals(ExecutionState.FAILED.value())) {
						mailboxName = procsr.getMailbox().getMbxName();
						slaViolatedMailboxes.add(mailboxName);
						emailSubject = String.format(SLA_VIOLATION_NOTIFICATION, mailboxName);
						ISO8601Util dateUtil = new ISO8601Util();
						emailBody = new StringBuilder(String.format(SLA_VIOLATION_NOTIFICATION_MESSAGE, mailboxName, MAILBOX_SLA_RULE, timeToPickUpFilePostedToMailbox)).append(String.format(SLA_NOTIFICATION_FAILURE_INFO, dateUtil.fromTimestamp(fsmStateVal.getCreatedDate())));
						sendEmail(procsr.getEmailAddress(), emailSubject, emailBody.toString(), "HTML");
					}
				}
				
			}
		
		}
		String additionalMessage = null;
		if (null != slaViolatedMailboxes && slaViolatedMailboxes.size() > 0) {
			additionalMessage = slaViolatedMailboxes.toString().substring(1, slaViolatedMailboxes.toString().length() - 1);
			serviceResponse.setResponse(new ResponseDTO(Messages.MAILBOX_DOES_NOT_ADHERES_SLA, Messages.FAILURE, additionalMessage));
		} else {
			serviceResponse.setResponse(new ResponseDTO(Messages.MAILBOX_ADHERES_SLA, Messages.SUCCESS, ""));
		}
		
		LOG.debug("Exit from validateMailboxSLARules.");
		return serviceResponse;

	}
	
	/**
	 * Sent notifications for SLA non adherence.
	 * 
	 * @param toEmailAddrList
	 *            The extra receivers. The default receiver will be available in
	 *            the mailbox.
	 * @param subject
	 *            The notification subject
	 * @param emailBody
	 *            The body of the notification
	 * @param type
	 *            The notification type(TEXT/HTML).
	 */

	private void sendEmail(List<String> toEmailAddrList, String subject, String emailBody, String type) {

		EmailNotifier notifier = new EmailNotifier();
		notifier.sendEmail(toEmailAddrList, subject, emailBody, type);
	}
	
	private Timestamp getSLAConfigurationAsTimeStamp(String slaConfiguration) throws IOException {
		
		Timestamp timeStmp = new Timestamp(new Date().getTime());
		
		// get the sla time configuration unit
		int timeConfigurationUnit = getSLATimeConfigurationUnit();	
		Calendar cal = Calendar.getInstance();
		cal.setTime(timeStmp);
		cal.add(timeConfigurationUnit, -Integer.parseInt(slaConfiguration));
		timeStmp.setTime(cal.getTime().getTime());
		return timeStmp;//new Timestamp(cal.getTime().getTime());
	}
	
	private int getSLATimeConfigurationUnit() throws IOException {
		
		// get sla time configuration unit from properties file
		String slaTimeConfigurationUnit = MailBoxUtil.getEnvironmentProperties().getString("mailbox.sla.time.configuration.unit");
		int timeConfigurationUnit = 0;
		switch(slaTimeConfigurationUnit.toUpperCase()) {
		
		case "MINUTES":
			timeConfigurationUnit = Calendar.MINUTE;
			break;
		case "HOURS":
			timeConfigurationUnit = Calendar.HOUR;
			break;
		default:
			timeConfigurationUnit = Calendar.HOUR;
			break;
		}
		return timeConfigurationUnit;	
	}
	
	/**
	 * Method which will consume request from queue and write the payload into the
	 * configured payload location in processor of type uploader
	 * 
	 * @param request
	 */
	public void invokeWatchDog(String request) {
		
		Processor processor = null;
		String mailboxId = null;
		String spectrumUrl = null;
		String profileName = null;
		String executionId = MailBoxUtil.getGUID();
		MailboxFSM fsm = new MailboxFSM();
		ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();		
		
		try {
			
			LOG.info("#####################----WATCHDOG INVOCATION BLOCK-AFTER CONSUMING FROM QUEUE---############################################");
			
			PayloadTicketRequestDTO dto = MailBoxUtil.unmarshalFromJSON(request, PayloadTicketRequestDTO.class);

			// validates mandatory value.			
			mailboxId = dto.getMailboxId();
			if (MailBoxUtil.isEmpty(mailboxId)) {
				throw new MailBoxServicesException(Messages.MANDATORY_FIELD_MISSING, "Mailbox Id", Response.Status.CONFLICT);
			}
			
			spectrumUrl = dto.getSpectrumUrl();
			if (MailBoxUtil.isEmpty(spectrumUrl)) {
				throw new MailBoxServicesException(Messages.MANDATORY_FIELD_MISSING, "Spectrum URL", Response.Status.CONFLICT);
			}
			
			LOG.info("The given mailbox id is {}", mailboxId);
			LOG.info("The spectrum URL is {}", spectrumUrl);
			
			//get payload from spectrum
			InputStream payload = FS2Util.retrievePayloadFromSpectrum(spectrumUrl);
			
			//get processor of type uploader configured with mailbox present in PayloadTicketRequest 
			processor = getProcessorOfTypeUploader(mailboxId);
			
			if (processor == null) {
				LOG.error("Processor of type uploader is not available for mailbox {}", mailboxId);
				throw new MailBoxServicesException(Messages.UPLOADER_NOT_AVAILABLE, mailboxId, Response.Status.CONFLICT);
			}
			
			//get local payload location from uploader
			String processorPayloadLocation = getProcessorPayloadLocation(processor);
			
			if (null == processorPayloadLocation) {
				LOG.error("payload location not configured for processor {}", processor.getProcsrName());
				throw new MailBoxServicesException(Messages.PAYLOAD_LOCATION_NOT_CONFIGURED, Response.Status.CONFLICT);
			}
			
			// check if file Name is available in the payloadTicketRequest if so save the file with the 
			// provided file Name if not save with processor Name with Timestamp
			String fileName = (dto.getTargetFileName() == null)?(processor.getProcsrName() + System.nanoTime()):dto.getTargetFileName();
			
			// get the very first profile configured in the processor
			profileName = (processor.getScheduleProfileProcessors() != null && processor.getScheduleProfileProcessors().size() > 0)? processor.getScheduleProfileProcessors().get(0).getScheduleProfilesRef().getSchProfName():null;
			
			if (null == profileName) {
				LOG.error("profile not configured for processor {}", processor.getProcsrName());
				throw new MailBoxServicesException(Messages.PAYLOAD_LOCATION_NOT_CONFIGURED, Response.Status.CONFLICT);
			}
			
			// write the payload retrieved from spectrum to the configured location of processor
			writeSpectrumPayloadToProcessorLocation(payload, processorPayloadLocation, fileName);		
			
			//Initiate FSM	
			ProcessorStateDTO processorStaged = ProcessorStateDTO.getProcessorStateInstance(executionId, processor, profileName, ExecutionState.STAGED, null, SLAVerificationStatus.SLA_NOT_VERIFIED.getCode());
			fsm.addState(processorStaged);
			//fsm.addDefaultStateTransitionRules(processorStaged);
					        
	        processor.setProcsrExecutionStatus(ExecutionState.STAGED.value());
		    processorDAO.merge(processor);
	        fsm.handleEvent(fsm.createEvent(ExecutionEvents.FILE_STAGED));
	        LOG.info("#################################################################");
			
		} catch (MailBoxServicesException e) {
			ProcessorStateDTO processorStageFailed = new ProcessorStateDTO(executionId, MailBoxConstants.DUMMY_PROCESSOR_ID_FOR_FSM_STATE, ExecutionState.STAGING_FAILED, MailBoxConstants.PROCESSOR_NOT_AVAILABLE,
					ProcessorType.REMOTEUPLOADER, (mailboxId == null)?MailBoxConstants.DUMMY_MAILBOX_ID_FOR_FSM_STATE:mailboxId, (profileName == null)?MailBoxConstants.PROFILE_NOT_AVAILABLE:profileName, null, SLAVerificationStatus.SLA_NOT_VERIFIED.getCode());
			fsm.addState(processorStageFailed);
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.FILE_STAGING_FAILED));
			processor.setProcsrExecutionStatus(ExecutionState.STAGING_FAILED.value());
			processorDAO.merge(processor);
			sendEmail(processor.getEmailAddress(), processor.getProcsrName() + ":" + e.getMessage(), ExceptionUtils.getStackTrace(e), "HTML");
			LOG.error("File Staging failed", e);
			
		} catch (Exception e) {
			
			ProcessorStateDTO processorStageFailed = new ProcessorStateDTO(executionId, MailBoxConstants.DUMMY_PROCESSOR_ID_FOR_FSM_STATE, ExecutionState.STAGING_FAILED, MailBoxConstants.PROCESSOR_NOT_AVAILABLE,
					ProcessorType.REMOTEUPLOADER, (mailboxId == null)?MailBoxConstants.DUMMY_MAILBOX_ID_FOR_FSM_STATE:mailboxId, (profileName == null)?MailBoxConstants.PROFILE_NOT_AVAILABLE:profileName, null, SLAVerificationStatus.SLA_NOT_VERIFIED.getCode());
			fsm.addState(processorStageFailed);
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.FILE_STAGING_FAILED));
			processor.setProcsrExecutionStatus(ExecutionState.STAGING_FAILED.value());
			processorDAO.merge(processor);
			sendEmail(processor.getEmailAddress(), processor.getProcsrName() + ":" + e.getMessage(), ExceptionUtils.getStackTrace(e), "HTML");
			LOG.error("File Staging failed", e);
		}
		
	}
	
	/**
	 * method to write the payload from spectrum to configured payload location of processor
	 * 
	 * @throws MailBoxServicesException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws FS2Exception
	 */
	public void writeSpectrumPayloadToProcessorLocation(InputStream response, String payloadLocation, String filename) throws IOException {

		LOG.info("Started writing payload from spectrum");
		File directory = new File(payloadLocation);
		if (!directory.exists()) {
			Files.createDirectories(directory.toPath());
		}
		
		File file = new File(directory.getAbsolutePath() + File.separatorChar + filename);
		Files.write(file.toPath(), IOUtils.toByteArray(response));
		LOG.info("Payload from spectrum is successfully written" + file.getAbsolutePath());
		if (response != null) {
		    response.close();
		}
	}

	
	/**
	 * Method to get the local payload location of Remote Uploader 
	 * associated with given mailbox
	 * 
	 * @param mailboxId
	 * @return
	 * @throws MailBoxServicesException 
	 * @throws IOException 
	 */
	private String getProcessorPayloadLocation (Processor processor) throws MailBoxServicesException, IOException {
		
		if (processor.getFolders() != null) {

			for (Folder folder : processor.getFolders()) {

				FolderType foundFolderType = FolderType.findByCode(folder.getFldrType());
				if (null == foundFolderType) {
					throw new MailBoxServicesException(Messages.FOLDERS_CONFIGURATION_INVALID, Response.Status.CONFLICT);
				} else if (FolderType.PAYLOAD_LOCATION.equals(foundFolderType)) {
					return processMountLocation(folder.getFldrUri());
				}
			}
		}
		return null;
		
	}
	
	/**
	 * Method is used to process the folder path given by user and replace the
	 * mount location with proper value form properties file.
	 * 
	 * @param folderPath
	 *            The folder path given by user
	 * 
	 * @return processedFolderPath The folder path with mount location
	 * 
	 */
	private String processMountLocation(String folderPath) throws IOException {

		String processedFolderPath = null;

		if (folderPath != null && folderPath.toUpperCase().contains(MailBoxConstants.MOUNT_LOCATION)) {
			String mountLocationValue = MailBoxUtil.getEnvironmentProperties().getString("MOUNT_POINT");
			processedFolderPath = folderPath.replaceAll(MailBoxConstants.MOUNT_LOCATION_PATTERN, mountLocationValue);
		} else {
			return folderPath;
		}
		LOG.info("The Processed Folder Path is" + processedFolderPath);
		return processedFolderPath;
	}
	
	/**
	 * Method to get the local payload location of Remote Uploader 
	 * associated with given mailbox
	 * 
	 * @param mailboxId
	 * @return
	 */
	private Processor getProcessorOfTypeUploader(String mailboxId) {
		
		// get processor of type remote uploader of given mailbox id
		ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
		List <Processor> processors = processorDAO.findProcessorByTypeAndMbx(ProcessorType.REMOTEUPLOADER, mailboxId);
		// always get the first available processor
		Processor remoteUploader = (null != processors && processors.size() > 0)?processors.get(0):null;
		return remoteUploader;
		
	}
	
	
	/**
	 * Iterate all Mailboxes and check whether Customer satisfies the SLA Rules
	 * configured to a mailbox
	 * 
	 * @throws IOException 
	 * @throws BootstrapingFailedException 
	 * @throws CMSException 
	 * @throws JSONException 
	 * @throws SymmetricAlgorithmException 
	 * @throws MailBoxServicesException 
	 * @throws URISyntaxException 
	 * @throws JAXBException 
	 * @throws LiaisonException 
	 * @throws OperatorCreationException 
	 * @throws KeyStoreException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws JsonParseException 
	 * @throws UnrecoverableKeyException 
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */
	public MailboxSLAResponseDTO validateCustomerSLARule() throws IOException, UnrecoverableKeyException, JsonParseException, NoSuchAlgorithmException, CertificateException, KeyStoreException, OperatorCreationException, LiaisonException, JAXBException, URISyntaxException, SymmetricAlgorithmException, JSONException, CMSException, BootstrapingFailedException {
		
		MailboxSLAResponseDTO serviceResponse = new MailboxSLAResponseDTO();
		LOG.debug("Entering into validateCustomerSLARule.");
		List <String> slaViolatedMailboxes = new ArrayList<String>();

		try {
			// get all processors of type uploader
			ProcessorConfigurationDAO procConfigDAO = new ProcessorConfigurationDAOBase();
			List <Processor> processors = procConfigDAO.findProcessorByType(ProcessorType.REMOTEUPLOADER);
			
			String timeToPickUpFilePostedByMailbox = null;
			for (Processor procsr : processors) {
				
				// get the mailbox of this processor to retrieve sla properties			
				MailBox mailbox = procsr.getMailbox();
				List <MailBoxProperty> mailboxProps = mailbox.getMailboxProperties();
				for (MailBoxProperty property : mailboxProps) {
					if (property .getMbxPropName().equals(MailBoxConstants.TIME_TO_PICK_UP_FILE_POSTED_BY_MAILBOX)) {
						timeToPickUpFilePostedByMailbox = property.getMbxPropValue();
						break;
					}
				}
				
				// if the sla configuration is not available in mailbox continue to next mailbox
				if (timeToPickUpFilePostedByMailbox == null) continue;
					
				FSMStateDAO procDAO = new FSMStateDAOBase();
				
				List<FSMStateValue> jobsExecuted = procDAO.findMostRecentSuccessfulExecutionOfProcessor(procsr.getPguid());
				
				// if no jobs were successfully executed for this processor continue to next one
				if (null == jobsExecuted || jobsExecuted.isEmpty())  continue;
				
				FSMStateValue mostRecentExecution = jobsExecuted.get(0) ;
							
				Timestamp processorLastExecutionTime = mostRecentExecution.getCreatedDate();
				
				List<FSMState> nonSLAVerifiedFileStagedEvents = procDAO.findNonSLAVerifiedFileStagedEvents(procsr.getPguid(), processorLastExecutionTime);
				
				boolean slaVerificationDone = false;
				for (FSMState fileStagedEvent : nonSLAVerifiedFileStagedEvents ) {						
					
					Timestamp slaConfiguredTime = getCustomerSLAConfigurationAsTimeStamp(timeToPickUpFilePostedByMailbox, processorLastExecutionTime);
					
					// check whether the sla verification required based on the 
					// last execution of processor and sla configuration in the mailbox
					if (isSLACheckRequired(processorLastExecutionTime, slaConfiguredTime)) {
						doCustomerSLAVerification(procsr, timeToPickUpFilePostedByMailbox, slaViolatedMailboxes);
						// update the status as sla verified true
						fileStagedEvent.setSlaVerificationStatus(SLAVerificationStatus.SLA_VERIFIED.getCode());
						procDAO.merge(fileStagedEvent);
						slaVerificationDone = true;
					}
				}
				
				// update the sla verfication of processor execution FSM state if sla verfication 
				// of file staged event of corresponding processor is done
				if (slaVerificationDone) {
					
					List<FSMState> nonSLAVerifiedProcessorExecutions = procDAO.findNonSLAVerifiedFSMEventsByValue(procsr.getPguid(), processorLastExecutionTime, ExecutionState.COMPLETED.value());
					
					for (FSMState fsmEvent : nonSLAVerifiedProcessorExecutions ) {											
						// update the status as sla verified true
						fsmEvent.setSlaVerificationStatus(SLAVerificationStatus.SLA_VERIFIED.getCode());
						procDAO.merge(fsmEvent);
					}
				}
			
			}
			String additionalMessage = null;
			if (null != slaViolatedMailboxes && slaViolatedMailboxes.size() > 0) {
				additionalMessage = slaViolatedMailboxes.toString().substring(1, slaViolatedMailboxes.toString().length() - 1);
				serviceResponse.setResponse(new ResponseDTO(Messages.MAILBOX_DOES_NOT_ADHERES_SLA, Messages.FAILURE, additionalMessage));
			} else {
				serviceResponse.setResponse(new ResponseDTO(Messages.MAILBOX_ADHERES_SLA, Messages.SUCCESS, ""));
			}
			
			LOG.debug("Exit from validateCustomerSLARules.");
			return serviceResponse;
		
		} catch (MailBoxServicesException e) {
			LOG.error(Messages.FAILED_TO_VALIDATE_SLA.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.FAILED_TO_VALIDATE_SLA, MAILBOX, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;
		}
	}
	
	/**
	 * Method to convert sla configuration property from mailbox into TimeStamp value
	 * 
	 * @param customerSLAConfiguration
	 * @param processorLastExecution
	 * @return
	 * @throws IOException
	 */
	private Timestamp getCustomerSLAConfigurationAsTimeStamp(String customerSLAConfiguration, Timestamp processorLastExecution) throws IOException {
		
		Timestamp timeStmp = new Timestamp(processorLastExecution.getTime());
		// get the sla time configuration unit
		int timeConfigurationUnit = getSLATimeConfigurationUnit();	
		Calendar cal = Calendar.getInstance();
		cal.setTime(timeStmp);
		cal.add(timeConfigurationUnit, +Integer.parseInt(customerSLAConfiguration));
		timeStmp.setTime(cal.getTime().getTime());
		return timeStmp;
	}
	
	/**
	 * Method which checks whether the SLAConfigured Time Limit exceeds or not
	 * by comparing the last execution of processor with the current time
	 * 
	 * @param processorLastExecutionTime
	 * @param slaConfiguredTime
	 * @return
	 */
	private boolean isSLACheckRequired(Timestamp processorLastExecutionTime, Timestamp slaConfiguredTime) {
		// check if the sla configured time is after the processor execution time and before current time
		Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());
		return (slaConfiguredTime.after(processorLastExecutionTime) && slaConfiguredTime.before(currentTimeStamp));
		
	}
	
	/**
	 * 
	 * 
	 * @param processor
	 * @param timeToPickUpFilePostedByMailbox
	 * @param slaViolatedMailboxes
	 * @throws UnrecoverableKeyException
	 * @throws JsonParseException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws KeyStoreException
	 * @throws OperatorCreationException
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws JAXBException
	 * @throws URISyntaxException
	 * @throws MailBoxServicesException
	 * @throws SymmetricAlgorithmException
	 * @throws JSONException
	 * @throws CMSException
	 * @throws BootstrapingFailedException
	 */
	private void doCustomerSLAVerification (Processor processor,  String timeToPickUpFilePostedByMailbox, List <String> slaViolatedMailboxes) throws UnrecoverableKeyException, JsonParseException, NoSuchAlgorithmException, CertificateException, KeyStoreException, OperatorCreationException,
												IOException, LiaisonException, JAXBException, URISyntaxException, MailBoxServicesException, SymmetricAlgorithmException, JSONException, CMSException, BootstrapingFailedException {
		
		MailBoxProcessor uploaderProcessor = MailBoxProcessorFactory.getInstance(processor);
		
		String mailboxName = null;
		String emailSubject = null;
		StringBuilder emailBody = null;
		
		boolean isCustomerSLAViolated = false;
		
		// check if file exist in the configured uploader location if it exists then
		// customer sla violated so send a notification to the customer email	
		if (uploaderProcessor instanceof FTPSRemoteUploader) {
			
			FTPSRemoteUploader ftpsRemoteUploader = (FTPSRemoteUploader) uploaderProcessor;
			isCustomerSLAViolated = ftpsRemoteUploader.checkFileExistence();
			
		} else if (uploaderProcessor instanceof SFTPRemoteUploader) {
			
			SFTPRemoteUploader sftpRemoteUploader = (SFTPRemoteUploader)uploaderProcessor;
			isCustomerSLAViolated = sftpRemoteUploader.checkFileExistence();		
		}	
		
		if (isCustomerSLAViolated) {
			
			mailboxName = processor.getMailbox().getMbxName();
			slaViolatedMailboxes.add(mailboxName);
			emailSubject = String.format(SLA_VIOLATION_NOTIFICATION, mailboxName);
			emailBody = new StringBuilder(String.format(SLA_VIOLATION_NOTIFICATION_MESSAGE, mailboxName, CUSTOMER_SLA_RULE, timeToPickUpFilePostedByMailbox));
			sendEmail(processor.getEmailAddress(), emailSubject, emailBody.toString(), "HTML");
		}		

	}
}
