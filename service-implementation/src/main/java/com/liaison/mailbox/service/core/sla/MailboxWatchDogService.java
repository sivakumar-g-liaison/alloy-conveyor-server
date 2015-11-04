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
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.FileWriter;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.RemoteUploader;
import com.liaison.mailbox.dtdm.model.Sweeper;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.rtdm.dao.FSMStateDAO;
import com.liaison.mailbox.rtdm.dao.FSMStateDAOBase;
import com.liaison.mailbox.rtdm.dao.MailboxRTDMDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.model.FSMStateValue;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.core.email.EmailInfoDTO;
import com.liaison.mailbox.service.core.email.EmailNotifier;
import com.liaison.mailbox.service.util.GlassMessage;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.QueryBuilderUtil;
import com.liaison.mailbox.service.util.TransactionVisibilityClient;

/**
 * Updates LENS status for the customer picked up the files
 * 
 * @author OFS
 */
public class MailboxWatchDogService {

	private static final Logger LOGGER = LogManager.getLogger(MailboxWatchDogService.class);

	protected static final String logPrefix = "WatchDog ";
	protected static final String seperator = " :";
	private static String SLA_VIOLATION_SUBJECT = "Files are not picked up by the customer within configured SLA of %s minutes";
	private static String SLA_UPLOADER_VIOLATION_SUBJECT = "Files are not uploaded to the customer within configured SLA of %s minutes";
	private static String SLA_MBX_VIOLATION_SUBJECT = "Files are not picked up by the Alloy Mailbox within configured SLA of %s minutes";
	private static final String MAILBOX_SLA = "mailbox_sla";
	private static final String CUSTOMER_SLA = "customer_sla";
	private String uniqueId;

	private String constructMessage(String... messages) {

		StringBuilder msgBuf = new StringBuilder()
				.append(logPrefix)
				.append(seperator);
        for (String str : messages) {
            msgBuf.append(str).append(seperator);
        }
        return msgBuf.toString();
	}
	
	public MailboxWatchDogService(){
		uniqueId = MailBoxUtil.getGUID();
	}

	/**
	 * Poll and update LENS status for the customer picked up files as well as doing sla validation
	 * @throws Exception 
	 *
	 */
	@SuppressWarnings("unchecked")
	public void pollAndUpdateStatus() throws Exception {

		String uniqueId = MailBoxUtil.getGUID();
		String filePath = null;
		String fileName = null;

		EntityTransaction tx = null;
		EntityManager em = null;
		List<StagedFile> updatedStatusList = new ArrayList<>();
		TransactionVisibilityClient transactionVisibilityClient = null;
		GlassMessage glassMessage = null;

		try {

			// Getting the mailbox.
			em = DAOUtil.getEntityManager(MailboxRTDMDAO.PERSISTENCE_UNIT_NAME);
			tx = em.getTransaction();
			tx.begin();
			
			List <String> processorTypes = getCannonicalNamesofSpecificProcessors(CUSTOMER_SLA);
			// query
			StringBuilder queryString = new StringBuilder().append("select sf from StagedFile sf")
					.append(" where sf.stagedFileStatus =:")
					.append(StagedFileDAO.STATUS)
					.append(" and ( ")
					.append(QueryBuilderUtil.constructSqlStringForTypeOperator(processorTypes))
					.append(")");;

			List<StagedFile> stagedFiles = em.createQuery(queryString.toString())
					.setParameter(StagedFileDAO.STATUS, EntityStatus.ACTIVE.value())
					.getResultList();
			if (stagedFiles == null || stagedFiles.isEmpty()) {
				LOGGER.info(constructMessage(uniqueId, "No active files found"));
				return;
			}

			for (StagedFile stagedFile : stagedFiles) {

				filePath = stagedFile.getFilePath();
				fileName = stagedFile.getFileName();

				if (MailBoxUtil.isEmpty(filePath)) {
					//This may be old staged file
					continue;
				}

				if (Files.exists(Paths.get(filePath + File.separatorChar + fileName), LinkOption.NOFOLLOW_LINKS)) {
					LOGGER.info(constructMessage(uniqueId, "File {} is exists at the location {}. so doing customer sla validation"), fileName, filePath);
					validateCustomerSLA(stagedFile);
					continue;
				}
				LOGGER.info(constructMessage(uniqueId, "File {} is not exist at the location {}"), fileName, filePath);
				
				// if the processor type is uploader then lens updation should not happen 
				// even if the file does not exist as the lens updation is already taken care by the corresponding uploader
				if (ProcessorType.REMOTEUPLOADER.getCode().equals(stagedFile.getProcessorType())) {
					continue;
				}
 
				transactionVisibilityClient = new TransactionVisibilityClient();
				glassMessage = new GlassMessage();
				glassMessage.setGlobalPId(stagedFile.getGlobalProcessId());

				glassMessage.setStatus(ExecutionState.COMPLETED);
				glassMessage.setOutAgent(stagedFile.getFilePath());
				glassMessage.setOutboundFileName(stagedFile.getFileName());
				glassMessage.logProcessingStatus(StatusType.SUCCESS, "File is picked up by the customer or other process", null, null);

				//TVAPI
				transactionVisibilityClient.logToGlass(glassMessage);
				LOGGER.info(constructMessage(uniqueId, "Updated LENS status for the file {} and location is {}"), fileName, filePath);

				// Inactivate the stagedFile
				stagedFile.setStagedFileStatus(EntityStatus.INACTIVE.value());
				stagedFile.setModifiedDate(MailBoxUtil.getTimestamp());
				updatedStatusList.add(stagedFile);
			}

			for (StagedFile updatedFile : updatedStatusList) {
	            em.merge(updatedFile);
	        }
			
			// To validate Mailbox sla for all mailboxes
			validateMailboxSLARule();
			tx.commit();

		} catch (Exception e) {
			LOGGER.error(constructMessage(uniqueId, "Error occured in watchdog service" , e.getMessage()));
			if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
		} finally {
            if (em != null) {
                em.close();
            }
        }

	}
	
