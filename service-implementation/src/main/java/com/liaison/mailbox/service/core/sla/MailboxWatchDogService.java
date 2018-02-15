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

import static com.liaison.mailbox.service.util.MailBoxUtil.DATACENTER_NAME;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.logging.LogTags;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.rtdm.dao.MailboxRTDMDAO;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAO;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAOBase;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.core.email.EmailInfoDTO;
import com.liaison.mailbox.service.core.email.EmailNotifier;
import com.liaison.mailbox.service.core.processor.ConditionalSweeper;
import com.liaison.mailbox.service.core.processor.DirectorySweeper;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorFactory;
import com.liaison.mailbox.service.dto.GlassMessageDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ConditionalSweeperPropertiesDTO;
import com.liaison.mailbox.service.glass.util.MailboxGlassMessageUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

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

/**
 * Updates LENS status for the customer picked up the files
 * 
 * @author OFS
 */
public class MailboxWatchDogService {

	private static final Logger LOGGER = LogManager.getLogger(MailboxWatchDogService.class);

    private static final String LOG_PREFIX = "WatchDog ";
    private static final String SEPARATOR = " :";
    private static final String SLA_VIOLATION_SUBJECT = "Files are not picked up by the customer within configured SLA of %s minutes";
	private static final String SLA_UPLOADER_VIOLATION_SUBJECT = "Files are not uploaded to the customer within configured SLA of %s minutes";
	private static final String SLA_MBX_VIOLATION_SUBJECT = "Files are not picked up by the Alloy Mailbox within configured SLA of %s minutes";
	private static final String EMAIL_NOTIFICATION_COUNT_PATTERN= "^[0-9]*$";
	private static final String MINUTES = "MINUTES";
	private static final String HOURS = "HOURS";
	private static final String PROCESS_DC = "process_dc";

    private static StringBuilder QUERY_STRING = new StringBuilder().append("SELECT sf.* FROM STAGED_FILE sf")
            .append(" INNER JOIN PROCESSOR_EXEC_STATE pes ON sf.PROCESSOR_GUID = pes.PROCESSOR_ID")
            .append(" WHERE sf.STATUS in ('ACTIVE', 'FAILED')")
            .append(" AND sf.PROCESSOR_TYPE IN ('FILEWRITER', 'REMOTEUPLOADER')")
            .append(" AND sf.CLUSTER_TYPE IN (?1)")
            .append(" AND pes.EXEC_STATUS != 'PROCESSING'")
            .append(" AND sf.PROCESS_DC =:")
            .append(PROCESS_DC);
    
	private String uniqueId;

    private String constructMessage(String... messages) {

        StringBuilder msgBuf = new StringBuilder()
                .append(LOG_PREFIX)
                .append(SEPARATOR)
                .append(uniqueId)
                .append(SEPARATOR);
        for (String str : messages) {
            msgBuf.append(str).append(SEPARATOR);
        }
        return msgBuf.toString();
    }

    public MailboxWatchDogService() {
		uniqueId = MailBoxUtil.getGUID();
	}

