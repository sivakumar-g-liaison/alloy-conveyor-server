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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.FileWriter;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.MailBoxProperty;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.RemoteUploader;
import com.liaison.mailbox.dtdm.model.Sweeper;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.SLAVerificationStatus;
import com.liaison.mailbox.rtdm.dao.FSMStateDAO;
import com.liaison.mailbox.rtdm.dao.FSMStateDAOBase;
import com.liaison.mailbox.rtdm.model.FSMState;
import com.liaison.mailbox.rtdm.model.FSMStateValue;
import com.liaison.mailbox.service.core.email.EmailNotifier;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorFactory;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorI;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.MailboxSLAResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Class that performs the SLA validations.
 * 
 * @author OFS
 */
public class MailboxSLAWatchDogService {

	private static final Logger LOG = LogManager.getLogger(MailboxSLAWatchDogService.class);

	private static String SLA_VIOLATION_SUBJECT = "Files are not picked up by the customer within configured SLA of %s minutes";
	private static String SLA_UPLOADER_VIOLATION_SUBJECT = "Files are not uploaded to the customer within configured SLA of %s minutes";
	private static String SLA_MBX_VIOLATION_SUBJECT = "Files are not picked up by the Alloy Mailbox within configured SLA of %s minutes";
	private static final String MAILBOX = "Mailbox";
	private static final String MAILBOX_SLA = "mailbox_sla";
	private static final String CUSTOMER_SLA = "customer_sla";
	protected static final String seperator = ": ";
	protected StringBuilder logPrefix;
	private String uniqueId;
	
	public MailboxSLAWatchDogService(){
		uniqueId = MailBoxUtil.getGUID();
	}
	/**
	 * Internal logger for watch dog services
	 *
	 * @param message
	 */
	private void log(String uniqueId, String message, Object... params) {
	    LOG.info("WatchDog-"+ uniqueId +": " + message, params);
	}

	/**
	 * Check Mailbox satisfies the SLA Rules or not.
	 *
	 * @return MailboxSLAResponseDTO
	 * @throws Exception
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
			Set <MailBoxProperty> mailboxProps = mailbox.getMailboxProperties();
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
			    log(uniqueId, "the mailbox sla configuration is not available in mailbox - {}. So proceed to next mailbox.", mailbox.getMbxName());
				continue;
			}

			// check whether sweeper got executed with in the configured sla time
			checkIfProcessorExecutedInSpecifiedSLAConfiguration(procsr, timeToPickUpFilePostedToMailbox, slaViolatedMailboxes, false);
		}
		if (null != slaViolatedMailboxes && slaViolatedMailboxes.size() > 0) {
		    log(uniqueId, "SLA Validation completed and the identified violations are notified to the user");
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
	 * Method to return a list of canonical names of specific processors
	 *
	 * @param type Mailbox_SLA - (sweeper), type Customer_SLA - (remoteuploader, filewriter)
	 * @return list of canonical names of processors of based on the type provided
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
	 * @return Processor
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
	 * @param slaViolatedMailboxesList
	 * @return true if customer sla is violated otherwise false
	 * @throws Exception
	 */
	public boolean validateCustomerSLARule(List<String> slaViolatedMailboxesList) throws Exception {

		LOG.debug("Entering into validateCustomerSLARule.");
		List<String> slaViolatedMailboxes = new ArrayList<String>();
		ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
		LOG.debug("Retrieving processor of type file writer and uploaders");
		List <Processor> processors = processorDAO.findProcessorsByType(getCannonicalNamesofSpecificProcessors(CUSTOMER_SLA));

		for (Processor procsr : processors) {

			// get the mailbox of this processor to retrieve sla properties
			MailBox mailbox = procsr.getMailbox();
			String timeToPickUpFilePostedByMailbox = null;
			Set <MailBoxProperty> mailboxProps = mailbox.getMailboxProperties();
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
			    log(uniqueId, "the customer sla configuration is not available in mailbox - {}", mailbox.getMbxName());
				continue;
			}

			// validate customer sla of file writer
			if (procsr.getProcessorType().getCode().equals(ProcessorType.FILEWRITER.getCode())) {
				validateCustomerSLAOfFileWriter(procsr, timeToPickUpFilePostedByMailbox, slaViolatedMailboxes);
			}

			// validate customer sla of remote uploaders by checking if the uploader is executed with in the configured sla time
			if (procsr.getProcessorType().getCode().equals(ProcessorType.REMOTEUPLOADER.getCode())) {
				checkIfProcessorExecutedInSpecifiedSLAConfiguration(procsr, timeToPickUpFilePostedByMailbox, slaViolatedMailboxes, true);
			}
		}

		if (!slaViolatedMailboxes.isEmpty()) {
			slaViolatedMailboxesList.addAll(slaViolatedMailboxes);
		}

