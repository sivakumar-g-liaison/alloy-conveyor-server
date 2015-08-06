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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.liaison.commons.exception.BootstrapingFailedException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.MailBoxConstants;
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
import com.liaison.mailbox.enums.SLAVerificationStatus;
import com.liaison.mailbox.rtdm.dao.FSMStateDAO;
import com.liaison.mailbox.rtdm.dao.FSMStateDAOBase;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAO;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAOBase;
import com.liaison.mailbox.rtdm.model.FSMState;
import com.liaison.mailbox.rtdm.model.FSMStateValue;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.fsm.ProcessorStateDTO;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorFactory;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorI;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.MailboxSLAResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.EmailUtil;
import com.liaison.mailbox.service.util.GlassMessage;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.TransactionVisibilityClient;

/**
 * @author OFS
 *
 */
public class MailboxSLAWatchDogService {

	private static final Logger LOG = LogManager.getLogger(MailboxSLAWatchDogService.class);

	private static String SLA_VIOLATION_SUBJECT = "Files are not picked up by the customer within configured SLA of %s minutes";
	private static String SLA_MBX_VIOLATION_SUBJECT = "Files are not picked up by the Alloy Mailbox within configured SLA of %s minutes";
	private static final String MAILBOX = "Mailbox";
	private static final String MAILBOX_SLA = "mailbox_sla";
	private static final String CUSTOMER_SLA = "customer_sla";
	protected static final String seperator = ": ";
	protected StringBuilder logPrefix;

	/**
	 * Internal logger for watch dog services
	 *
	 * @param message
	 */
	private void log(String message, Object... params) {
	    LOG.info("WatchDog : " + message, params);
	}

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
			boolean isMailboxSLAValidationSuccess = validateMailboxSLARule(slaViolatedMailboxesList);
			boolean isCustomerSLAValidationSuccess = validateCustomerSLARule(slaViolatedMailboxesList);
			if (isMailboxSLAValidationSuccess && isCustomerSLAValidationSuccess) {
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
			    log("the mailbox sla configuration is not available in mailbox - {}. So proceed to next mailbox.", mailbox.getMbxName());
				continue;
			}

			FSMStateDAO procDAO = new FSMStateDAOBase();

			List<FSMStateValue> listfsmStateVal = null;

			log("checking whether the processor {} is executed with in the specified mailbox SLA configuration time", procsr.getProcsrName());
			listfsmStateVal = procDAO.findExecutingProcessorsByProcessorId(procsr.getPguid(), getSLAConfigurationAsTimeStamp(timeToPickUpFilePostedToMailbox));

			String emailSubject = null;
			// If the list is empty then the processor is not executed at all during the specified sla time.
			if (null == listfsmStateVal || listfsmStateVal.isEmpty()) {

			    log("The processor {} was not executed with in the specified mailbox SLA configuration time", procsr.getProcsrName());
				slaViolatedMailboxes.add(procsr.getMailbox().getMbxName());
				emailSubject = String.format(SLA_MBX_VIOLATION_SUBJECT, timeToPickUpFilePostedToMailbox);
                EmailUtil.sendEmail(procsr, emailSubject, emailSubject, true);
				log("The SLA violations are notified to the user by sending email for the prcocessor {}", procsr.getProcsrName());
				continue;
			}

