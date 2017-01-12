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

import com.liaison.common.log4j2.markers.GlassMessageMarkers;
import com.liaison.commons.message.glass.dom.MapItemType;
import com.liaison.commons.message.glass.dom.StatusCode;
import com.liaison.commons.message.glass.dom.TransactionVisibilityAPI;
import com.liaison.commons.message.glass.util.GlassMessageUtil;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

/**
 * Java wrapper client for logging messages in LENS.
 *
 * @author OFS
 */
public class TransactionVisibilityClient {

	public static final String PROPERTY_COM_LIAISON_LENS_HUB = "com.liaison.lens.hub";
	public static final String MESSAGE_ERROR_INFO = "messageerrorinfo";
	public static final String DEFAULT_SENDER_NAME = "UNKNOWN";

	private static DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();
	private static final Logger logger = LogManager.getLogger(TransactionVisibilityClient.class);

	private static final String PROCESSOR_EXEC_ID = "proc-exec-id";
	private static final String MAILBOX_ID = "mailbox-id";
	private static final String MAILBOX_NAME = "mailbox-name";
	private static final String PROCESSOR_ID = "processor-id";
	private static final String TENANCY_KEY = "tenancy-key";
	private static final String SIID = "siid";
	private static final String INBOUND_PIPELINE_ID = "inbound-pipeline-id";
	private static final String OUTBOUND_PIPELINE_ID = "outbound-pipeline-id";
	private static final String TRANSFER_PROFILE_NAME = "transfer-profile-name";
	private static final String STAGED_FILE_ID = "staged-file-id";
	private static final String META = "meta";
	private static final String INBOUND_FILE_NAME = "inboundfilename";
	private static final String OUTBOUND_FILE_NAME = "outboundfilename";
	private static final String ORGANIZATION_ID = "organization-id";
	private static final String ORGANIZATION_NAME = "organization-name";

	private TransactionVisibilityAPI visibilityAPI;

	public TransactionVisibilityClient() {
		visibilityAPI = new TransactionVisibilityAPI();
		visibilityAPI.setHub(configuration.getString(PROPERTY_COM_LIAISON_LENS_HUB));
	}

