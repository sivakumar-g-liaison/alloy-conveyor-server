/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.sla;

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
import java.util.Arrays;
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
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;

import com.liaison.commons.exception.BootstrapingFailedException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.message.glass.dom.GatewayType;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.ISO8601Util;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.FileWriter;
import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.MailBoxProperty;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.RemoteUploader;
import com.liaison.mailbox.dtdm.model.Sweeper;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.FolderType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.enums.SLAVerificationStatus;
import com.liaison.mailbox.rtdm.dao.FSMStateDAO;
import com.liaison.mailbox.rtdm.dao.FSMStateDAOBase;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAO;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAOBase;
import com.liaison.mailbox.rtdm.model.FSMState;
import com.liaison.mailbox.rtdm.model.FSMStateValue;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.service.core.email.EmailNotifier;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.fsm.ProcessorStateDTO;
import com.liaison.mailbox.service.core.processor.FTPSRemoteUploader;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorFactory;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorI;
import com.liaison.mailbox.service.core.processor.SFTPRemoteUploader;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.MailboxSLAResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.GlassMessage;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.TransactionVisibilityClient;

/**
 * @author OFS
 *
 */
public class MailboxSLAWatchDogService {

	private static final Logger LOG = LogManager.getLogger(MailboxSLAWatchDogService.class);

	private static String SLA_VIOLATION_NOTIFICATION = "Mailbox %s does not adhere to SLA";
	private static String SLA_VIOLATION_NOTIFICATION_MESSAGE = "Mailbox %s does not adhere to SLA Rule \"%s - %s\".";
	private static String SLA_NOTIFICATION_FAILURE_INFO = "\n\n The last execution dated %s got failed.";
	private static String MAILBOX_SLA_RULE = "Time to pick up file posted to mailbox";
	private static String CUSTOMER_SLA_RULE = "Time to pick up file posted by mailbox";
	private static final String MAILBOX = "Mailbox";
	private static final String MAILBOX_SLA = "mailbox_sla";
	private static final String CUSTOMER_SLA = "customer_sla";

