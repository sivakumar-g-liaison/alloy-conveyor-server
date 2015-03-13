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
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.common.log4j2.markers.GlassMessageMarkers;
import com.liaison.commons.message.glass.dom.MapItemType;
import com.liaison.commons.message.glass.dom.StatusCode;
import com.liaison.commons.message.glass.dom.TransactionVisibilityAPI;
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

	 public TransactionVisibilityClient (String glassMessageId){
		 visibilityAPI = new TransactionVisibilityAPI();
		 visibilityAPI.setGlassMessageId(glassMessageId);
		 visibilityAPI.setVersion(String.valueOf(System.currentTimeMillis()));
		 visibilityAPI.setHub(configuration.getString(PROPERTY_COM_LIAISON_LENS_HUB));
		 visibilityAPI.setSenderName(DEFAULT_SENDER_NAME);
		 visibilityAPI.setSenderId(DEFAULT_SENDER_PGUID);

	 }


	 public void logToGlass(GlassMessage message){

		 MapItemType item = new MapItemType();
		 item.setKey("proc-exec-id");
		 item.setValue(message.getExecutionId());
		 visibilityAPI.getAdditionalInformation().add(item);

		 item = new MapItemType();
		 item.setKey("mailbox-id");
		 item.setValue(message.getMailboxId());
		 visibilityAPI.getAdditionalInformation().add(item);

		 item = new MapItemType();
		 item.setKey("processor-id");
		 item.setValue(message.getProcessorId());
		 visibilityAPI.getAdditionalInformation().add(item);

		 item = new MapItemType();
		 item.setKey("tenancy-key");
		 item.setValue(message.getTenancyKey());
		 visibilityAPI.getAdditionalInformation().add(item);

		 item = new MapItemType();
		 item.setKey("siid");
		 item.setValue(message.getServiceInstandId());
		 visibilityAPI.getAdditionalInformation().add(item);

		 item = new MapItemType();
		 item.setKey("pipeline-id");
		 item.setValue(message.getPipelineId());
		 visibilityAPI.getAdditionalInformation().add(item);

		 visibilityAPI.setCategory(message.getProtocol()+":"+message.getCategory().getCode());
		 visibilityAPI.setId(message.getGlobalPId());
		 if(ExecutionState.PROCESSING.value().equals(message.getStatus().value())){
			 visibilityAPI.setStatus(StatusCode.P);
		 }else if (ExecutionState.QUEUED.value().equals(message.getStatus().value())){
			 visibilityAPI.setStatus(StatusCode.B);
		 }else if (ExecutionState.PROCESSING.value().equals(message.getStatus().value())){
			 visibilityAPI.setStatus(StatusCode.P);
		 }else if (ExecutionState.FAILED.value().equals(message.getStatus().value())){
			 visibilityAPI.setStatus(StatusCode.F);
		 }else if (ExecutionState.COMPLETED.value().equals(message.getStatus().value())){
			 visibilityAPI.setStatus(StatusCode.S);
		 }else if (ExecutionState.SKIPPED.value().equals(message.getStatus().value())){
			 visibilityAPI.setStatus(StatusCode.N);
		 }else if (ExecutionState.STAGED.value().equals(message.getStatus().value())){
			 visibilityAPI.setStatus(StatusCode.G);
		 }

		 visibilityAPI.setInAgent(message.getInAgent());
		 XMLGregorianCalendar t = toXmlGregorianCalendar(new Date().getTime());
	     visibilityAPI.setArrivalTime(t);
	     visibilityAPI.setStatusDate(t);


		 logger.info(GlassMessageMarkers.GLASS_MESSAGE_MARKER, visibilityAPI);
		 logger.debug("TransactionVisibilityAPI with status {} logged for execution :{}",message.getStatus().value(),message.getExecutionId());

	 }

	 /**
	     * Converts a given time in milliseconds into a {@link XMLGregorianCalendar} object.
	     * <p>
	     * The input milliseconds value represents the specified number of milliseconds since the standard base time known
	     * as "the epoch", namely January 1, 1970, 00:00:00 GMT.
	     *
	     * @param date
	     *            A given time corresponding to the number of milliseconds since January 1, 1970, 00:00:00 GMT
	     * @return A new instance of <code>XMLGregorianCalendar</code> representing the input time
	     */
	    public  XMLGregorianCalendar toXmlGregorianCalendar(final long date) {
	        try {
	            final GregorianCalendar calendar = new GregorianCalendar();
	            calendar.setTimeInMillis(date);
	            return DatatypeFactory.newInstance().newXMLGregorianCalendar(
	                calendar);
	        }
	        catch (final DatatypeConfigurationException ex) {
	            System.out.println("Unable to convert date '%s' to an XMLGregorianCalendar object");
	        }
			return null;
	    }
}
