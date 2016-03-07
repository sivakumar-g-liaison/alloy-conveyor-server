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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
	private static final String EMAIL_NOTIFICATION_COUNT_PATTERN= "^[0-9]*$";
	private String uniqueId;

	private String constructMessage(String... messages) {

		StringBuilder msgBuf = new StringBuilder()
				.append(logPrefix)
				.append(seperator)
				.append(uniqueId)
				.append(seperator);
        for (String str : messages) {
            msgBuf.append(str).append(seperator);
        }
        return msgBuf.toString();
	}
	
	public MailboxWatchDogService() {
		uniqueId = MailBoxUtil.getGUID();
	}

	/**
	 * Poll and update LENS status for the customer picked up files as well as doing sla validation
	 * @throws Exception 
	 *
	 */
	@SuppressWarnings("unchecked")
    public void pollAndUpdateStatus() {

		String filePath = null;
		String fileName = null;

		EntityTransaction tx = null;
		EntityManager em = null;
		List<StagedFile> updatedStatusList = new ArrayList<>();
		List<StagedFile> updatedNotificationCountList = new ArrayList<>();
		Map<String, Processor> processors = new HashMap<>();		
		TransactionVisibilityClient transactionVisibilityClient = null;
		GlassMessage glassMessage = null;
		ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();

		try {

			// Getting the mailbox.
			em = DAOUtil.getEntityManager(MailboxRTDMDAO.PERSISTENCE_UNIT_NAME);
			tx = em.getTransaction();
			tx.begin();

			// query
			StringBuilder queryString = new StringBuilder().append("select sf from StagedFile sf")
					.append(" where sf.stagedFileStatus =:")
					.append(StagedFileDAO.STATUS)
					.append(" and sf.processorType in (:")
					.append(StagedFileDAO.TYPE)
					.append(")");

			//Processor Types
			List<String> processorTypes = new ArrayList<>();
			processorTypes.add(ProcessorType.FILEWRITER.name());
			processorTypes.add(ProcessorType.REMOTEUPLOADER.name());
			
			List<StagedFile> stagedFiles = em.createQuery(queryString.toString())
					.setParameter(StagedFileDAO.STATUS, EntityStatus.ACTIVE.value())
					.setParameter(StagedFileDAO.TYPE, processorTypes)
					.getResultList();

			if (stagedFiles == null || stagedFiles.isEmpty()) {
				LOGGER.info(constructMessage("No active files found"));
				return;
			}
			
			Processor processor = null;
			for (StagedFile stagedFile : stagedFiles) {

				filePath = stagedFile.getFilePath();
				fileName = stagedFile.getFileName();

				if (MailBoxUtil.isEmpty(filePath)) {
					//This may be old staged file
					continue;
				}

				if (Files.exists(Paths.get(filePath + File.separatorChar + fileName), LinkOption.NOFOLLOW_LINKS)) {

					LOGGER.info(constructMessage("File {} is exists at the location {}. so doing customer sla validation"), fileName, filePath);

					// get the processor from processor Id
					processor = processors.get(stagedFile.getProcessorId());
					if (null == processor) {
						processor = config.find(Processor.class, stagedFile.getProcessorId());
						processors.put(stagedFile.getProcessorId(), processor);
					}
					//get the mailbox properties
					Map<String, String> mailboxProperties = getMailboxProperties(processor);
					//file is not picked up beyond the configured TTL it should deleted immediately and 
					//No need to call the validateCustomerSLA
					if (validateTTLUnit(stagedFile, mailboxProperties)) {
                        try {
                            Files.delete(Paths.get(filePath + File.separatorChar + fileName));
                        } catch (IOException e) {
                            LOGGER.info(constructMessage("Unable to delete a stale file {} in the filePath {}"), fileName, filePath);
                        }
                        continue;
					}
					validateCustomerSLA(stagedFile, processor, mailboxProperties);
					updatedNotificationCountList.add(stagedFile);
					continue;
				}
				LOGGER.info(constructMessage("File {} is not exist at the location {}"), fileName, filePath);

				// if the processor type is uploader then Lens updation should not happen
				// even if the file does not exist as the lens updation is already taken care by the corresponding uploader
				if (ProcessorType.REMOTEUPLOADER.getCode().equals(stagedFile.getProcessorType())) {
					continue;
				}

				transactionVisibilityClient = new TransactionVisibilityClient();
				glassMessage = new GlassMessage();
				glassMessage.setGlobalPId(stagedFile.getGPID());

				glassMessage.setStatus(ExecutionState.COMPLETED);
				glassMessage.setOutAgent(stagedFile.getFilePath());
				glassMessage.setOutboundFileName(stagedFile.getFileName());
				glassMessage.logProcessingStatus(StatusType.SUCCESS, "File is picked up by the customer or other process", stagedFile.getProcessorType(), null);

				//TVAPI
				transactionVisibilityClient.logToGlass(glassMessage);
				LOGGER.info(constructMessage("Updated LENS status for the file {} and location is {}"), fileName, filePath);

				// Inactivate the stagedFile
				stagedFile.setStagedFileStatus(EntityStatus.INACTIVE.value());
				stagedFile.setModifiedDate(MailBoxUtil.getTimestamp());
				updatedStatusList.add(stagedFile);
			}
            //updated the stagedFile with latest notification count.
			for (StagedFile updatedNotificationCount : updatedNotificationCountList) {
	            em.merge(updatedNotificationCount);
	        }
			
			for (StagedFile updatedFile : updatedStatusList) {
	            em.merge(updatedFile);
	        }

			tx.commit();

		} catch (Exception e) {
			LOGGER.error(constructMessage("Error occured in watchdog service" , e.getMessage()));
			if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException(e);
		} finally {
            if (em != null) {
                em.close();
            }
        }

	}
	
	/**
	 * This method used to retrieve the mailboxProperties from processor
	 * 
	 * @param processor
	 * @return map 
	 */
	private Map<String, String> getMailboxProperties(Processor processor) {
		
		List <String> mailboxPropsToBeRetrieved = new ArrayList<>();		
		mailboxPropsToBeRetrieved.add(MailBoxConstants.TIME_TO_PICK_UP_FILE_POSTED_BY_MAILBOX);
		mailboxPropsToBeRetrieved.add(MailBoxConstants.MBX_RCVR_PROPERTY);
		mailboxPropsToBeRetrieved.add(MailBoxConstants.EMAIL_NOTIFICATION_FOR_SLA_VIOLATION);
		mailboxPropsToBeRetrieved.add(MailBoxConstants.MAX_NUM_OF_NOTIFICATION_FOR_SLA_VIOLATION);
		mailboxPropsToBeRetrieved.add(MailBoxConstants.TTL);
		mailboxPropsToBeRetrieved.add(MailBoxConstants.TTL_UNIT);
		return processor.retrieveMailboxProperties(mailboxPropsToBeRetrieved);
	}
	
	/**
	 *  Method to validate the expire time.
	 *  
	 * @param stagedFile
	 * @param processor
	 * @param mailboxPropsToBeRetrieved
	 * @return boolean
	 * @throws IOException
	 */
	private boolean validateTTLUnit(StagedFile stagedFile, Map<String, String> mailboxProperties) throws IOException {

		String ttl = mailboxProperties.get(MailBoxConstants.TTL);
		String ttlUnit = mailboxProperties.get(MailBoxConstants.TTL_UNIT);
		if (MailBoxUtil.isEmpty(ttl)) {
			LOGGER.info(constructMessage("ttl is not configured in mailbox, using the default TTL configuration"));
			ttl = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DROPBOX_PAYLOAD_TTL_DAYS);
			ttlUnit = MailBoxConstants.TTL_UNIT_DAYS;
		}	
		
		Timestamp expireTimestamp = getTTLAsTimestamp(MailBoxUtil.convertTTLIntoDays(ttlUnit, Integer.parseInt(ttl)), stagedFile.getCreatedDate());
		return isTimeLimitExceeded(expireTimestamp);
	}
	
	/**
	 * Method to convert ttl into TimeStamp value
	 *
	 * @param ttlDay
	 * @param stagedFileTime
	 * @return Timestamp
	 * @throws IOException
	 */
	private Timestamp getTTLAsTimestamp(int ttlDay, Timestamp stagedFileTime) throws IOException {

		Timestamp timeStmp = new Timestamp(stagedFileTime.getTime());
		// get the sla time configuration unit
		Calendar cal = Calendar.getInstance();
		cal.setTime(timeStmp);
		cal.add(Calendar.DATE, + ttlDay);
		timeStmp.setTime(cal.getTime().getTime());
		return timeStmp;
	}
	
	/**
	 * Method to validate customer sla
	 * 
	 * @param stagedFile - staged file entity which contains details of when staging occurs 
	 * 					   and the related processor and mailbox details 
	 * @throws IOException 
	 */
	private void validateCustomerSLA(StagedFile stagedFile, Processor processor, Map<String, String> mailboxProperties) throws IOException {		
		
		String customerSLAConfiguration = mailboxProperties.get(MailBoxConstants.TIME_TO_PICK_UP_FILE_POSTED_BY_MAILBOX);
		String emailAddress = mailboxProperties.get(MailBoxConstants.MBX_RCVR_PROPERTY);
		String enableEmailNotification = mailboxProperties.get(MailBoxConstants.EMAIL_NOTIFICATION_FOR_SLA_VIOLATION);
		String maxNumOfNotification = mailboxProperties.get(MailBoxConstants.MAX_NUM_OF_NOTIFICATION_FOR_SLA_VIOLATION);
		if (MailBoxUtil.isEmpty(enableEmailNotification)) {
			enableEmailNotification = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_SLA_EMAIL_NOTIFICATION);					
		}		
		if (MailBoxUtil.isEmpty(maxNumOfNotification)) {
			maxNumOfNotification = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_SLA_MAX_NOTIFICATION_COUNT);
		}			

		LOGGER.debug("Mailbox Properties retrieved. customer sla - {}, emailAddress - {}", customerSLAConfiguration, emailAddress);
		
		if (MailBoxUtil.isEmpty(customerSLAConfiguration)) {

			LOGGER.info(constructMessage("customer sla is not configured in mailbox, using the default customer sla configuration"));
			customerSLAConfiguration = (ProcessorType.FILEWRITER.getCode().equals(stagedFile.getProcessorType()))
										? MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_CUSTOMER_SLA)
										: MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_MAILBOX_SLA);
		}
		
		Timestamp customerSLATimeLimit = getCustomerSLAConfigurationAsTimeStamp(customerSLAConfiguration, stagedFile.getCreatedDate());
		if (isTimeLimitExceeded(customerSLATimeLimit) && Boolean.valueOf(enableEmailNotification)
				&& !countEmailNotification(stagedFile, maxNumOfNotification)) {
			String emailSubject = null;
			if (ProcessorType.FILEWRITER.getCode().equals(stagedFile.getProcessorType())) {
				emailSubject = String.format(SLA_VIOLATION_SUBJECT, customerSLAConfiguration);
			} else {
				emailSubject = String.format(SLA_UPLOADER_VIOLATION_SUBJECT, customerSLAConfiguration);
			}
			StringBuilder body = new StringBuilder(emailSubject)
					.append("\n\n")
					.append("File : ")
					.append(stagedFile.getFileName());
			// send email notifications for sla violations
			sendEmail(processor, emailAddress, emailSubject, body.toString());
		} else {
			LOGGER.info("Email Notification to the User reached Maximum So unable to sending the email to user");
		}

	}
	
	/**
	 * This method used to check the max Number Of Notification send to client.
	 * 
	 * @param metaData
	 * @param maxNumOfNotification
	 * @return isReachedMaxNumOfNotification;
	 */
	private boolean countEmailNotification(StagedFile stagedFile, String maxNumOfNotification) {

		boolean isReachedMaxNumOfNotification = false;
		int notificationCount = 1;
		String metaData = stagedFile.getFileMetaData();
		
		if (!MailBoxUtil.isEmpty(metaData) && metaData.matches(EMAIL_NOTIFICATION_COUNT_PATTERN)) {
			notificationCount = Integer.parseInt(metaData);
			if (Integer.parseInt(maxNumOfNotification) > notificationCount) {
				notificationCount = notificationCount + 1 ;
				isReachedMaxNumOfNotification = false;
				metaData = String.valueOf(notificationCount);
			} else {
				isReachedMaxNumOfNotification = true;
			}     
		} else {
			metaData = String.valueOf(notificationCount);
			isReachedMaxNumOfNotification = false;
		}
		stagedFile.setFileMetaData(metaData);

		return isReachedMaxNumOfNotification;
	}
	
	/**
	 * Method which checks whether the SLAConfigured Time Limit exceeds or not
	 * by comparing the customer sla time with the current time
	 *
	 * @param slaConfiguredTime
	 * @return true if sla time exceeds otherwise false
	 */
	private boolean isTimeLimitExceeded(Timestamp slaConfiguredTime) {
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
		String slaTimeConfigurationUnit = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.MBX_SLA_CONFIG_UNIT, TimeUnit.MINUTES.name());
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
		List <String> processorTypes = new ArrayList<>();
		processorTypes.add(Sweeper.class.getCanonicalName());
		List <Processor> sweepers = config.findProcessorsByType(processorTypes);
		
		for (Processor procsr : sweepers) {
			
			LOGGER.debug("Retrieving Mailbox properties");
			List <String> mailboxPropsToBeRetrieved = new ArrayList<>();
			mailboxPropsToBeRetrieved.add(MailBoxConstants.TIME_TO_PICK_UP_FILE_POSTED_BY_MAILBOX);
			mailboxPropsToBeRetrieved.add(MailBoxConstants.MBX_RCVR_PROPERTY);
			mailboxPropsToBeRetrieved.add(MailBoxConstants.EMAIL_NOTIFICATION_FOR_SLA_VIOLATION);
			
			Map <String, String> mailboxProperties = procsr.retrieveMailboxProperties(mailboxPropsToBeRetrieved);
			// check whether sweeper got executed with in the configured sla time
			checkIfProcessorExecutedInSpecifiedSLAConfiguration(procsr, mailboxProperties);
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
	private void checkIfProcessorExecutedInSpecifiedSLAConfiguration (Processor processor, Map<String, String> mailboxProperties) throws IOException {
		
		String mailboxSLAConfiguration = mailboxProperties.get(MailBoxConstants.TIME_TO_PICK_UP_FILE_POSTED_BY_MAILBOX);
		String emailAddress = mailboxProperties.get(MailBoxConstants.MBX_RCVR_PROPERTY);		
		String enableEmailNotification = mailboxProperties.get(MailBoxConstants.EMAIL_NOTIFICATION_FOR_SLA_VIOLATION);
		LOGGER.debug("Mailbox Properties retrieved. mailbox sla - {}, emailAddress - {}, emailNotificationEnabled - {}", mailboxSLAConfiguration, emailAddress, enableEmailNotification);
		
		if (MailBoxUtil.isEmpty(mailboxSLAConfiguration)) {
			LOGGER.info(constructMessage("mailbox sla is not configured in mailbox, using the default mailbox sla configuration"));
			mailboxSLAConfiguration = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_MAILBOX_SLA);
		}
		if (MailBoxUtil.isEmpty(enableEmailNotification)) {
			enableEmailNotification = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_SLA_EMAIL_NOTIFICATION);					
		}
		
		boolean isEmailNotificationEnabled = Boolean.valueOf(enableEmailNotification);
		FSMStateDAO procDAO = new FSMStateDAOBase();

		List<FSMStateValue> listfsmStateVal = null;

		LOGGER.info(constructMessage("checking whether the processor {} is executed with in the specified SLA configuration time"), processor.getProcsrName());
		listfsmStateVal = procDAO.findExecutingProcessorsByProcessorId(processor.getPguid(), getSLAConfigurationAsTimeStamp(mailboxSLAConfiguration));

		// If the list is empty then the processor is not executed at all during the specified sla time.
		if (null == listfsmStateVal || listfsmStateVal.isEmpty()) {
		    LOGGER.info(constructMessage("The processor {} was not executed with in the specified SLA configuration time"), processor.getProcsrName());
		    notifySLAViolationToUser(processor, mailboxSLAConfiguration, emailAddress, isEmailNotificationEnabled);
			return;
		}  

		// If the processor is executed during the specified sla time but got failed.
		if (null != listfsmStateVal && !listfsmStateVal.isEmpty()) {
			
			for (FSMStateValue fsmStateVal : listfsmStateVal) {

				if (fsmStateVal.getValue().equals(ExecutionState.FAILED.value())) {
				    LOGGER.info(constructMessage("The processor {} was executed but got failed with in the specified SLA configuration time"), processor.getProcsrName());
				    notifySLAViolationToUser(processor, mailboxSLAConfiguration, emailAddress, isEmailNotificationEnabled);
				}
			}
		}
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

		LOGGER.info("Retrieving processors of type uploader or filewriter for mailbox {}", mailboxId);
		// get processor of type remote uploader of given mailbox id
		ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
		List <Processor> processors = processorDAO.findSpecificProcessorTypesOfMbx(mailboxId, getCannonicalNamesofSpecificProcessors(CUSTOMER_SLA));
		// always get the first available processor because there
		// will be either one uploader or file writer available for each mailbox
		Processor processor = (null != processors && processors.size() > 0) ? processors.get(0) : null;
		return processor;

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
		EmailInfoDTO emailInfo = new EmailInfoDTO(mailbox.getMbxName(), mailbox.getPguid(), processor.getProcsrName(), emailAddressList, emailSubject, emailSubject, true, false);
		EmailNotifier.sendEmail(emailInfo);
	}
	
	/**
	 * Method to notify SLA violations to User
	 * 
	 * @param processor - processor for which sla violated
	 * @param mailboxSLAConfiguration - sla configuration
	 * @param emailAddress - email address to which the notification to be sent
	 * @param isEmailNotificationEnabled - if true notification will be sent.
	 */
	private void notifySLAViolationToUser(Processor processor, String mailboxSLAConfiguration, String emailAddress, boolean isEmailNotificationEnabled) {
		
	    if (isEmailNotificationEnabled) {
	    	
			String emailSubject = String.format(SLA_MBX_VIOLATION_SUBJECT, mailboxSLAConfiguration);
			sendEmail(processor, emailAddress, emailSubject, emailSubject);
			LOGGER.info(constructMessage("The SLA violations are notified to the user by sending email for the prcocessor {}"), processor.getProcsrName());
	    } else {
	    	LOGGER.info(constructMessage("The SLA violations are not notified to the user for the prcocessor {} since the email notification for SLA is disabled"), processor.getProcsrName());
	    }
	}
}