	public void logToGlass(GlassMessage message) {

		visibilityAPI.getAdditionalInformation().clear();

		// Log TransactionVisibilityAPI
		MapItemType item;

		if (!MailBoxUtil.isEmpty(message.getExecutionId())) {
			item = new MapItemType();
			item.setKey(PROCESSOR_EXEC_ID);
			item.setValue(message.getExecutionId());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (!MailBoxUtil.isEmpty(message.getMailboxId())) {
			item = new MapItemType();
			item.setKey(MAILBOX_ID);
			item.setValue(message.getMailboxId());
			visibilityAPI.getAdditionalInformation().add(item);
		}
		
		if (!MailBoxUtil.isEmpty(message.getMailboxName())) {
			item = new MapItemType();
			item.setKey(MAILBOX_NAME);
			item.setValue(message.getMailboxName());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (!MailBoxUtil.isEmpty(message.getProcessorId())) {
			item = new MapItemType();
			item.setKey(PROCESSOR_ID);
			item.setValue(message.getProcessorId());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (!MailBoxUtil.isEmpty(message.getTenancyKey())) {
			item = new MapItemType();
			item.setKey(TENANCY_KEY);
			item.setValue(message.getTenancyKey());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (!MailBoxUtil.isEmpty(message.getServiceInstandId())) {
			item = new MapItemType();
			item.setKey(SIID);
			item.setValue(message.getServiceInstandId());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (!MailBoxUtil.isEmpty(message.getInboundPipelineId())) {
			item = new MapItemType();
			item.setKey(INBOUND_PIPELINE_ID);
			item.setValue(message.getInboundPipelineId());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (!MailBoxUtil.isEmpty(message.getOutboundPipelineId())) {
			item = new MapItemType();
			item.setKey(OUTBOUND_PIPELINE_ID);
			item.setValue(message.getOutboundPipelineId());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (!MailBoxUtil.isEmpty(message.getTransferProfileName())) {
			item = new MapItemType();
			item.setKey(TRANSFER_PROFILE_NAME);
			item.setValue(message.getTransferProfileName());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (!MailBoxUtil.isEmpty(message.getStagedFileId())) {
			item = new MapItemType();
			item.setKey(STAGED_FILE_ID);
			item.setValue(message.getStagedFileId());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (!MailBoxUtil.isEmpty(message.getMeta())) {
			item = new MapItemType();
			item.setKey(META);
			item.setValue(message.getMeta());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (!MailBoxUtil.isEmpty(message.getInboundFileName())) {
            item = new MapItemType();
            item.setKey(INBOUND_FILE_NAME);
            item.setValue(message.getInboundFileName());
            visibilityAPI.getAdditionalInformation().add(item);
        }

		if (!MailBoxUtil.isEmpty(message.getOutboundFileName())) {
            item = new MapItemType();
            item.setKey(OUTBOUND_FILE_NAME);
            item.setValue(message.getOutboundFileName());
            visibilityAPI.getAdditionalInformation().add(item);
        }
		
		if (!MailBoxUtil.isEmpty(message.getOrganizationName())) {
		    item = new MapItemType();
		    item.setKey(ORGANIZATION_NAME);
		    item.setValue(message.getOrganizationName());
		    visibilityAPI.getAdditionalInformation().add(item);
		}
		
		if (!MailBoxUtil.isEmpty(message.getOrganizationID())) {
		    item = new MapItemType();
		    item.setKey(ORGANIZATION_ID);
		    item.setValue(message.getOrganizationID());
		    visibilityAPI.getAdditionalInformation().add(item);
		}

        if (!MailBoxUtil.isEmpty(message.getSenderIp())) {
            GlassMessageUtil.logSenderAddress(visibilityAPI, message.getSenderIp());
        }

        if (!MailBoxUtil.isEmpty(message.getReceiverIp())) {
            GlassMessageUtil.logReceiverAddress(visibilityAPI, message.getReceiverIp());
        }

		if (message.getCategory() != null && !message.getCategory().equals("")) {
			if (MailBoxConstants.DROPBOX_PROCESSOR.equalsIgnoreCase(message.getProtocol())) {
				visibilityAPI.setCategory("MFT" + ":" + MailBoxConstants.DROPBOX_SERVICE_NAME);
			} else {
				visibilityAPI.setCategory(message.getProtocol() + ":" + message.getCategory().getCode());
			}
		}

		if (message.getFirstCornerTimestamp() != null) {
			GlassMessageUtil.logFirstCorner(visibilityAPI, message.getFirstCornerTimestamp().getTimestamp());
		}

		if (message.getSecondCornerTimestamp() != null) {
			GlassMessageUtil.logSecondCorner(visibilityAPI, message.getSecondCornerTimestamp().getTimestamp());
		}

		if (message.getThirdCornerTimestamp() != null) {
			GlassMessageUtil.logThirdCorner(visibilityAPI, message.getThirdCornerTimestamp().getTimestamp());
		}

		if (message.getFourthCornerTimestamp() != null) {
			GlassMessageUtil.logFourthCorner(visibilityAPI, message.getFourthCornerTimestamp().getTimestamp());
		}

		handleExecutionState(message);

		visibilityAPI.setId(message.getGlobalPId());
		visibilityAPI.setGlobalId(message.getGlobalPId());
	    visibilityAPI.setGlassMessageId(MailBoxUtil.getGUID());
	    visibilityAPI.setVersion(String.valueOf(System.currentTimeMillis()));
		visibilityAPI.setStatusDate(GlassMessageUtil.convertToXMLGregorianCalendar(new Date()));

		logger.info(GlassMessageMarkers.GLASS_MESSAGE_MARKER, visibilityAPI);
		logger.info("TransactionVisibilityAPI with status {} logged for GPID :{} and Glass Message Id is {}", message.getStatus().value(),
		        message.getGlobalPId(), visibilityAPI.getId());
		
	}

	/**
	 *
	 * @param message
     */
	private void handleExecutionState(GlassMessage message) {

		switch (message.getStatus()) {
			case PROCESSING :
				visibilityAPI.setStatus(StatusCode.P);
				if (null != message.getInSize()) {
					visibilityAPI.setInSize(message.getInSize());
				} else {
					logger.warn("The inbound size is null for the gpid {}", message.getGlobalPId());
				}
				visibilityAPI.setArrivalTime(GlassMessageUtil.convertToXMLGregorianCalendar(new Date()));
				visibilityAPI.setInAgent(message.getInAgent());
				break;
			case QUEUED :
				visibilityAPI.setStatus(StatusCode.B);
				break;
			case READY :
				visibilityAPI.setStatus(StatusCode.R);
				if (null != message.getOutSize()) {
					visibilityAPI.setOutSize(message.getOutSize());
				}
				if (null != message.getOutAgent()) {
					visibilityAPI.setOutAgent(message.getOutAgent());
				}
				break;
			case COMPLETED :
				visibilityAPI.setStatus(StatusCode.S);
				if (null != message.getOutSize()) {
					visibilityAPI.setOutSize(message.getOutSize());
				}
				visibilityAPI.setOutAgent(message.getOutAgent());
				break;
			case SKIPPED :
				visibilityAPI.setStatus(StatusCode.N);
				break;
			case STAGED :
				visibilityAPI.setStatus(StatusCode.G);
				break;
			case DUPLICATE :
				visibilityAPI.setStatus(StatusCode.U);
				if (null != message.getOutAgent()) {
					visibilityAPI.setOutAgent(message.getOutAgent());
				}
				break;
            case FAILED :
                visibilityAPI.setStatus(StatusCode.F);
                if (message.isArrivalTime()) {
                    visibilityAPI.setArrivalTime(GlassMessageUtil.convertToXMLGregorianCalendar(new Date()));
                    if (null != message.getInAgent()) {
                        visibilityAPI.setInAgent(message.getInAgent());
                    }
                }
				if (null != message.getOutAgent()) {
					visibilityAPI.setOutAgent(message.getOutAgent());
				}
                break;
            case VALIDATION_ERROR :
                visibilityAPI.setStatus(StatusCode.V);
                visibilityAPI.setArrivalTime(GlassMessageUtil.convertToXMLGregorianCalendar(new Date()));
                break;
            default:
                throw new RuntimeException("Invalid glass message status - " + message.getStatus());
		}

	}
}