	/**
	 * Method to validate customer sla
	 * 
	 * @param stagedFile - staged file entity which contains details of when staging occurs 
	 * 					   and the related processor and mailbox details 
	 * @throws IOException 
	 */
	private void validateCustomerSLA(StagedFile stagedFile) throws IOException {
		
		// get the mailbox from mailbox Id
		ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
		Processor processor = config.find(Processor.class, stagedFile.getProcessorId());
		
		LOGGER.debug("Retrieving mailbox properties");
		List <String> mailboxPropsToBeRetrieved = new ArrayList<>();
		mailboxPropsToBeRetrieved.add(MailBoxConstants.TIME_TO_PICK_UP_FILE_POSTED_BY_MAILBOX);
		mailboxPropsToBeRetrieved.add(MailBoxConstants.MBX_RCVR_PROPERTY);
		
		Map <String, String> mailboxProperties = processor.retrieveMailboxProperties(mailboxPropsToBeRetrieved);
		String customerSLAConfiguration = mailboxProperties.get(MailBoxConstants.TIME_TO_PICK_UP_FILE_POSTED_BY_MAILBOX);
		String emailAddress = mailboxProperties.get(MailBoxConstants.MBX_RCVR_PROPERTY);
		LOGGER.debug("Mailbox Properties retrieved. customer sla - {}, emailAddress - {}", customerSLAConfiguration, emailAddress);
		
		if (MailBoxUtil.isEmpty(customerSLAConfiguration)) {
			
			log(uniqueId, "customer sla is not configured in mailbox, using the default customer sla configuration");
			customerSLAConfiguration = (ProcessorType.FILEWRITER.getCode().equals(stagedFile.getProcessorType()))
										? MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_CUSTOMER_SLA)
										: MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_MAILBOX_SLA);
		}
		
		Timestamp customerSLATimeLimit = getCustomerSLAConfigurationAsTimeStamp(customerSLAConfiguration, stagedFile.getCreatedDate());
		if (isCustomerSLAViolated(customerSLATimeLimit)) {
			
			String emailSubject = null;
			String emailBody = null;
			if (ProcessorType.FILEWRITER.getCode().equals(stagedFile.getProcessorType())) {
				
				emailSubject = String.format(SLA_VIOLATION_SUBJECT, customerSLAConfiguration);
				StringBuilder body = new StringBuilder(emailSubject)
				.append("\n\n")
				.append("File : ")
				.append(stagedFile.getFileName());
				emailBody = body.toString();				
			} else {
				emailSubject = emailBody = String.format(SLA_UPLOADER_VIOLATION_SUBJECT, customerSLAConfiguration);
			}
			// send email notifications for sla violations
			sendEmail(processor, emailAddress, emailSubject, emailBody);
		}
		
	}
	
	
	/**
	 * Method which checks whether the SLAConfigured Time Limit exceeds or not
	 * by comparing the customer sla time with the current time
	 *
	 * @param slaConfiguredTime
	 * @return true if sla time exceeds otherwise false
	 */
	private boolean isCustomerSLAViolated(Timestamp slaConfiguredTime) {
		// check if the sla configured time is before current time
		Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());
		return slaConfiguredTime.before(currentTimeStamp);
	}

	
	/**
	 * Method to convert sla configuration property from mailbox into TimeStamp value
	 *
	 * @param customerSLAConfiguration
	 * @param stagedFileTime
	 * @return
	 * @throws IOException
	 */
	private Timestamp getCustomerSLAConfigurationAsTimeStamp(String customerSLAConfiguration, Timestamp stagedFileTime) throws IOException {

		Timestamp timeStmp = new Timestamp(stagedFileTime.getTime());
		// get the sla time configuration unit
		int timeConfigurationUnit = getSLATimeConfigurationUnit();
		Calendar cal = Calendar.getInstance();
		cal.setTime(timeStmp);
		cal.add(timeConfigurationUnit, +Integer.parseInt(customerSLAConfiguration));
		timeStmp.setTime(cal.getTime().getTime());
		return timeStmp;
	}
	
	
	/**
	 * Method to convert the given sla configuration as timestamp
	 * 
	 * @param slaConfiguration
	 * @return timestamp value of given sla configuration
	 * @throws IOException
	 */
	private Timestamp getSLAConfigurationAsTimeStamp(String slaConfiguration) throws IOException {

		Timestamp timeStmp = new Timestamp(new Date().getTime());

		// get the sla time configuration unit
		int timeConfigurationUnit = getSLATimeConfigurationUnit();
		Calendar cal = Calendar.getInstance();
		cal.setTime(timeStmp);
		cal.add(timeConfigurationUnit, -Integer.parseInt(slaConfiguration));
		timeStmp.setTime(cal.getTime().getTime());
		return timeStmp;
	}
	
	/**
	 * Method to return sla time configuration unit from properties file
	 * 
	 * @return  config unit as int value. 
	 * 			The default value is hours.The possibel values are as follows
	 * 			12 for  Minutes
	 * 			10 for Hours
	 * @throws IOException
	 */
	private int getSLATimeConfigurationUnit() throws IOException {

		// get sla time configuration unit from properties file
		String slaTimeConfigurationUnit = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.MBX_SLA_CONFIG_UNIT);
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
	 * Iterate all Mailboxes and check whether Mailbox satisfies the SLA Rules
	 *
	 * @return boolean
	 * @throws IOException
	 */
	public void validateMailboxSLARule() throws IOException {

		LOGGER.debug("Entering into validateMailboxSLARules.");

		ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();
		
		LOGGER.debug("Retrieving all sweepers");
		List <String> processorTypes = getCannonicalNamesofSpecificProcessors(MAILBOX_SLA);
		List <Processor> sweepers = config.findProcessorsByType(processorTypes);
		
		for (Processor procsr : sweepers) {
			
			LOGGER.debug("Retrieving Mailbox properties");
			List <String> mailboxPropsToBeRetrieved = new ArrayList<>();
			mailboxPropsToBeRetrieved.add(MailBoxConstants.TIME_TO_PICK_UP_FILE_POSTED_BY_MAILBOX);
			mailboxPropsToBeRetrieved.add(MailBoxConstants.MBX_RCVR_PROPERTY);
			
			Map <String, String> mailboxProperties = procsr.retrieveMailboxProperties(mailboxPropsToBeRetrieved);
			String mailboxSLAConfiguration = mailboxProperties.get(MailBoxConstants.TIME_TO_PICK_UP_FILE_POSTED_BY_MAILBOX);
			String emailAddress = mailboxProperties.get(MailBoxConstants.MBX_RCVR_PROPERTY);
			LOGGER.debug("Mailbox Properties retrieved. mailbox sla - {}, emailAddress - {}", mailboxSLAConfiguration, emailAddress);
			
			if (MailBoxUtil.isEmpty(mailboxSLAConfiguration)) {
				
				log(uniqueId, "mailbox sla is not configured in mailbox, using the default mailbox sla configuration");
				mailboxSLAConfiguration = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_MAILBOX_SLA);
			}
			// check whether sweeper got executed with in the configured sla time
			checkIfProcessorExecutedInSpecifiedSLAConfiguration(procsr, mailboxSLAConfiguration, emailAddress);
		}
	}
	
	/**
	 * Method which checks if the given processor is executed with in the given slaconfiguration time.
	 *
	 * @param processor - processor for which sla has to be validated
	 * @param slaConfigurationTime - sla configured in mailbox
	 * @param emailAddress - email Address to which SLA has to be notified
	 * @throws IOException
	 */
	private void checkIfProcessorExecutedInSpecifiedSLAConfiguration (Processor processor, String slaConfigurationTime, String emailAddress) throws IOException {

		FSMStateDAO procDAO = new FSMStateDAOBase();

		List<FSMStateValue> listfsmStateVal = null;

		log(uniqueId, "checking whether the processor {} is executed with in the specified SLA configuration time", processor.getProcsrName());
		listfsmStateVal = procDAO.findExecutingProcessorsByProcessorId(processor.getPguid(), getSLAConfigurationAsTimeStamp(slaConfigurationTime));

		String emailSubject = null;
		// If the list is empty then the processor is not executed at all during the specified sla time.
		if (null == listfsmStateVal || listfsmStateVal.isEmpty()) {

		    log(uniqueId, "The processor {} was not executed with in the specified SLA configuration time", processor.getProcsrName());
			emailSubject = String.format(SLA_MBX_VIOLATION_SUBJECT, slaConfigurationTime);
			sendEmail(processor, emailAddress, emailSubject, emailSubject);
			log(uniqueId, "The SLA violations are notified to the user by sending email for the prcocessor {}", processor.getProcsrName());
			return;
		}

		// If the processor is executed during the speicified sla time but got failed.
		if (null != listfsmStateVal && !listfsmStateVal.isEmpty()) {
			
			for (FSMStateValue fsmStateVal : listfsmStateVal) {

				if (fsmStateVal.getValue().equals(ExecutionState.FAILED.value())) {

				    log(uniqueId, "The processor {} was executed but got failed with in the specified SLA configuration time", processor.getProcsrName());
					emailSubject = String.format(SLA_MBX_VIOLATION_SUBJECT, slaConfigurationTime);
					sendEmail(processor, emailAddress, emailSubject, emailSubject);
					log(uniqueId, "The SLA violations are notified to the user by sending email for the prcocessor {}", processor.getProcsrName());
				}
			}
		}
	}
	
	/**
	 * Internal logger for watch dog services
	 *
	 * @param message
	 */
	private void log(String uniqueId, String message, Object... params) {
	    LOGGER.info("WatchDog-"+ uniqueId +": " + message, params);
	}
	
	/**
	 * Method to send email to user for all sla violations
	 * 
	 * @param processor - processor details 
	 * @param emailAddress - email address to which sla violations has to be notified
	 * @param emailSubject - email subject
	 * @param emailBody - email content
	 */
	private void sendEmail(Processor processor, String emailAddress, String emailSubject, String emailBody) {
		
		MailBox mailbox = processor.getMailbox();
		List <String> emailAddressList = new ArrayList<>();
		emailAddressList.add(emailAddress);
		EmailInfoDTO emailInfo = new EmailInfoDTO(mailbox.getMbxName(), mailbox.getPguid(), processor.getProcsrName(), emailAddressList, emailSubject, emailSubject, true);
		EmailNotifier.sendEmail(emailInfo);
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

}