	/**
	 * Poll and update LENS status for the customer picked up files as well as doing sla validation
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
		ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();

		try {

			// Getting the mailbox.
			em = DAOUtil.getEntityManager(MailboxRTDMDAO.PERSISTENCE_UNIT_NAME);
			tx = em.getTransaction();
			tx.begin();

            List<StagedFile> stagedFiles = em
                    .createNativeQuery(QUERY_STRING.toString(), StagedFile.class)
                    .setParameter(1, MailBoxUtil.CLUSTER_TYPE)
                    .setParameter(PROCESS_DC, DATACENTER_NAME)
                    .getResultList();

            List<GlassMessageDTO> glassMessageDTOs = new ArrayList<>();

			if (stagedFiles == null || stagedFiles.isEmpty()) {
				LOGGER.debug(constructMessage("No active files found"));
				return;
			}

			Processor processor = null;
			for (StagedFile stagedFile : stagedFiles) {

			    // Fish tag global process id
                ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, stagedFile.getGlobalProcessId());
				filePath = stagedFile.getFilePath();
				fileName = stagedFile.getFileName();

				if (MailBoxUtil.isEmpty(filePath)) {
					//This may be old staged file and this needs to be in-activated
				    inactiveStagedFile(stagedFile, updatedStatusList);
					continue;
				}

                // get the processor from processor Id
                processor = processors.computeIfAbsent(stagedFile.getProcessorId()
                        , k -> config.find(Processor.class, stagedFile.getProcessorId()));

                // Inactive the staged files if processor is deleted or inactive
                // This may be old staged file and this needs to be in-activated since process is not found
                if (null == processor) {
                    inactiveStagedFile(stagedFile, updatedStatusList);
                    continue;
                }

                //get the mailbox properties
                int staleFileTTL = MailBoxUtil.getStaleFileTTLValue(processor.getProcsrProperties());

                boolean isFileExist = Files.exists(Paths.get(filePath + File.separatorChar + fileName), LinkOption.NOFOLLOW_LINKS);
                if (isFileExist) {

                    LOGGER.debug(constructMessage("File {} is exists at the location {}. so doing customer sla validation"), fileName, filePath);

					//file is not picked up beyond the configured TTL it should deleted immediately and
					//No need to call the validateCustomerSLA
					if (validateTTLUnit(stagedFile, staleFileTTL)) {
                        try {

                            Files.delete(Paths.get(filePath + File.separatorChar + fileName));
                            LOGGER.warn(constructMessage("{} : File {} is deleted in the filePath {}"), processor.getPguid(), fileName, filePath);
                            inactiveStagedFile(stagedFile, updatedStatusList);
                        } catch (IOException e) {
                            LOGGER.error(constructMessage("{} : Unable to delete a stale file {} in the filePath {}"), processor.getPguid(), fileName, filePath);
                        }
                        continue;
					}

					//Check SLA and send notification to the user
					validateCustomerSLA(stagedFile, processor);
					updatedNotificationCountList.add(stagedFile);
					continue;
				}

				LOGGER.debug(constructMessage("File {} does not exist at the location {}"), fileName, filePath);
				// if the processor type is uploader then Lens updation should not happen
				// even if the file does not exist as the lens updation is already taken care by the corresponding uploader
                if (ProcessorType.REMOTEUPLOADER.getCode().equals(stagedFile.getProcessorType())) {

                    if (validateTTLUnit(stagedFile, staleFileTTL)) {
                        inactiveStagedFile(stagedFile, updatedStatusList);
                        LOGGER.warn(constructMessage("{} : File {} is not present but staged file entry exceeded and it is active."), stagedFile.getProcessorId(), stagedFile.getFileName());
                    }
                    continue;
                }

                GlassMessageDTO glassMessageDTO = new GlassMessageDTO();
                glassMessageDTO.setGlobalProcessId(stagedFile.getGPID());
                glassMessageDTO.setProcessorType(ProcessorType.findByName(stagedFile.getProcessorType()));
                glassMessageDTO.setProcessProtocol(MailBoxUtil.getProtocolFromFilePath(filePath));
                glassMessageDTO.setFileName(fileName);
                glassMessageDTO.setFilePath(filePath);
                glassMessageDTO.setFileLength(0);
                glassMessageDTO.setStatus(ExecutionState.COMPLETED);
                glassMessageDTO.setMessage("File is picked up by the customer or another process");
                glassMessageDTO.setPipelineId(null);
                glassMessageDTO.setFirstCornerTimeStamp(null);

                glassMessageDTOs.add(glassMessageDTO);
                inactiveStagedFile(stagedFile, updatedStatusList);

			}

            //updated the stagedFile with latest notification count.
			for (StagedFile updatedNotificationCount : updatedNotificationCountList) {
	            em.merge(updatedNotificationCount);
	        }

			for (StagedFile updatedFile : updatedStatusList) {
	            em.merge(updatedFile);
	        }

            glassMessageDTOs.forEach((glassMessage) -> {
                MailboxGlassMessageUtil.logGlassMessage(glassMessage);
                LOGGER.info(constructMessage("{} : Updated LENS status for the file {} and location is {}"), glassMessage.getGlobalProcessId(), glassMessage.getFileName(), glassMessage.getFilePath());
            });

            tx.commit();

        } catch (Exception e) {
            if (null != tx && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (em != null) {
                em.close();
            }
            ThreadContext.clearMap();
        }

	}

    /**
	 * Method to inactive the stagedFile
	 * 
	 * @param stagedFile
	 * @param updatedStatusList
	 */
	private void inactiveStagedFile(StagedFile stagedFile, List<StagedFile> updatedStatusList) {

	    stagedFile.setStagedFileStatus(EntityStatus.INACTIVE.value());
        stagedFile.setModifiedDate(MailBoxUtil.getTimestamp());
        updatedStatusList.add(stagedFile);
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
		return processor.retrieveMailboxProperties(mailboxPropsToBeRetrieved);
	}
	
