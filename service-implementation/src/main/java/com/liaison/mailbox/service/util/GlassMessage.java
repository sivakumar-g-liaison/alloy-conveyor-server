/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.util;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.common.log4j2.markers.GlassMessageMarkers;
import com.liaison.commons.message.glass.dom.ActivityStatusAPI;
import com.liaison.commons.message.glass.dom.GatewayType;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.commons.message.glass.dom.TimeStamp;
import com.liaison.commons.message.glass.dom.TimeStampAPI;
import com.liaison.commons.message.glass.util.GlassMessageUtil;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.framework.util.IdentifierUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;

/**
 * @author OFS
 *
 */
public class GlassMessage {

	// TimestampLogger
	public static final String DEFAULT_FIRST_CORNER_NAME = "FIRST CORNER";
	public static final String PROPERTY_FIRST_CORNER_NAME = "com.liaison.firstcorner.name";
	public static final String DEFAULT_SECOND_CORNER_NAME = "SECOND CORNER";
	public static final String PROPERTY_SECOND_CORNER_NAME = "com.liaison.secondcorner.name";
	public static final String DEFAULT_THIRD_CORNER_NAME = "THIRD CORNER";
	public static final String PROPERTY_THIRD_CORNER_NAME = "com.liaison.thirdcorner.name";
	public static final String DEFAULT_FOURTH_CORNER_NAME = "FOURTH CORNER";
	public static final String PROPERTY_FOURTH_CORNER_NAME = "com.liaison.fourthcorner.name";

	private transient ExecutionTimestamp firstCornerTimestamp;
	private transient ExecutionTimestamp thirdCornerTimestamp;

	private static final String MAILBOX_ASA_IDENTIFIER = "MAILBOX";

	private static final Logger logger = LogManager.getLogger(GlassMessage.class);

	DecryptableConfiguration config = LiaisonConfigurationFactory.getConfiguration();

