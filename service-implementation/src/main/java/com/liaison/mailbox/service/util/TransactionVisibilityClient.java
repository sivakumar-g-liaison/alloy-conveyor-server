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
import com.liaison.commons.message.glass.dom.MapItemType;
import com.liaison.commons.message.glass.dom.StatusCode;
import com.liaison.commons.message.glass.dom.TransactionVisibilityAPI;
import com.liaison.commons.message.glass.util.GlassMessageUtil;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.mailbox.enums.ExecutionState;

/**
 *
 * @author OFS
 *
 */
public class TransactionVisibilityClient {

	public static final String PROPERTY_COM_LIAISON_LENS_HUB = "com.liaison.lens.hub";
	public static final String MESSAGE_ERROR_INFO = "messageerrorinfo";
	public static final String DEFAULT_SENDER_NAME = "UNKNOWN";
	final String DEFAULT_SENDER_PGUID = "00000000000000000000000000000000";

	protected static DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();
	private static final Logger logger = LogManager.getLogger(TransactionVisibilityClient.class);

	private TransactionVisibilityAPI visibilityAPI;

	public TransactionVisibilityClient() {
		visibilityAPI = new TransactionVisibilityAPI();
		visibilityAPI.setHub(configuration.getString(PROPERTY_COM_LIAISON_LENS_HUB));
	}

	public void logToGlass(GlassMessage message) {

		visibilityAPI.getAdditionalInformation().clear();

		// Log TransactionVisibilityAPI
		MapItemType item;

		if (message.getExecutionId() != null && !message.getExecutionId().equals("")) {
			item = new MapItemType();
			item.setKey("proc-exec-id");
			item.setValue(message.getExecutionId());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (message.getMailboxId() != null && !message.getMailboxId().equals("")) {
			item = new MapItemType();
			item.setKey("mailbox-id");
			item.setValue(message.getMailboxId());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (message.getProcessorId() != null && !message.getProcessorId().equals("")) {
			item = new MapItemType();
			item.setKey("processor-id");
			item.setValue(message.getProcessorId());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (message.getTenancyKey() != null && !message.getTenancyKey().equals("")) {
			item = new MapItemType();
			item.setKey("tenancy-key");
			item.setValue(message.getTenancyKey());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (message.getServiceInstandId() != null && !message.getServiceInstandId().equals("")) {
			item = new MapItemType();
			item.setKey("siid");
			item.setValue(message.getServiceInstandId());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (message.getPipelineId() != null && !message.getPipelineId().equals("")) {
			item = new MapItemType();
			item.setKey("pipeline-id");
			item.setValue(message.getPipelineId());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (message.getTransferProfileName() != null && !message.getTransferProfileName().equals("")) {
			item = new MapItemType();
			item.setKey("tranfer-profile-name");
			item.setValue(message.getTransferProfileName());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (message.getStagedFileId() != null && !message.getStagedFileId().equals("")) {
			item = new MapItemType();
			item.setKey("staged-file-id");
			item.setValue(message.getStagedFileId());
			visibilityAPI.getAdditionalInformation().add(item);
		}

		if (message.getMeta() != null && !message.getMeta().equals("")) {
			item = new MapItemType();
			item.setKey("meta");
			item.setValue(message.getMeta());
			visibilityAPI.getAdditionalInformation().add(item);
		}
		
		if (message.getCategory() != null && !message.getCategory().equals("")) {
			visibilityAPI.setCategory(message.getProtocol() + ":" + message.getCategory().getCode());
		}
		
		if (ExecutionState.PROCESSING.value().equals(message.getStatus().value())) {
			visibilityAPI.setStatus(StatusCode.P);
	        visibilityAPI.setInSize(message.getInSize());
	        visibilityAPI.setArrivalTime(GlassMessageUtil.convertToXMLGregorianCalendar(new Date()));
	        visibilityAPI.setInAgent(message.getInAgent());
		} else if (ExecutionState.QUEUED.value().equals(message.getStatus().value())) {
			visibilityAPI.setStatus(StatusCode.B);
		} else if (ExecutionState.READY.value().equals(message.getStatus().value())) {
			visibilityAPI.setStatus(StatusCode.R);
		} else if (ExecutionState.FAILED.value().equals(message.getStatus().value())) {
			visibilityAPI.setStatus(StatusCode.F);
		} else if (ExecutionState.COMPLETED.value().equals(message.getStatus().value())) {
			visibilityAPI.setStatus(StatusCode.S);
			visibilityAPI.setOutSize(message.getOutSize());
			visibilityAPI.setOutAgent(message.getOutAgent());
		} else if (ExecutionState.SKIPPED.value().equals(message.getStatus().value())) {
			visibilityAPI.setStatus(StatusCode.N);
		} else if (ExecutionState.STAGED.value().equals(message.getStatus().value())) {
			visibilityAPI.setStatus(StatusCode.G);
		}

		visibilityAPI.setId(message.getGlobalPId());
		visibilityAPI.setGlobalId(message.getGlobalPId());
	    visibilityAPI.setGlassMessageId(MailBoxUtil.getGUID());
	    visibilityAPI.setVersion(String.valueOf(System.currentTimeMillis()));
		visibilityAPI.setStatusDate(GlassMessageUtil.convertToXMLGregorianCalendar(new Date()));

		logger.info(GlassMessageMarkers.GLASS_MESSAGE_MARKER, visibilityAPI);
		logger.info("TransactionVisibilityAPI with status {} logged for GPID :{} and Glass Message Id is {}", message.getStatus().value(),
		        message.getGlobalPId(), visibilityAPI.getId());
		
	}
}