    /**
     *  Method to validate the expire time.
     *
     * @param stagedFile staged file entity
     * @param ttl ttl value
     * @return boolean
     */
    private boolean validateTTLUnit(StagedFile stagedFile, int ttl) {

        if (0 == ttl) {
            LOGGER.debug(constructMessage("ttl is not configured in mailbox, using the default TTL configuration"));
            ttl = Integer.parseInt(MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.MAILBOX_PAYLOAD_TTL_DAYS,
                    MailBoxConstants.MAILBOX_PAYLOAD_TTL_DAYS_DEFAULT ));
        }

        Timestamp expireTimestamp = getTTLAsTimestamp(ttl, stagedFile.getCreatedDate());
        return isTimeLimitExceeded(expireTimestamp);
    }

	/**
	 * Method to convert ttl into TimeStamp value
	 *
	 * @param ttlDay
	 * @param stagedFileTime
	 * @return Timestamp
	 */
	private Timestamp getTTLAsTimestamp(int ttlDay, Timestamp stagedFileTime) {

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
	 */
	private void validateCustomerSLA(StagedFile stagedFile, Processor processor) {

        Map<String, String> mailboxProperties = getMailboxProperties(processor);

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
		    LOGGER.debug(constructMessage("customer sla is not configured in mailbox, using the default customer sla configuration"));
			customerSLAConfiguration = (ProcessorType.FILEWRITER.getCode().equals(stagedFile.getProcessorType()))
										? MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_CUSTOMER_SLA)
										: MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_MAILBOX_SLA);
		}

		Timestamp customerSLATimeLimit = getCustomerSLAConfigurationAsTimeStamp(customerSLAConfiguration, stagedFile.getCreatedDate());
		if (isTimeLimitExceeded(customerSLATimeLimit)
		        && Boolean.valueOf(enableEmailNotification)
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
            LOGGER.debug(constructMessage(" {} : {} : Email Notification to the User reached Maximum So unable to sending the email to user"), processor.getProcsrName(), processor.getPguid());
        }

	}

	/**
	 * This method used to check the max Number Of Notification send to client.
	 * 
	 * @param stagedFile
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
	 */
	private Timestamp getCustomerSLAConfigurationAsTimeStamp(String customerSLAConfiguration, Timestamp stagedFileTime) {

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
	 */
	private Timestamp getSLAConfigurationAsTimeStamp(String slaConfiguration) {

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
	 */
	private int getSLATimeConfigurationUnit() {

		// get sla time configuration unit from properties file
		String slaTimeConfigurationUnit = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.MBX_SLA_CONFIG_UNIT, TimeUnit.MINUTES.name());
		int timeConfigurationUnit = 0;
		switch(slaTimeConfigurationUnit.toUpperCase()) {

		case MINUTES:
			timeConfigurationUnit = Calendar.MINUTE;
			break;
		case HOURS:
		default:
			timeConfigurationUnit = Calendar.HOUR;
			break;
		}
		return timeConfigurationUnit;
	}
	
	/**
	 * Iterate all Mailboxes and check whether Mailbox satisfies the SLA Rules
	 *
	 * @param mailboxStatus status of mailbox. could be ACTIVE or INACTIVE
	 */
	public void validateMailboxSLARule(EntityStatus mailboxStatus) {

		LOGGER.debug("Entering into validateMailboxSLARules.");

		ProcessorConfigurationDAO config = new ProcessorConfigurationDAOBase();

		LOGGER.debug("Retrieving all sweepers");
		List <String> processorTypes = new ArrayList<>();
		processorTypes.add(ProcessorType.SWEEPER.name());
		processorTypes.add(ProcessorType.CONDITIONALSWEEPER.name());

		//fetches the sweeper and conditionalsweeper based on mailbox and processor status
		List <Processor> sweepers = config.findProcessorsByType(processorTypes, mailboxStatus);

		for (Processor procsr : sweepers) {

			try {
				// sla validation must be done only if both mailbox and processors are active
				if (EntityStatus.ACTIVE.equals(mailboxStatus)) {

					LOGGER.debug("Retrieving Mailbox properties");
					List <String> mailboxPropsToBeRetrieved = new ArrayList<>();
					mailboxPropsToBeRetrieved.add(MailBoxConstants.TIME_TO_PICK_UP_FILE_POSTED_TO_MAILBOX);
					mailboxPropsToBeRetrieved.add(MailBoxConstants.MBX_RCVR_PROPERTY);
					mailboxPropsToBeRetrieved.add(MailBoxConstants.EMAIL_NOTIFICATION_FOR_SLA_VIOLATION);

					Map <String, String> mailboxProperties = procsr.retrieveMailboxProperties(mailboxPropsToBeRetrieved);
					// check whether sweeper got executed with in the configured sla time
					checkIfProcessorExecutedInSpecifiedSLAConfiguration(procsr, mailboxProperties);
				}

				// check sweeper location for stale file cleanup
				if (ProcessorType.SWEEPER.equals(procsr.getProcessorType())) {
				    DirectorySweeper directorySweeper = (DirectorySweeper) MailBoxProcessorFactory.getInstance(procsr);
				    directorySweeper.cleanupStaleFiles();
				} else {
				    ConditionalSweeper conditionalSweeper = (ConditionalSweeper) MailBoxProcessorFactory.getInstance(procsr);
				    conditionalSweeper.cleanupStaleFiles(((ConditionalSweeperPropertiesDTO) conditionalSweeper.getProperties()).getStaleFileTTL());
				}
				
			} catch (Exception e) {
                // Exceptions are handled gracefully so that sla validation can be continued for other processors.
                LOGGER.error(constructMessage("Error occurred in watchdog service during mailbox sla validation", e.getMessage()));
			}
		}
	}

    /**
     * Method which checks if the given processor is executed with in the given slaconfiguration time.
     *
     * @param processor         - processor for which sla has to be validated
     * @param mailboxProperties - mailbox properties
     */
    private void checkIfProcessorExecutedInSpecifiedSLAConfiguration(Processor processor, Map<String, String> mailboxProperties) {

        String mailboxSLAConfiguration = mailboxProperties.get(MailBoxConstants.TIME_TO_PICK_UP_FILE_POSTED_TO_MAILBOX);
        String emailAddress = mailboxProperties.get(MailBoxConstants.MBX_RCVR_PROPERTY);
        String enableEmailNotification = mailboxProperties.get(MailBoxConstants.EMAIL_NOTIFICATION_FOR_SLA_VIOLATION);
        LOGGER.debug("Mailbox Properties retrieved. mailbox sla - {}, emailAddress - {}, emailNotificationEnabled - {}", mailboxSLAConfiguration, emailAddress, enableEmailNotification);

        if (MailBoxUtil.isEmpty(mailboxSLAConfiguration)) {
            LOGGER.debug(constructMessage("mailbox sla is not configured in mailbox, using the default mailbox sla configuration"));
            mailboxSLAConfiguration = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_MAILBOX_SLA);
        }
        if (MailBoxUtil.isEmpty(enableEmailNotification)) {
            enableEmailNotification = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_SLA_EMAIL_NOTIFICATION);
        }

        boolean isEmailNotificationEnabled = Boolean.valueOf(enableEmailNotification);

        LOGGER.debug(constructMessage("checking whether the processor {} is executed with in the specified SLA configuration time"), processor.getProcsrName());
        ProcessorExecutionStateDAO procDAO = new ProcessorExecutionStateDAOBase();
        ProcessorExecutionState processorExecutionState = procDAO.findByProcessorId(processor.getPguid());

        // If the list is empty then the processor is not executed at all during the specified sla time.
        if (null == processorExecutionState) {
            LOGGER.debug(constructMessage("The processor {} was not executed with in the specified SLA configuration time"), processor.getProcsrName());
            notifySLAViolationToUser(processor, mailboxSLAConfiguration, emailAddress, isEmailNotificationEnabled);
            return;
        }

        Timestamp timestamp = getSLAConfigurationAsTimeStamp(mailboxSLAConfiguration);
        if (null != processorExecutionState.getLastExecutionDate() && 
                new Timestamp(processorExecutionState.getLastExecutionDate().getTime()).before(timestamp)) {
            LOGGER.error(constructMessage("The processor {} was not executed with in the specified SLA configuration time"), processor.getProcsrName());
            notifySLAViolationToUser(processor, mailboxSLAConfiguration, emailAddress, isEmailNotificationEnabled);
            return;
        }

        // If the processor is executed during the specified sla time but got failed.
        if (ExecutionState.FAILED.value().equals(processorExecutionState.getExecutionStatus())) {
            LOGGER.debug(constructMessage("The processor {} was executed but got failed with in the specified SLA configuration time"), processor.getProcsrName());
            notifySLAViolationToUser(processor, mailboxSLAConfiguration, emailAddress, isEmailNotificationEnabled);
        }
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
        EmailInfoDTO emailInfo = new EmailInfoDTO(mailbox.getMbxName(), mailbox.getPguid(), processor.getProcsrName(), emailAddressList, emailSubject, emailBody, true, false);
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
			LOGGER.debug(constructMessage("The SLA violations are notified to the user by sending email for the prcocessor {}"), processor.getProcsrName());
        } else {
            LOGGER.debug(constructMessage("The SLA violations are not notified to the user for the prcocessor {} since the email notification for SLA is disabled"), processor.getProcsrName());
        }
	}

}