	public GlassMessage(WorkTicket wrkTicket) {
		this.setGlobalPId(wrkTicket.getGlobalProcessId());
		this.setPipelineId(wrkTicket.getPipelineId());
		Long payloadSize = wrkTicket.getPayloadSize();
		if (payloadSize != null && payloadSize < Integer.MAX_VALUE) {
			this.setInSize((int) (long) payloadSize);
		}
		this.setTransferProfileName((String) wrkTicket.getAdditionalContextItem(MailBoxConstants.DBX_WORK_TICKET_PROFILE_NAME));
		this.setProcessorId((String) wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_WORKTICKET_PROCESSOR_ID));
		this.setTenancyKey((String) wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_WORKTICKET_TENANCYKEY));
		this.setTransferProfileName((String) wrkTicket.getAdditionalContextItem(MailBoxConstants.DBX_WORK_TICKET_PROFILE_NAME));
		this.setServiceInstandId((String) wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_SERVICE_INSTANCE_ID));
		this.setMailboxId((String) wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_MAILBOX_ID));
	}

	public GlassMessage() {
		// TODO Auto-generated constructor stub
	}

	private ProcessorType category;
	private ExecutionState status;
	private String globalPId;
	private String mailboxId;
	private String processorId;
	private String executionId;
	private String tenancyKey;
	private String serviceInstandId;
	private String protocol;
	private String pipelineId;
	private GatewayType inAgent;
	private GatewayType outAgent;
	private String message;
	private int inSize;
	private String processId;
	private String senderId;
	private String transferProfileName;
	private String stagedFileId;
	private String meta;

	public String getTransferProfileName() {
		return transferProfileName;
	}

	public void setTransferProfileName(String transferProfileName) {
		this.transferProfileName = transferProfileName;
	}

	public String getStagedFileId() {
		return stagedFileId;
	}

	public void setStagedFileId(String stagedFileId) {
		this.stagedFileId = stagedFileId;
	}

	public String getMeta() {
		return meta;
	}

	public void setMeta(String meta) {
		this.meta = meta;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public GatewayType getOutAgent() {
		return outAgent;
	}

	public void setOutAgent(GatewayType outAgent) {
		this.outAgent = outAgent;
	}

	public GatewayType getInAgent() {
		return inAgent;
	}

	public void setInAgent(GatewayType inAgent) {
		this.inAgent = inAgent;
	}

	public String getPipelineId() {
		return pipelineId;
	}

	public void setPipelineId(String pipelineId) {
		this.pipelineId = pipelineId;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public ProcessorType getCategory() {
		return category;
	}

	public void setCategory(ProcessorType category) {
		this.category = category;
	}

	public ExecutionState getStatus() {
		return status;
	}

	public void setStatus(ExecutionState status) {
		this.status = status;
	}

	public String getGlobalPId() {
		return globalPId;
	}

	public void setGlobalPId(String globalPId) {
		this.globalPId = globalPId;
	}

	public String getMailboxId() {
		return mailboxId;
	}

	public void setMailboxId(String mailboxId) {
		this.mailboxId = mailboxId;
	}

	public String getProcessorId() {
		return processorId;
	}

	public void setProcessorId(String processorId) {
		this.processorId = processorId;
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public String getTenancyKey() {
		return tenancyKey;
	}

	public void setTenancyKey(String tenancyKey) {
		this.tenancyKey = tenancyKey;
	}

	public String getServiceInstandId() {
		return serviceInstandId;
	}

	public void setServiceInstandId(String serviceInstandId) {
		this.serviceInstandId = serviceInstandId;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setInSize(int inSize) {
		this.inSize = inSize;
	}

	public int getInSize() {
		return inSize;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public static void logTimestamp(Logger logger, String message, Object... objects) {
		if (logger != null) {
			logger.info(String.format("[TIME] %s | %s", new Date(System.currentTimeMillis()),
					String.format(message, objects)));
		}
	}

	public void logFirstCornerTimestamps() {
		firstCornerTimestamp = logBeginTimestamp(config.getString(PROPERTY_FIRST_CORNER_NAME, DEFAULT_FIRST_CORNER_NAME));
	}

	public void logFirstCornerTimestamp(ExecutionTimestamp timeStamp) {
        logTimeStamp(timeStamp);
    }

	public void logSecondCornerTimestamp() {
		logEndTimestamp(config.getString(DEFAULT_SECOND_CORNER_NAME, PROPERTY_SECOND_CORNER_NAME));
	}

	public void logThirdCrnerTimestamp() {
		thirdCornerTimestamp = logBeginTimestamp(config.getString(DEFAULT_THIRD_CORNER_NAME, PROPERTY_THIRD_CORNER_NAME));
	}

	public void logFourthCornerTimestamp() {
		logEndTimestamp(config.getString(PROPERTY_FOURTH_CORNER_NAME, DEFAULT_FOURTH_CORNER_NAME));
	}

	private ExecutionTimestamp logBeginTimestamp(String name) {
		ExecutionTimestamp timeStamp = ExecutionTimestamp.beginTimestamp(name);
		logTimeStamp(timeStamp);
		return timeStamp;
	}
	
	public void logEndTimestamp(String name, String sessionId) {
		ExecutionTimestamp timeStamp = ExecutionTimestamp.endTimestamp(name, sessionId);
		logTimeStamp(timeStamp);
	}

	public void logEndTimestamp(String name) {
		ExecutionTimestamp timeStamp = ExecutionTimestamp.endTimestamp(name);
		logTimeStamp(timeStamp);
	}

	private void logTimeStamp(ExecutionTimestamp timestamp) {
		logger.info(GlassMessageMarkers.GLASS_MESSAGE_MARKER, constructTimeStampAPI(timestamp));
	}

	private TimeStampAPI constructTimeStampAPI(ExecutionTimestamp timestamp) {
		return constructTimeStampAPI(ExecutionTimestamp.buildGlassTimeStamp(timestamp));
	}

	private TimeStampAPI constructTimeStampAPI(TimeStamp glassTimeStamp) {

		TimeStampAPI timeStampAPI = new TimeStampAPI();
		timeStampAPI.setProcessId(getProcessId());
		timeStampAPI.setGlobalId(getGlobalPId());
		timeStampAPI.setPipelineId(getPipelineId());
		timeStampAPI.getTimeStamps().add(glassTimeStamp);

		return timeStampAPI;
	}

	public void logProcessingStatus(StatusType statusType, String message) {

		// Log ActivityStatusAPI
		ActivityStatusAPI activityStatusAPI = new ActivityStatusAPI();
		activityStatusAPI.setPipelineId(getPipelineId());
		activityStatusAPI.setProcessId(getProcessId());
		activityStatusAPI.setGlobalId(getGlobalPId());
		activityStatusAPI.setGlassMessageId(IdentifierUtil.getUuid());

		com.liaison.commons.message.glass.dom.Status status = new com.liaison.commons.message.glass.dom.Status();
		status.setDate(GlassMessageUtil.convertToXMLGregorianCalendar(new Date()));
		if (message != null && !message.equals("")) {
			status.setDescription(MAILBOX_ASA_IDENTIFIER + ": " + message);
		} else {
			status.setDescription(MAILBOX_ASA_IDENTIFIER);
		}
		status.setStatusId(IdentifierUtil.getUuid());
		status.setType(statusType);

		activityStatusAPI.getStatuses().add(status);

		logger.info(GlassMessageMarkers.GLASS_MESSAGE_MARKER, activityStatusAPI);
	}
}
