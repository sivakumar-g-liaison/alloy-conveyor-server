/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.glass.util;

import java.util.Date;

import com.liaison.gem.service.dto.OrganizationDTO;
import com.liaison.mailbox.service.util.ServiceBrokerUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.common.log4j2.markers.GlassMessageMarkers;
import com.liaison.commons.message.glass.dom.ActivityStatusAPI;
import com.liaison.commons.message.glass.dom.GatewayType;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.commons.message.glass.dom.TimeStamp;
import com.liaison.commons.message.glass.dom.TimeStampAPI;
import com.liaison.commons.message.glass.util.GlassMessageUtil;
import com.liaison.commons.util.UUIDGen;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Class that contains the fields required for logging messages in LENS.
 * 
 * @author OFS
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
	private transient ExecutionTimestamp secondCornerTimestamp;
	private transient ExecutionTimestamp thirdCornerTimestamp;
	private transient ExecutionTimestamp fourthCornerTimestamp;

	private static final String MAILBOX_ASA_IDENTIFIER = "MAILBOX";

	private static final Logger logger = LogManager.getLogger(GlassMessage.class);

	DecryptableConfiguration config = LiaisonConfigurationFactory.getConfiguration();

	public GlassMessage(WorkTicket wrkTicket) {
		this.setGlobalPId(wrkTicket.getGlobalProcessId());
		this.setOutboundPipelineId(wrkTicket.getPipelineId());
		Long payloadSize = wrkTicket.getPayloadSize();
		if (payloadSize != null && payloadSize != -1L) {
			this.setOutSize(payloadSize);
		}
		this.setTransferProfileName((String) wrkTicket.getAdditionalContextItem(MailBoxConstants.DBX_WORK_TICKET_PROFILE_NAME));
		this.setProcessorId((String) wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_WORKTICKET_PROCESSOR_ID));
		this.setTenancyKey((String) wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_WORKTICKET_TENANCYKEY));
		this.setTransferProfileName((String) wrkTicket.getAdditionalContextItem(MailBoxConstants.DBX_WORK_TICKET_PROFILE_NAME));
		this.setServiceInstandId((String) wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_SERVICE_INSTANCE_ID));
		this.setMailboxId((String) wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_MAILBOX_ID));
	}

	public GlassMessage() {
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
	private String inboundPipelineId;
	private String outboundPipelineId;
	private GatewayType inAgent;
	private GatewayType outAgent;
	private String message;
	private Long inSize;
	private Long outSize;
	private String processId;
	private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverName;
	private String transferProfileName;
	private String stagedFileId;
	private String meta;
	private String inboundFileName;
	private String outboundFileName;
	private String mailboxName;
    private String senderIp;
    private String receiverIp;
    private boolean arrivalTime;
    private String adminErrorDetails;


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

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public GatewayType getOutAgent() {
		return outAgent;
	}

	public void setOutAgent(GatewayType outAgent) {
		this.outAgent = outAgent;
	}

	public void setOutAgent(String outAgent) {
        if (outAgent.contains("ftps")) {
            this.outAgent = GatewayType.FTPS;
        } else if (outAgent.contains("sftp")) {
            this.outAgent = GatewayType.SFTP;
        } else if (outAgent.contains("ftp")) {
            this.outAgent = GatewayType.FTP;
        }
    }

	public GatewayType getInAgent() {
		return inAgent;
	}

	public void setInAgent(GatewayType inAgent) {
		this.inAgent = inAgent;
	}

	public void setInAgent(String inAgent) {
	    if (inAgent.contains("ftps")) {
	        this.inAgent = GatewayType.FTPS;
        } else if (inAgent.contains("sftp")) {
            this.inAgent = GatewayType.SFTP;
        } else if (inAgent.contains("ftp")) {
            this.inAgent = GatewayType.FTP;
        }
    }

	public String getInboundPipelineId() {
		return inboundPipelineId;
	}

	public void setInboundPipelineId(String inboundPipelineId) {
		this.inboundPipelineId = inboundPipelineId;
	}

	public String getOutboundPipelineId() {
		return outboundPipelineId;
	}

	public void setOutboundPipelineId(String outboundPipelineId) {
		this.outboundPipelineId = outboundPipelineId;
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

	public void setInSize(Long inSize) {
		this.inSize = inSize;
	}

	public Long getInSize() {
		return inSize;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public Long getOutSize() {
	    if (outSize != null && (0L == outSize || -1L == outSize)) {
	        return null;
	    }
        return outSize;
    }

    public void setOutSize(Long outSize) {
        this.outSize = outSize;
    }

    public String getInboundFileName() {
        return inboundFileName;
    }

    public void setInboundFileName(String inboundFileName) {
        this.inboundFileName = inboundFileName;
    }

    public String getOutboundFileName() {
        return outboundFileName;
    }

    public void setOutboundFileName(String outboundFileName) {
        this.outboundFileName = outboundFileName;
    }
    
    public String getMailboxName() {
		return mailboxName;
	}

	public void setMailboxName(String mailboxName) {
		this.mailboxName = mailboxName;
	}

	public static void logTimestamp(Logger logger, String message, Object... objects) {
		if (logger != null) {
			logger.info(String.format("[TIME] %s | %s", new Date(System.currentTimeMillis()),
					String.format(message, objects)));
		}
	}

	public void logFirstCornerTimestamp() {
		firstCornerTimestamp = logBeginTimestamp(config.getString(PROPERTY_FIRST_CORNER_NAME, DEFAULT_FIRST_CORNER_NAME));
	}

	public void logFirstCornerTimestamp(ExecutionTimestamp timeStamp) {
		firstCornerTimestamp = timeStamp;
        logTimeStamp(timeStamp);
    }

	public void logSecondCornerTimestamp() {
		secondCornerTimestamp = logEndTimestamp(config.getString(DEFAULT_SECOND_CORNER_NAME, PROPERTY_SECOND_CORNER_NAME));
	}

	public void logThirdCrnerTimestamp() {
		thirdCornerTimestamp = logBeginTimestamp(config.getString(DEFAULT_THIRD_CORNER_NAME, PROPERTY_THIRD_CORNER_NAME));
	}

	public void logFourthCornerTimestamp() {
		fourthCornerTimestamp = logEndTimestamp(config.getString(PROPERTY_FOURTH_CORNER_NAME, DEFAULT_FOURTH_CORNER_NAME));
	}

	public ExecutionTimestamp logBeginTimestamp(String name) {
		ExecutionTimestamp timeStamp = ExecutionTimestamp.beginTimestamp(name);
		logTimeStamp(timeStamp);
		return timeStamp;
	}
	
	public void logEndTimestamp(String name, String sessionId) {
		ExecutionTimestamp timeStamp = ExecutionTimestamp.endTimestamp(name, sessionId);
		logTimeStamp(timeStamp);
	}

	public ExecutionTimestamp logEndTimestamp(String name) {
		ExecutionTimestamp timeStamp = ExecutionTimestamp.endTimestamp(name);
		logTimeStamp(timeStamp);
		return timeStamp;
	}

	private void logTimeStamp(ExecutionTimestamp timestamp) {
		logger.info(GlassMessageMarkers.GLASS_MESSAGE_MARKER, constructTimeStampAPI(timestamp));
	}

	private TimeStampAPI constructTimeStampAPI(ExecutionTimestamp timestamp) {
		return constructTimeStampAPI(ExecutionTimestamp.buildGlassTimeStamp(timestamp));
	}

	public String getPipelineId() {
		return getInboundPipelineId() == null ? getOutboundPipelineId() : getInboundPipelineId();
	}

	public boolean isArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(boolean arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

    public String getSenderIp() {
        return senderIp;
    }

    public void setSenderIp(String senderIp) {
        this.senderIp = senderIp;
    }

    public String getReceiverIp() {
        return receiverIp;
    }

    public void setReceiverIp(String receiverIp) {
        this.receiverIp = receiverIp;
    }

    private TimeStampAPI constructTimeStampAPI(TimeStamp glassTimeStamp) {

		TimeStampAPI timeStampAPI = new TimeStampAPI();
		timeStampAPI.setProcessId(getProcessId());
		timeStampAPI.setGlobalId(getGlobalPId());
		timeStampAPI.setPipelineId(getPipelineId());
		timeStampAPI.getTimeStamps().add(glassTimeStamp);
		timeStampAPI.setGlassMessageId(UUIDGen.getCustomUUID());

		return timeStampAPI;
	}

	/**
	 * Method to construct the Activity Status API.
	 *
	 * @param statusType
	 * @param message
	 * @param processorType
	 * @param techDescription
	 * @param processorProtocol
	 * @return ActivityStatusAPI
	 */
	private ActivityStatusAPI constructActivityStatusAPI(StatusType statusType, String message, String processorType, String techDescription, String processorProtocol) {

		ActivityStatusAPI activityStatusAPI = new ActivityStatusAPI();
		activityStatusAPI.setPipelineId(getPipelineId());
		activityStatusAPI.setProcessId(getProcessId());
		activityStatusAPI.setGlobalId(getGlobalPId());
		activityStatusAPI.setGlassMessageId(UUIDGen.getCustomUUID());

		com.liaison.commons.message.glass.dom.Status status = new com.liaison.commons.message.glass.dom.Status();
		status.setDate(GlassMessageUtil.convertToXMLGregorianCalendar(new Date()));
		
		StringBuilder lensMessage = new StringBuilder().append(MAILBOX_ASA_IDENTIFIER);
		if (!MailBoxUtil.isEmpty(processorProtocol)) {
			lensMessage.append(" ");
			lensMessage.append(processorProtocol);
		}
		if (!MailBoxUtil.isEmpty(processorType)) {
			lensMessage.append(" ");
			lensMessage.append(processorType);
		}
		if (!MailBoxUtil.isEmpty(techDescription)) {
			status.setTechDescription(techDescription);
		}
		
		if (message != null && !message.equals("")) {
				status.setDescription(lensMessage.toString() + ": " + message);
		} else {
			status.setDescription(MAILBOX_ASA_IDENTIFIER);
		}
		status.setStatusId(UUIDGen.getCustomUUID());
		status.setType(statusType);

		activityStatusAPI.getStatuses().add(status);
		
		return activityStatusAPI;
	}
	
	/**
	 * Method to log the ActivityStatusAPI along with processorProtocol.
	 * 
	 * @param processorProtocol
	 * @param message
	 * @param processorType
	 * @param statusType
	 */
	public void logProcessingStatus(String processorProtocol, String message, String processorType, StatusType statusType) {
		
		// Log ActivityStatusAPI
		ActivityStatusAPI activityStatusAPI = constructActivityStatusAPI(statusType, message, processorType, null, processorProtocol);
		logger.info(GlassMessageMarkers.GLASS_MESSAGE_MARKER, activityStatusAPI);
	}
	
	/**
	 * Method to log the ActivityStatusAPI.
	 * 
	 * @param statusType
	 * @param message
	 * @param processorType
	 */
	public void logProcessingStatus(StatusType statusType, String message, String processorType) {

		// Log ActivityStatusAPI
		ActivityStatusAPI activityStatusAPI = constructActivityStatusAPI(statusType, message, processorType, null, null);
		logger.info(GlassMessageMarkers.GLASS_MESSAGE_MARKER, activityStatusAPI);
	}

	/**
	 * Method to log the errors and technical description in ActivityStatusAPI
	 * and to log error details (tech description) in meta data.
	 * 
	 * @param statusType
	 * @param message
	 * @param processorType
	 * @param techDescription
	 */
	public void logProcessingStatus(StatusType statusType, String message, String processorType, String techDescription) {
		
		// Log ActivityStatusAPI
		ActivityStatusAPI activityStatusAPI = constructActivityStatusAPI(statusType, message, processorType, techDescription, null);
		this.setAdminErrorDetails(techDescription);
		logger.info(GlassMessageMarkers.GLASS_MESSAGE_MARKER, activityStatusAPI);
	}

	public ExecutionTimestamp getFirstCornerTimestamp() {
		return firstCornerTimestamp;
	}

	public void setFirstCornerTimestamp(ExecutionTimestamp firstCornerTimestamp) {
		this.firstCornerTimestamp = firstCornerTimestamp;
	}

	public ExecutionTimestamp getSecondCornerTimestamp() {
		return secondCornerTimestamp;
	}

	public void setSecondCornerTimestamp(ExecutionTimestamp secondCornerTimestamp) {
		this.secondCornerTimestamp = secondCornerTimestamp;
	}

	public ExecutionTimestamp getThirdCornerTimestamp() {
		return thirdCornerTimestamp;
	}

	public void setThirdCornerTimestamp(ExecutionTimestamp thirdCornerTimestamp) {
		this.thirdCornerTimestamp = thirdCornerTimestamp;
	}

	public ExecutionTimestamp getFourthCornerTimestamp() {
		return fourthCornerTimestamp;
	}

	public void setFourthCornerTimestamp(ExecutionTimestamp fourthCornerTimestamp) {
		this.fourthCornerTimestamp = fourthCornerTimestamp;
	}

    /**
     * Reads org details from SB and sets in TVAPI
     *
     * @param pipelineId pipeline pguid
     */
    public void setOrganizationDetails(String pipelineId) {

        OrganizationDTO org = ServiceBrokerUtil.getOrganizationByPipelineId(pipelineId);
        this.setSenderId(org.getPguid());
        this.setSenderName(org.getName());
    }
    
    public String getAdminErrorDetails() {
        return adminErrorDetails;
    }
    
    public void setAdminErrorDetails(String adminErrorDetails) {
        this.adminErrorDetails = adminErrorDetails;
    }
}
