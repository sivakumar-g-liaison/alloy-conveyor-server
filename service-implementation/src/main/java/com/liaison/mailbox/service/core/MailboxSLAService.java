package com.liaison.mailbox.service.core;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.util.ISO8601Util;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.jpa.dao.FSMStateDAO;
import com.liaison.mailbox.jpa.dao.FSMStateDAOBase;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.jpa.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.jpa.model.FSMStateValue;
import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.jpa.model.MailBoxProperty;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.MailboxSLAResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;

public class MailboxSLAService {

	private static final Logger LOG = LogManager.getLogger(MailboxSLAService.class);
	
	private static String SLA_VIOLATION_NOTIFICATION = "Mailbox %s does not adhere to SLA";
	private static String SLA_VIOLATION_NOTIFICATION_MESSAGE = "Mailbox %s does not adhere to SLA Rule \"%s - %s\".";
	private static String SLA_NOTIFICATION_FAILURE_INFO = "\n\n The last execution dated %s got failed.";
	private static String SLA_RULE_1 = "Time to pick up file posted to mailbox";
	private static String SLA_RULE_2 = "Time to pick up file posted by mailbox";
	
	/**
	 * Iterate all Mailboxes and check whether Mailbox satisfies the SLA Rules
	 * @throws IOException 
	 * 
	 * @throws MailBoxConfigurationServicesException
	 */
	public MailboxSLAResponseDTO validateMailboxSLARules() throws IOException {
		
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
				emailBody = new StringBuilder(String.format(SLA_VIOLATION_NOTIFICATION_MESSAGE, mailboxName, SLA_RULE_1, timeToPickUpFilePostedToMailbox));
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
						emailBody = new StringBuilder(String.format(SLA_VIOLATION_NOTIFICATION_MESSAGE, mailboxName, SLA_RULE_1, timeToPickUpFilePostedToMailbox)).append(String.format(SLA_NOTIFICATION_FAILURE_INFO, dateUtil.fromTimestamp(fsmStateVal.getCreatedDate())));
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
	
}