			// If the processor is executed during the speicified sla time but got failed.
			if(null != listfsmStateVal && !listfsmStateVal.isEmpty()) {
				for (FSMStateValue fsmStateVal : listfsmStateVal) {

					if (fsmStateVal.getValue().equals(ExecutionState.FAILED.value())) {

					    log("The processor {} was executed but got failed with in the specified mailbox SLA configuration time", procsr.getProcsrName());
						slaViolatedMailboxes.add(procsr.getMailbox().getMbxName());
						emailSubject = String.format(SLA_MBX_VIOLATION_SUBJECT, timeToPickUpFilePostedToMailbox);
		                EmailUtil.sendEmail(procsr, emailSubject, emailSubject, true);
						log("The SLA violations are notified to the user by sending email or the prcocessor {}", procsr.getProcsrName());

					}
				}

			}

		}
		if (null != slaViolatedMailboxes && slaViolatedMailboxes.size() > 0) {
		    log("SLA Validation completed and the identified violations are notified to the user");
			slaViolatedMailboxesList.addAll(slaViolatedMailboxes);
		}

		LOG.debug("Exit from validateMailboxSLARules.");
		return slaViolatedMailboxes.isEmpty();

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
	@Deprecated
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
		TransactionVisibilityClient transactionVisibilityClient = new TransactionVisibilityClient();
		GlassMessage glassMessage = null;

		try {

			LOG.info("#####################----WATCHDOG INVOCATION BLOCK-AFTER CONSUMING FROM QUEUE---############################################");

			//PayloadTicketRequestDTO dto = MailBoxUtil.unmarshalFromJSON(request, PayloadTicketRequestDTO.class);
			WorkTicket workTicket = JAXBUtility.unmarshalFromJSON(request, WorkTicket.class);
			glassMessage = new GlassMessage(workTicket);
			glassMessage.setStatus(ExecutionState.COMPLETED);
			glassMessage.logProcessingStatus(StatusType.RUNNING, "Consumed workticket from queue");

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
				LOG.error(constructMessage(processor, "Processor of type uploader/filewriter is not available for mailbox {}"), mailboxId);
				throw new MailBoxServicesException(Messages.UPLOADER_OR_FILEWRITER_NOT_AVAILABLE, mailboxId, Response.Status.CONFLICT);
			}

			LOG.info(constructMessage(processor, "Start Run"));
			LOG.info(constructMessage(processor, "JSON received from SB {}"), new JSONObject(request).toString(2));
			long startTime = System.currentTimeMillis();

			// check if file Name is available in the payloadTicketRequest if so save the file with the
			// provided file Name if not save with processor Name with Timestamp
			String fileName = (workTicket.getFileName() == null)?(processor.getProcsrName() + System.nanoTime()):workTicket.getFileName();

			LOG.info(constructMessage(processor, "Global PID", seperator, workTicket.getGlobalProcessId(), "retrieved from workticket for file", fileName));
			glassMessage.setCategory(processor.getProcessorType());
			glassMessage.setProtocol(processor.getProcessorType().getCode());
			LOG.info(constructMessage(processor, "Found the processor to write the payload in the local payload location"), mailboxId);

			// retrieve the processor execution status of corresponding uploader from run-time DB
			processorExecutionState = processorExecutionStateDAO.findByProcessorId(processor.getPguid());

			//get payload from spectrum
			InputStream payload = StorageUtilities.retrievePayload(payloadURI);

			if (null == payload) {
				LOG.error(constructMessage(processor,
				        "Global PID",
				        seperator,
				        workTicket.getGlobalProcessId(),
				        seperator,
				        "Failed to retrieve payload from spectrum"));
				throw new MailBoxServicesException("Failed to retrieve payload from spectrum", Response.Status.BAD_REQUEST);
			}

			//get local payload location from uploader/filewriter
			String processorPayloadLocation = getLocationToWritePayloadFromSpectrum(processor);

			if (null == processorPayloadLocation) {
				LOG.error(constructMessage(processor,
				        "Global PID",
                        seperator,
                        workTicket.getGlobalProcessId(),
                        seperator,
                        "payload or filewrite location not configured for processor {}"), processor.getProcsrName());
				throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.COMMON_LOCATION, Response.Status.CONFLICT);
			}
			// get the very first profile configured in the processor
			profileName = (processor.getScheduleProfileProcessors() != null && processor.getScheduleProfileProcessors().size() > 0)? processor.getScheduleProfileProcessors().get(0).getScheduleProfilesRef().getSchProfName():null;

			if (null == profileName && processor.getProcessorType().equals(ProcessorType.REMOTEUPLOADER)) {
				LOG.error(constructMessage(processor, "profile not configured for processor {}"), processor.getProcsrName());
				throw new MailBoxServicesException(Messages.PROFILE_NOT_CONFIGURED, Response.Status.CONFLICT);
			}

			if (null == profileName && processor.getProcessorType().equals(ProcessorType.FILEWRITER)) {
				profileName = MailBoxConstants.PROFILE_NOT_AVAILABLE;
			}

			boolean isOverwrite = (workTicket.getAdditionalContextItem(MailBoxConstants.KEY_OVERWRITE) == Boolean.TRUE)?true:false;
			LOG.info(constructMessage(processor,
			        "Global PID",
                    seperator,
                    workTicket.getGlobalProcessId(),
                    seperator,
                    "Started writing payload to ",
                    processorPayloadLocation,
                    seperator,
                    fileName));

			// write the payload retrieved from spectrum to the configured location of processor
			MailBoxUtil.writeDataToGivenLocation(payload, processorPayloadLocation, fileName, isOverwrite);
			LOG.info(constructMessage(processor,
			        "Global PID",
                    seperator,
                    workTicket.getGlobalProcessId(),
                    seperator,
                    "Payload is successfully written to ",
                    processorPayloadLocation,
                    seperator,
                    fileName));

			//Initiate FSM
			ProcessorStateDTO processorStaged = new ProcessorStateDTO();
			processorStaged.setValues(executionId, processor, profileName, ExecutionState.STAGED,SLAVerificationStatus.SLA_NOT_VERIFIED.getCode());
			fsm.addState(processorStaged);

	        processorExecutionState.setExecutionStatus(ExecutionState.STAGED.value());
	        processorExecutionStateDAO.merge(processorExecutionState);
	        fsm.handleEvent(fsm.createEvent(ExecutionEvents.FILE_STAGED));

	        //GLASS LOGGING BEGINS//
	        glassMessage.setOutAgent(processorPayloadLocation);

			//GLASS LOGGING CORNER 4 //
	        StringBuilder message = new StringBuilder()
                    .append("Payload delivered at target location : ")
                    .append(processorPayloadLocation)
                    .append(File.separatorChar)
                    .append(fileName);

            transactionVisibilityClient.logToGlass(glassMessage);
			glassMessage.logProcessingStatus(StatusType.SUCCESS, message.toString());
            glassMessage.logFourthCornerTimestamp();
			 //GLASS LOGGING ENDS//
	        LOG.info("#################################################################");

            long endTime = System.currentTimeMillis();
            LOG.info(constructMessage(processor, "Number of files processed 1"));
            LOG.info(constructMessage(processor, "Total time taken to process files {}"), endTime - startTime);
            LOG.info(constructMessage(processor, "End run"));

		} catch (JAXBException | JsonParseException | JsonMappingException e) {
			//cannot send email since the request json cannot be parsed
			LOG.error("Unable to Parse Payload Work Ticket from ServiceBroker", e);

		} catch (Exception e) {
			LOG.error(constructMessage(processor, "File Staging failed"), e);
			ProcessorStateDTO processorStageFailed = new ProcessorStateDTO();
			processorStageFailed.setExecutionId(executionId);
			processorStageFailed.setExecutionState(ExecutionState.STAGING_FAILED);
			processorStageFailed.setMailboxId(mailboxId != null ? mailboxId : MailBoxConstants.DUMMY_MAILBOX_ID_FOR_FSM_STATE);
			processorStageFailed.setProfileName(profileName != null ? profileName : MailBoxConstants.PROFILE_NOT_AVAILABLE);
			processorStageFailed.setSlaVerficationStatus(SLAVerificationStatus.SLA_NOT_VERIFIED.getCode());
			processorStageFailed.setProcessorId((processor == null) ? MailBoxConstants.DUMMY_PROCESSOR_ID_FOR_FSM_STATE : processor.getPguid());
			processorStageFailed.setProcessorName((processor == null) ? MailBoxConstants.PROCESSOR_NOT_AVAILABLE : processor.getProcsrName());
			processorStageFailed.setProcessorType((processor == null) ? ProcessorType.REMOTEUPLOADER : processor.getProcessorType());
			// processorExecutionState table will be updated only if processorExecution is available
			if (null != processorExecutionState) {
				processorExecutionState.setExecutionStatus(ExecutionState.STAGING_FAILED.value());
				processorDAO.merge(processor);
			}

			fsm.addState(processorStageFailed);
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.FILE_STAGING_FAILED));

			// send email in case of exception
			String emailSubject = null;
			if (null != processor) {
				emailSubject = processor.getProcsrName() + ":" + e.getMessage();
			} else {
				emailSubject = e.getMessage();
			}
			// email will be sent only if emailAddress is available
			EmailUtil.sendEmail(processor, emailSubject, e);
			//GLASS LOGGING CORNER 4 //
			glassMessage.setStatus(ExecutionState.FAILED);
			transactionVisibilityClient.logToGlass(glassMessage);
			glassMessage.logProcessingStatus(StatusType.ERROR, "Delivery Failed :" + e.getMessage());
			glassMessage.logFourthCornerTimestamp();
			 //GLASS LOGGING ENDS//
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	public Processor getSpecificProcessorofMailbox(String mailboxId) {

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
		List<String> files = null;
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
			    log("the customer sla configuration is not available in mailbox - {}", mailbox.getMbxName());
				continue;
			}

			FSMStateDAO procDAO = new FSMStateDAOBase();

			log("Finding the most recent successful execution of processor {}", procsr.getProcsrName());
			List<FSMStateValue> jobsExecuted = procDAO.findMostRecentSuccessfulExecutionOfProcessor(procsr.getPguid(), procsr.getProcessorType());

			// if no jobs were successfully executed for this processor continue to next one
			if (null == jobsExecuted || jobsExecuted.isEmpty()) {
				log("There are no succesful executions for this processor {} in recent time", procsr.getProcsrName());
				continue;
			}

			FSMStateValue mostRecentExecution = jobsExecuted.get(0) ;
			Timestamp processorLastExecutionTime = mostRecentExecution.getCreatedDate();
			log("The most recent successful execution of processor {} is on {}", procsr.getProcsrName(), processorLastExecutionTime);

			log("Finding non sla verified file staged events");
			List<FSMState> nonSLAVerifiedFileStagedEvents = procDAO.findNonSLAVerifiedFileStagedEvents(procsr.getPguid(), processorLastExecutionTime, procsr.getProcessorType());

			// There are no non sla verified file staged events.
			if (null != nonSLAVerifiedFileStagedEvents && nonSLAVerifiedFileStagedEvents.isEmpty()) {
			    log("There are no non sla verified file staged events for the processor {}", procsr.getProcsrName());
			}
			boolean slaVerificationDone = false;
			for (FSMState fileStagedEvent : nonSLAVerifiedFileStagedEvents ) {

				Timestamp slaConfiguredTime = getCustomerSLAConfigurationAsTimeStamp(timeToPickUpFilePostedByMailbox, processorLastExecutionTime);

				// check whether the sla verification required based on the
				// last execution of processor and sla configuration in the mailbox
				if (isSLACheckRequired(processorLastExecutionTime, slaConfiguredTime)) {
					LOG.debug("customer sla verification is required");
					files = doCustomerSLAVerification(procsr);
					// update the sla verification status as sla verified
					fileStagedEvent.setSlaVerificationStatus(SLAVerificationStatus.SLA_VERIFIED.getCode());
					procDAO.merge(fileStagedEvent);
					slaVerificationDone = true;
				} else {
					LOG.debug("customer sla verification is not required");
				}
			}

			// send an email if there is a sla violation for the current iterating processor
			if (files != null && !files.isEmpty()) {

				slaViolatedMailboxes.add(procsr.getMailbox().getMbxName());
				String emailSubject = String.format(SLA_VIOLATION_SUBJECT, timeToPickUpFilePostedByMailbox);
				StringBuilder body = new StringBuilder(emailSubject)
    				.append("\n\n")
    				.append("Files : ")
    				.append(StringUtils.join(files.toArray()));
				EmailUtil.sendEmail(procsr, emailSubject, body.toString(), true);
			}

			// update the sla verification of processor execution FSM state if sla verification
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
	private List<String> doCustomerSLAVerification(Processor processor) throws Exception {

	    LOG.info("Entering Customer SLA Verification check");

        try {

            MailBoxProcessorI processorInstance = MailBoxProcessorFactory.getInstance(processor);

            // check if file exist in the configured payload location
            // for processors of type uploader or filewrite location
            // for processors of type filewriter if file exists then
            // customer sla is violated

            if (processorInstance instanceof com.liaison.mailbox.service.core.processor.FileWriter) {
            	com.liaison.mailbox.service.core.processor.FileWriter fileWriterProcessor = (com.liaison.mailbox.service.core.processor.FileWriter) processorInstance;
            	return fileWriterProcessor.checkFileExistence();
            } /*else if (processorInstance instanceof FTPSRemoteUploader) {
                FTPSRemoteUploader ftpsRemoteUploader = (FTPSRemoteUploader) processorInstance;
                return ftpsRemoteUploader.checkFileExistence();
            } else if (processorInstance instanceof SFTPRemoteUploader) {
                SFTPRemoteUploader sftpRemoteUploader = (SFTPRemoteUploader)processorInstance;
                return sftpRemoteUploader.checkFileExistence();
            }*/

        } catch (Exception e) {

            LOG.error("Error occured during file existence check of processor {} , {}", processor.getProcsrName(), e.getMessage());
            // if any exception occurs during file existence check, a notification will be send to the user
            // and the mailbox corresponding to this processor will not be considered for sla validation
            String emailSubject = null;
   			emailSubject = processor.getProcsrName() + ":" + e.getMessage();
   			EmailUtil.sendEmail(processor, emailSubject, e);
        }
        return null;

	}

	/**
	 * This method will get the file write location of filewriter and check if any file exist in that specified location
	 *
	 * @param processor
	 * @return boolean - if the file exists it will return value of true otherwise a value of false.
	 * @throws MailBoxServicesException
	 * @throws IOException
	 */
	@Deprecated
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

    /**
     * Method to construct log messages for easy visibility
     *
     * @param messages append to prefix, please make sure the order of the inputs
     * @return constructed string
     */
    public String constructMessage(Processor processor, String... messages) {

        if (null == logPrefix && null == processor) {

            logPrefix = new StringBuilder()
                .append("WatchDog")
                .append(seperator);
        } else {
            logPrefix = new StringBuilder()
                .append("WatchDog")
                .append(seperator)
                .append(processor.getProcessorType().name())
                .append(seperator)
                .append(processor.getProcsrName())
                .append(seperator)
                .append(processor.getMailbox().getMbxName())
                .append(seperator)
                .append(processor.getMailbox().getPguid())
                .append(seperator);
        }

        StringBuilder msgBuf = new StringBuilder().append(logPrefix);
        for (String str : messages) {
            msgBuf.append(str);
        }

        return msgBuf.toString();
    }

}