	/**
	 * Check Mailbox satisfies the SLA Rules or not.
	 *
	 * @return MailboxSLAResponseDTO.
	 * @throws IOException
	 * @throws BootstrapingFailedException
	 * @throws CMSException
	 * @throws JSONException
	 * @throws SymmetricAlgorithmException
	 * @throws URISyntaxException
	 * @throws JAXBException
	 * @throws LiaisonException
	 * @throws OperatorCreationException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableKeyException
	 */
	public MailboxSLAResponseDTO validateSLARules() throws Exception {

		MailboxSLAResponseDTO serviceResponse = new MailboxSLAResponseDTO();
		LOG.debug("Entering into validateSLARules.");
		List <String> slaViolatedMailboxesList  = new ArrayList<String>();

		try {

			boolean isMailboxSLAAdhered = validateMailboxSLARule(slaViolatedMailboxesList);
			boolean isCustomerSLAAdhered = validateCustomerSLARule(slaViolatedMailboxesList);

			if (isMailboxSLAAdhered && isCustomerSLAAdhered) {
				serviceResponse.setResponse(new ResponseDTO(Messages.MAILBOX_ADHERES_SLA, Messages.SUCCESS, ""));
			} else {

				String additionalMessage = slaViolatedMailboxesList.toString().substring(1, slaViolatedMailboxesList.toString().length() - 1);
				serviceResponse.setResponse(new ResponseDTO(Messages.MAILBOX_DOES_NOT_ADHERES_SLA, Messages.FAILURE, additionalMessage));
			}
		} catch (MailBoxServicesException e) {

			LOG.error(Messages.FAILED_TO_VALIDATE_SLA.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.FAILED_TO_VALIDATE_SLA, MAILBOX, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;
		}

		LOG.debug("Exit from validateSLARules.");
		return serviceResponse;
	}

	/**
	 * Iterate all Mailboxes and check whether Mailbox satisfies the SLA Rules
	 *
	 * @param slaViolatedMailboxes
	 * @return boolean
	 * @throws IOException
	 */
	public boolean validateMailboxSLARule(List<String> slaViolatedMailboxesList) throws IOException {

		LOG.debug("Entering into validateMailboxSLARules.");
		List <String> slaViolatedMailboxes = new ArrayList<String>();

		ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
		LOG.debug("Retrieving all sweepers");
		List <Processor> sweepers = config.findProcessorsByType(getCannonicalNamesofSpecificProcessors(MAILBOX_SLA));
		for (Processor procsr : sweepers) {

			String timeToPickUpFilePostedToMailbox = null;
			// get the mailbox of this processor to retrieve sla properties
			MailBox mailbox = procsr.getMailbox();
			List <MailBoxProperty> mailboxProps = mailbox.getMailboxProperties();
			LOG.debug("Retrieving Mailbox SLA Configuration property");
			for (MailBoxProperty property : mailboxProps) {
				if (property.getMbxPropName().equals(MailBoxConstants.TIME_TO_PICK_UP_FILE_POSTED_TO_MAILBOX)) {
					timeToPickUpFilePostedToMailbox = property.getMbxPropValue();
					LOG.debug("The Mailbox SLA configuration is {}", timeToPickUpFilePostedToMailbox);
					break;
				}
			}

			// if the sla configuration is not available in mailbox continue to next mailbox
			if (timeToPickUpFilePostedToMailbox == null)  {
				LOG.info("the mailbox sla configuration is not available in mailbox - {}. So proceed to next mailbox.", mailbox.getMbxName());
				continue;
			}

			Timestamp timeStmp = getSLAConfigurationAsTimeStamp(timeToPickUpFilePostedToMailbox);

			FSMStateDAO procDAO = new FSMStateDAOBase();

			List<FSMStateValue> listfsmStateVal = null;

			LOG.debug("checking whether the processor executed with in the specified mailbox SLA configuration time");
			listfsmStateVal = procDAO.findProcessorsExecutingByProcessorId(procsr.getPguid(), timeStmp);

			String mailboxName = null;
			String emailSubject = null;
			StringBuilder emailBody = null;
			// If the list is empty then the processor is not executed at all during the specified sla time.
			if (null == listfsmStateVal || listfsmStateVal.isEmpty()) {

				LOG.debug("The processor was not executed with in the specified mailbox SLA configuration time");
				mailboxName = procsr.getMailbox().getMbxName();
				slaViolatedMailboxes.add(mailboxName);
				emailSubject = String.format(SLA_VIOLATION_NOTIFICATION, mailboxName);
				emailBody = new StringBuilder(String.format(SLA_VIOLATION_NOTIFICATION_MESSAGE, mailboxName, MAILBOX_SLA_RULE, timeToPickUpFilePostedToMailbox));
				LOG.info("The SLA violations are notified to the user by sending email");
				sendEmail(procsr.getEmailAddress(), emailSubject, emailBody.toString(), "HTML");
				continue;
			}

			// If the processor is executed during the speicified sla time but got failed.
			if(null != listfsmStateVal && !listfsmStateVal.isEmpty()) {
				for (FSMStateValue fsmStateVal : listfsmStateVal) {

					if (fsmStateVal.getValue().equals(ExecutionState.FAILED.value())) {
						LOG.debug("The processor was executed but got failed with in the specified mailbox SLA configuration time");
						mailboxName = procsr.getMailbox().getMbxName();
						slaViolatedMailboxes.add(mailboxName);
						emailSubject = String.format(SLA_VIOLATION_NOTIFICATION, mailboxName);
						ISO8601Util dateUtil = new ISO8601Util();
						emailBody = new StringBuilder(String.format(SLA_VIOLATION_NOTIFICATION_MESSAGE, mailboxName, MAILBOX_SLA_RULE, timeToPickUpFilePostedToMailbox)).append(String.format(SLA_NOTIFICATION_FAILURE_INFO, dateUtil.fromTimestamp(fsmStateVal.getCreatedDate())));
						LOG.info("The SLA violations are notified to the user by sending email");
						sendEmail(procsr.getEmailAddress(), emailSubject, emailBody.toString(), "HTML");
					}
				}

			}

		}
		if (null != slaViolatedMailboxes && slaViolatedMailboxes.size() > 0) {
			LOG.info("SLA Validation completed and the identified violations are notified to the user");
			slaViolatedMailboxesList.addAll(slaViolatedMailboxes);
		}

		LOG.debug("Exit from validateMailboxSLARules.");
		return slaViolatedMailboxes.isEmpty();

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
		ProcessorExecutionState processorExecutionState = null;
		String mailboxId = null;
		String payloadURI = null;
		String profileName = null;
		String executionId = MailBoxUtil.getGUID();
		MailboxFSM fsm = new MailboxFSM();
		ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
		ProcessorExecutionStateDAO processorExecutionStateDAO = new ProcessorExecutionStateDAOBase();

		try {

			LOG.info("#####################----WATCHDOG INVOCATION BLOCK-AFTER CONSUMING FROM QUEUE---############################################");

			//PayloadTicketRequestDTO dto = MailBoxUtil.unmarshalFromJSON(request, PayloadTicketRequestDTO.class);
			WorkTicket workTicket = JAXBUtility.unmarshalFromJSON(request, WorkTicket.class);

			// validates mandatory value.
			mailboxId = workTicket.getAdditionalContextItem(MailBoxConstants.KEY_MAILBOX_ID);
			if (MailBoxUtil.isEmpty(mailboxId)) {
				throw new MailBoxServicesException(Messages.MANDATORY_FIELD_MISSING, "Mailbox Id", Response.Status.CONFLICT);
			}

			payloadURI = workTicket.getPayloadURI();
			if (MailBoxUtil.isEmpty(payloadURI)) {
				throw new MailBoxServicesException(Messages.MANDATORY_FIELD_MISSING, "Spectrum URL", Response.Status.CONFLICT);
			}

			LOG.info("The given mailbox id is {}", mailboxId);
			LOG.info("The payloadURI is {}", payloadURI);

			//get processor of type uploader/filewriter configured with mailbox present in PayloadTicketRequest
			processor = getSpecificProcessorofMailbox(mailboxId);

			if (processor == null) {
				LOG.error("Processor of type uploader is not available for mailbox {}", mailboxId);
				throw new MailBoxServicesException(Messages.UPLOADER_OR_FILEWRITER_NOT_AVAILABLE, mailboxId, Response.Status.CONFLICT);
			}
			LOG.debug("Processor {} of type uploader or filewriter is available for mailbox {}", processor.getProcsrName(), mailboxId);

			// retrieve the processor execution status of corresponding uploader from run-time DB
			processorExecutionState = processorExecutionStateDAO.findByProcessorId(processor.getPguid());

			//get payload from spectrum
			InputStream payload = StorageUtilities.retrievePayload(payloadURI);

			if (null == payload) {
				LOG.error("Failed to retrieve payload from spectrum");
				throw new MailBoxServicesException("Failed to retrieve payload from spectrum", Response.Status.BAD_REQUEST);
			}

			//get local payload location from uploader/filewriter
			String processorPayloadLocation = getLocationToWritePayloadFromSpectrum(processor);

			if (null == processorPayloadLocation) {
				LOG.error("payload or filewrite location  not configured for processor {}", processor.getProcsrName());
				throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.COMMON_LOCATION, Response.Status.CONFLICT);
			}

			// check if file Name is available in the payloadTicketRequest if so save the file with the
			// provided file Name if not save with processor Name with Timestamp
			String fileName = (workTicket.getFileName() == null)?(processor.getProcsrName() + System.nanoTime()):workTicket.getFileName();

			// get the very first profile configured in the processor
			profileName = (processor.getScheduleProfileProcessors() != null && processor.getScheduleProfileProcessors().size() > 0)? processor.getScheduleProfileProcessors().get(0).getScheduleProfilesRef().getSchProfName():null;

			if (null == profileName && processor.getProcessorType().equals(ProcessorType.REMOTEUPLOADER)) {
				LOG.error("profile not configured for processor {}", processor.getProcsrName());
				throw new MailBoxServicesException(Messages.PROFILE_NOT_CONFIGURED, Response.Status.CONFLICT);
			}

			if (null == profileName && processor.getProcessorType().equals(ProcessorType.FILEWRITER)) {
				profileName = MailBoxConstants.PROFILE_NOT_AVAILABLE;
			}

			boolean isOverwrite = (workTicket.getAdditionalContextItem(MailBoxConstants.KEY_OVERWRITE) == Boolean.TRUE)?true:false;
			LOG.debug("Started writing payload from spectrum to processor payload location");
			// write the payload retrieved from spectrum to the configured location of processor
			MailBoxUtil.writeDataToGivenLocation(payload, processorPayloadLocation, fileName, isOverwrite);
			LOG.debug("Payload from spectrum is successfully written to given payload location");

			//Initiate FSM
			ProcessorStateDTO processorStaged = ProcessorStateDTO.getProcessorStateInstance(executionId, processor, profileName, ExecutionState.STAGED, null, SLAVerificationStatus.SLA_NOT_VERIFIED.getCode());
			fsm.addState(processorStaged);

	        processorExecutionState.setExecutionStatus(ExecutionState.STAGED.value());
	        processorExecutionStateDAO.merge(processorExecutionState);
	        fsm.handleEvent(fsm.createEvent(ExecutionEvents.FILE_STAGED));
	        
	      //GLASS LOGGING BEGINS//
			TransactionVisibilityClient glassLogger = new TransactionVisibilityClient(MailBoxUtil.getGUID());
			GlassMessage glassMessage = new GlassMessage();
			glassMessage.setCategory(ProcessorType.FILEWRITER);
			glassMessage.setProtocol(Protocol.FILEWRITER.getCode());
			glassMessage.setGlobalPId(workTicket.getGlobalProcessId());
			glassMessage.setMailboxId(mailboxId);
			glassMessage.setStatus(ExecutionState.STAGED);
			glassMessage.setPipelineId(workTicket.getPipelineId());
			if(processorPayloadLocation.contains("ftps")){
				glassMessage.setOutAgent(GatewayType.FTPS);
			}if(processorPayloadLocation.contains("sftp")){
				glassMessage.setOutAgent(GatewayType.SFTP);
			}else if(processorPayloadLocation.contains("ftp")){
				glassMessage.setOutAgent(GatewayType.FTP);
			}

			glassLogger.logToGlass(glassMessage);
			 //GLASS LOGGING ENDS//
	        LOG.info("#################################################################");
		} catch (JAXBException | JsonParseException | JsonMappingException e) {
			//cannot send email since the request json cannot be parsed
			LOG.error("Unable to Parse Payload Work Ticket from ServiceBroker", e);

		} catch (MailBoxServicesException e) {
			ProcessorStateDTO processorStageFailed = new ProcessorStateDTO(executionId, (processor == null)? MailBoxConstants.DUMMY_PROCESSOR_ID_FOR_FSM_STATE :
								processor.getPguid(), ExecutionState.STAGING_FAILED, (processor == null)? MailBoxConstants.PROCESSOR_NOT_AVAILABLE : processor.getProcsrName(),
								(processor == null)?ProcessorType.REMOTEUPLOADER : processor.getProcessorType(), (mailboxId == null) ?
								MailBoxConstants.DUMMY_MAILBOX_ID_FOR_FSM_STATE : mailboxId, (profileName == null) ?
								MailBoxConstants.PROFILE_NOT_AVAILABLE : profileName, null, SLAVerificationStatus.SLA_NOT_VERIFIED.getCode());
			fsm.addState(processorStageFailed);
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.FILE_STAGING_FAILED));
			// processorExecutionState table will be updated only if processorExecution is available
			if (null != processorExecutionState) {
				processorExecutionState.setExecutionStatus(ExecutionState.STAGING_FAILED.value());
				processorDAO.merge(processor);
			}
			notifyUser(processor, mailboxId, e);
			LOG.error("File Staging failed", e);

		} catch (Exception e) {

			ProcessorStateDTO processorStageFailed = new ProcessorStateDTO(executionId, (processor == null) ? MailBoxConstants.DUMMY_PROCESSOR_ID_FOR_FSM_STATE :
								processor.getPguid(), ExecutionState.STAGING_FAILED, (processor == null)? MailBoxConstants.PROCESSOR_NOT_AVAILABLE : processor.getProcsrName(),
								(processor == null)?ProcessorType.REMOTEUPLOADER : processor.getProcessorType(), (mailboxId == null) ?
								MailBoxConstants.DUMMY_MAILBOX_ID_FOR_FSM_STATE : mailboxId, (profileName == null) ?
								MailBoxConstants.PROFILE_NOT_AVAILABLE : profileName, null, SLAVerificationStatus.SLA_NOT_VERIFIED.getCode());
			fsm.addState(processorStageFailed);
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.FILE_STAGING_FAILED));
			// processorExecutionState table will be updated only if processorStateExecution is available
			if (null != processorExecutionState) {
				processorExecutionState.setExecutionStatus(ExecutionState.STAGING_FAILED.value());
				processorDAO.merge(processor);
			}
			notifyUser(processor, mailboxId, e);
			LOG.error("File Staging failed", e);
		}

	}

	private List <String> getEmailAddress (Processor processor, String mailboxId) {

		LOG.debug ("Retrieving Email Address from mailbox properties");
		if (null != processor) {
			return processor.getEmailAddress();
		}
		if (null != mailboxId) {
			MailBoxConfigurationDAO mailboxDAO = new MailBoxConfigurationDAOBase();
			MailBox mailBox = null;
			try {
				mailBox = mailboxDAO.find(MailBox.class, mailboxId) ;
				List<MailBoxProperty> properties = mailBox.getMailboxProperties();

				if (null != properties) {

					for (MailBoxProperty property : properties) {

						if (MailBoxConstants.MBX_RCVR_PROPERTY.equals(property.getMbxPropName())) {
							String address = property.getMbxPropValue();
							LOG.info("The retrieved emails are {}", address);
							return Arrays.asList(address.split(","));
						}
					}
				}
			} catch (Exception e) {
				LOG.error ("Failed to get Email Address", e);
			}
		}
		return null;
	}

	private void notifyUser (Processor processor, String mailboxId, Exception e) {

		String emailSubject = null;
		List <String> emailAddress = getEmailAddress(processor, mailboxId);
		if (null != processor) {
			emailSubject = processor.getProcsrName() + ":" + e.getMessage();
		} else {
			emailSubject = e.getMessage();
		}
		// Email will be sent only if email address is available
		if (null != emailAddress) sendEmail(emailAddress, emailSubject, ExceptionUtils.getStackTrace(e), "HTML");
	}

	/**
	 * method to write the payload from spectrum to configured payload location of processor
	 *
	 * @throws MailBoxServicesException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws FS2Exception
	 */
	public void writeSpectrumPayloadToProcessorLocation(InputStream response, String payloadLocation, String filename, Boolean isOverwrite) throws IOException {

		LOG.info("Started writing payload from spectrum to processor payload location");
		File directory = new File(payloadLocation);
		if (!directory.exists()) {
			Files.createDirectories(directory.toPath());
		}

		File file = new File(directory.getAbsolutePath() + File.separatorChar + filename);
		// if the file already exists create a file and write the contents.
		if (file.exists() && !isOverwrite)  {
			LOG.info("File {} already exists and should not be overwritten", file.getName());
		} else {
			Files.write(file.toPath(), IOUtils.toByteArray(response));
		}
		LOG.info("Payload from spectrum is successfully written to location {}", file.getAbsolutePath());
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
	private String getLocationToWritePayloadFromSpectrum (Processor processor) throws MailBoxServicesException, IOException {

		LOG.info("Retrieving payload location from processor");
		if (processor.getFolders() != null) {

			for (Folder folder : processor.getFolders()) {

				FolderType foundFolderType = FolderType.findByCode(folder.getFldrType());
				if (null == foundFolderType) {
					throw new MailBoxServicesException(Messages.FOLDERS_CONFIGURATION_INVALID, Response.Status.CONFLICT);
				} else if (processor.getProcessorType().equals(ProcessorType.REMOTEUPLOADER) && FolderType.PAYLOAD_LOCATION.equals(foundFolderType)) {
					LOG.info("The payload location retrieved from processor is {}", folder.getFldrUri());
					return processMountLocation(folder.getFldrUri());
				}  else if (processor.getProcessorType().equals(ProcessorType.FILEWRITER) && FolderType.FILE_WRITE_LOCATION.equals(foundFolderType)) {
					LOG.info("The file write location retrieved from processor is {}", folder.getFldrUri());
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
	 * Method to return a list of canonical names of specific processors
	 *
	 *
	 * @return list of canonical names of processors of based on the type provided
	 * type Mailbox_SLA - (sweeper), type Customer_SLA - (remoteuploader, filewriter)
	 */
	private List<String> getCannonicalNamesofSpecificProcessors(String type) {

		List <String> specificProcessors = new ArrayList<String>();
		switch(type) {
			case MAILBOX_SLA:
				specificProcessors.add(Sweeper.class.getCanonicalName());
				break;
			case CUSTOMER_SLA:
				specificProcessors.add(RemoteUploader.class.getCanonicalName());
				specificProcessors.add(FileWriter.class.getCanonicalName());
				break;

		}

		return specificProcessors;
	}

	/**
	 * Method to get the processor of type RemoteUploader/fileWriter of Mailbox
	 * associated with given mailbox
	 *
	 * @param mailboxId
	 * @return
	 */
	private Processor getSpecificProcessorofMailbox(String mailboxId) {

		LOG.info("Retrieving processors of type uploader for mailbox {}", mailboxId);
		// get processor of type remote uploader of given mailbox id
		ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
		List <Processor> processors = processorDAO.findSpecificProcessorTypesOfMbx(mailboxId, getCannonicalNamesofSpecificProcessors(CUSTOMER_SLA));
		// always get the first available processor because there
		// will be either one uploader or file writer available for each mailbox
		Processor processor = (null != processors && processors.size() > 0) ? processors.get(0) : null;
		return processor;

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
	public boolean validateCustomerSLARule(List<String> slaViolatedMailboxesList) throws Exception {

		LOG.debug("Entering into validateCustomerSLARule.");
		List <String> slaViolatedMailboxes = new ArrayList<String>();
		String timeToPickUpFilePostedByMailbox = null;
		ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
		LOG.debug("Retrieving processor of type file writer and uploaders");
		List <Processor> processors = processorDAO.findProcessorsByType(getCannonicalNamesofSpecificProcessors(CUSTOMER_SLA));

		for (Processor procsr : processors) {

			// get the mailbox of this processor to retrieve sla properties
			MailBox mailbox = procsr.getMailbox();
			List <MailBoxProperty> mailboxProps = mailbox.getMailboxProperties();
			LOG.debug("Retrieving the customer SLA configuration from Mailbox");
			for (MailBoxProperty property : mailboxProps) {
				if (property .getMbxPropName().equals(MailBoxConstants.TIME_TO_PICK_UP_FILE_POSTED_BY_MAILBOX)) {
					timeToPickUpFilePostedByMailbox = property.getMbxPropValue();
					LOG.debug("The customer SLA configuration is {}", timeToPickUpFilePostedByMailbox);
					break;
				}
			}

			// if the sla configuration is not available in mailbox continue to next mailbox
			if (timeToPickUpFilePostedByMailbox == null) {
				LOG.info("the customer sla configuration is not available in mailbox - {}", mailbox.getMbxName());
				continue;
			}

			FSMStateDAO procDAO = new FSMStateDAOBase();

			LOG.debug("Finding the most recent successful execution of processor {}", procsr.getProcsrName());
			List<FSMStateValue> jobsExecuted = procDAO.findMostRecentSuccessfulExecutionOfProcessor(procsr.getPguid(), procsr.getProcessorType());

			// if no jobs were successfully executed for this processor continue to next one
			if (null == jobsExecuted || jobsExecuted.isEmpty()) {
				LOG.debug("There are no succesful executions for this processor {} in recent time", procsr.getProcsrName());
				continue;
			}

			FSMStateValue mostRecentExecution = jobsExecuted.get(0) ;
			Timestamp processorLastExecutionTime = mostRecentExecution.getCreatedDate();
			LOG.info("The most recent successful execution of processor {} is on {}", procsr.getProcsrName(), processorLastExecutionTime);

			LOG.info("Finding non sla verified file staged events");
			List<FSMState> nonSLAVerifiedFileStagedEvents = procDAO.findNonSLAVerifiedFileStagedEvents(procsr.getPguid(), processorLastExecutionTime, procsr.getProcessorType());

			// There are no non sla verified file staged events.
			if (null != nonSLAVerifiedFileStagedEvents && nonSLAVerifiedFileStagedEvents.isEmpty()) {
				LOG.info("There are no non sla verified file staged events for the processor {}", procsr.getProcsrName());
			}
			boolean slaVerificationDone = false;
			boolean isCustomerSLAViolated = false;
			for (FSMState fileStagedEvent : nonSLAVerifiedFileStagedEvents ) {

				Timestamp slaConfiguredTime = getCustomerSLAConfigurationAsTimeStamp(timeToPickUpFilePostedByMailbox, processorLastExecutionTime);

				// check whether the sla verification required based on the
				// last execution of processor and sla configuration in the mailbox
				if (isSLACheckRequired(processorLastExecutionTime, slaConfiguredTime)) {
					LOG.debug("customer sla verification is required");
					isCustomerSLAViolated = doCustomerSLAVerification(procsr, slaViolatedMailboxes);
					// update the sla verification status as sla verified
					fileStagedEvent.setSlaVerificationStatus(SLAVerificationStatus.SLA_VERIFIED.getCode());
					procDAO.merge(fileStagedEvent);
					slaVerificationDone = true;
				} else {
					LOG.debug("customer sla verification is not required");
				}
			}

			// send an email if there is a sla violation for the current iterating processor
			if (isCustomerSLAViolated) {

				String mailboxName = procsr.getMailbox().getMbxName();
				slaViolatedMailboxes.add(mailboxName);
				String emailSubject = String.format(SLA_VIOLATION_NOTIFICATION, mailboxName);
				StringBuilder emailBody = new StringBuilder(String.format(SLA_VIOLATION_NOTIFICATION_MESSAGE, mailboxName, CUSTOMER_SLA_RULE, timeToPickUpFilePostedByMailbox));
				sendEmail(procsr.getEmailAddress(), emailSubject, emailBody.toString(), "HTML");
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
		if (!slaViolatedMailboxes.isEmpty()) {
			slaViolatedMailboxesList.addAll(slaViolatedMailboxes);
		}

		LOG.debug("Exit from validateCustomerSLARules.");
		return slaViolatedMailboxes.isEmpty();
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
	private boolean doCustomerSLAVerification (Processor processor, List <String> slaViolatedMailboxes) throws Exception {

	    LOG.info("Entering Customer SLA Verification check");
        boolean isCustomerSLAViolated = false;

        try {
            // check if the file exists in the configured file write location
            // for processors  of type FileWriter if file exists then customer
            // sla is violated
            if (processor instanceof FileWriter) {
                isCustomerSLAViolated = checkFileExistenceOfFileWriter(processor);
            } else {
                MailBoxProcessorI uploaderProcessor = MailBoxProcessorFactory.getInstance(processor);

                // check if file exist in the configured payload location
                // for processors of type uploader if file exists then
                // customer sla is violated
                if (uploaderProcessor instanceof FTPSRemoteUploader) {

                    FTPSRemoteUploader ftpsRemoteUploader = (FTPSRemoteUploader) uploaderProcessor;
                    isCustomerSLAViolated = ftpsRemoteUploader.checkFileExistence();

                } else if (uploaderProcessor instanceof SFTPRemoteUploader) {

                    SFTPRemoteUploader sftpRemoteUploader = (SFTPRemoteUploader)uploaderProcessor;
                    isCustomerSLAViolated = sftpRemoteUploader.checkFileExistence();
                }
            }
            return isCustomerSLAViolated;
        } catch (Exception e) {

            LOG.error("Error occured during file existence check of processor {} , {}", processor.getProcsrName(), e.getMessage());
            // if any exception occurs during file existence check, a notification will be send to the user
            // and the mailbox corresponding to this processor will not be considered for sla validation
            notifyUser(processor, processor.getMailbox().getPguid(), e);
            return isCustomerSLAViolated;
        }

	}

	/**
	 * This method will get the file write location of filewriter and check if any file exist in that specified location
	 *
	 * @param processor
	 * @return boolean - if the file exists it will return value of true otherwise a value of false.
	 * @throws MailBoxServicesException
	 * @throws IOException
	 */
	private boolean checkFileExistenceOfFileWriter(Processor processor) throws MailBoxServicesException, IOException {

		LOG.debug ("Entering file Existence check for File Writer processor");
		boolean isFileExists = false;
		String fileWriteLocation = getLocationToWritePayloadFromSpectrum(processor);
		if (null == fileWriteLocation) {
			LOG.error("filewrite location  not configured for processor {}", processor.getProcsrName());
			throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.FILEWRITE_LOCATION, Response.Status.CONFLICT);
		}
		File fileWriteLocationDirectory = new File(fileWriteLocation);
		if (fileWriteLocationDirectory.isDirectory() && fileWriteLocationDirectory.exists()) {
			String[] files =  fileWriteLocationDirectory.list();
			isFileExists = (null != files && files.length > 0);
		} else {
			throw new MailBoxServicesException(Messages.INVALID_DIRECTORY, Response.Status.BAD_REQUEST);
		}
		LOG.debug("File Eixstence check completed for FTP Uploader. File exists - {}", isFileExists);
		return isFileExists;

	}

}