		LOG.debug("Exit from validateCustomerSLARules.");
		return slaViolatedMailboxes.isEmpty();
	}

	/**
	 * Method which checks if the given processor is executed with in the given slaconfiguration time.
	 *
	 * @param processor - processor for which sla has to be validated
	 * @param slaConfigurationTime - sla configured in mailbox
	 * @param slaViolatedMailboxes - list to hold any sla violated mailboxes
	 * @param isCustomerSLA - boolean stating if it is for customer sla or mailbox sla
	 * @throws IOException
	 */
	private void checkIfProcessorExecutedInSpecifiedSLAConfiguration (Processor processor, String slaConfigurationTime, List<String> slaViolatedMailboxes, boolean isCustomerSLA) throws IOException {

		FSMStateDAO procDAO = new FSMStateDAOBase();

		List<FSMStateValue> listfsmStateVal = null;

		log(uniqueId, "checking whether the processor {} is executed with in the specified SLA configuration time", processor.getProcsrName());
		listfsmStateVal = procDAO.findExecutingProcessorsByProcessorId(processor.getPguid(), getSLAConfigurationAsTimeStamp(slaConfigurationTime));

		String emailSubject = null;
		// If the list is empty then the processor is not executed at all during the specified sla time.
		if (null == listfsmStateVal || listfsmStateVal.isEmpty()) {

		    log(uniqueId, "The processor {} was not executed with in the specified SLA configuration time", processor.getProcsrName());
			slaViolatedMailboxes.add(processor.getMailbox().getMbxName());
			emailSubject = (isCustomerSLA)
					? String.format(SLA_UPLOADER_VIOLATION_SUBJECT, slaConfigurationTime)
					: String.format(SLA_MBX_VIOLATION_SUBJECT, slaConfigurationTime);
					EmailNotifier.sendEmail(processor, emailSubject, emailSubject, true);
			log(uniqueId, "The SLA violations are notified to the user by sending email for the prcocessor {}", processor.getProcsrName());
			return;
		}

		// If the processor is executed during the speicified sla time but got failed.
		if(null != listfsmStateVal && !listfsmStateVal.isEmpty()) {
			for (FSMStateValue fsmStateVal : listfsmStateVal) {

				if (fsmStateVal.getValue().equals(ExecutionState.FAILED.value())) {

				    log(uniqueId, "The processor {} was executed but got failed with in the specified SLA configuration time", processor.getProcsrName());
					slaViolatedMailboxes.add(processor.getMailbox().getMbxName());
					emailSubject = (isCustomerSLA)
					        ? String.format(SLA_UPLOADER_VIOLATION_SUBJECT, slaConfigurationTime)
					        : String.format(SLA_MBX_VIOLATION_SUBJECT, slaConfigurationTime);
					        EmailNotifier.sendEmail(processor, emailSubject, emailSubject, true);
					log(uniqueId, "The SLA violations are notified to the user by sending email for the prcocessor {}", processor.getProcsrName());
				}
			}

		}

	}

	private void validateCustomerSLAOfFileWriter(Processor processor, String timeToPickUpFilePostedByMailbox, List<String> slaViolatedMailboxes) throws Exception {

		FSMStateDAO procDAO = new FSMStateDAOBase();
		List<String> files = null;

		log(uniqueId, "Finding the most recent successful execution of processor {}", processor.getProcsrName());
		List<FSMStateValue> jobsExecuted = procDAO.findMostRecentSuccessfulExecutionOfProcessor(processor.getPguid(), processor.getProcessorType());

		// if no jobs were successfully executed for this processor continue to next one
		if (null == jobsExecuted || jobsExecuted.isEmpty()) {
			log(uniqueId, "There are no succesful executions for this processor {} in recent time", processor.getProcsrName());
			return;
		}

		FSMStateValue mostRecentExecution = jobsExecuted.get(0) ;
		Timestamp processorLastExecutionTime = mostRecentExecution.getCreatedDate();
		log(uniqueId, "The most recent successful execution of processor {} is on {}", processor.getProcsrName(), processorLastExecutionTime);

		log(uniqueId, "Finding non sla verified file staged events");
		List<FSMState> nonSLAVerifiedFileStagedEvents = procDAO.findNonSLAVerifiedFileStagedEvents(processor.getPguid(), processorLastExecutionTime, processor.getProcessorType());

		// There are no non sla verified file staged events.
		if (null != nonSLAVerifiedFileStagedEvents && nonSLAVerifiedFileStagedEvents.isEmpty()) {
		    log(uniqueId, "There are no non sla verified file staged events for the processor {}", processor.getProcsrName());
		}
		for (FSMState fileStagedEvent : nonSLAVerifiedFileStagedEvents) {

			Timestamp slaConfiguredTime = getCustomerSLAConfigurationAsTimeStamp(timeToPickUpFilePostedByMailbox, processorLastExecutionTime);

			// check whether the sla verification required based on the
			// last execution of processor and sla configuration in the mailbox
			if (isSLACheckRequired(processorLastExecutionTime, slaConfiguredTime)) {
				LOG.debug("customer sla verification is required");
				files = doCustomerSLAVerification(processor);
				// update the sla verification status as sla verified if files contain file name
				if (files != null && !files.contains(fileStagedEvent.getProfileName())) {
				    fileStagedEvent.setSlaVerificationStatus(SLAVerificationStatus.SLA_VERIFIED.getCode());
				    procDAO.merge(fileStagedEvent);
				}
			} else {
				LOG.debug("customer sla verification is not required");
			}
		}

		// send an email if there is a sla violation for the current iterating processor
		if (files != null && !files.isEmpty()) {

			slaViolatedMailboxes.add(processor.getMailbox().getMbxName());
			String emailSubject = String.format(SLA_VIOLATION_SUBJECT, timeToPickUpFilePostedByMailbox);
			StringBuilder body = new StringBuilder(emailSubject)
				.append("\n\n")
				.append("Files : ")
				.append(StringUtils.join(files.toArray(), ","));
			EmailNotifier.sendEmail(processor, emailSubject, body.toString(), true);
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
	 * Customer SLA Verification
	 *
	 * @param processor
	 * @return List of available files in the given processor location
	 * @throws Exception
	 */
	private List<String> doCustomerSLAVerification(Processor processor) throws Exception {

	    LOG.debug("Entering Customer SLA Verification check");

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
   			EmailNotifier.sendEmail(processor, emailSubject, e);
        }
        return null;

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
