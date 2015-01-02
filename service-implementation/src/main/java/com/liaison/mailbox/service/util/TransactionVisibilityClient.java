package com.liaison.mailbox.service.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.common.log4j2.markers.GlassMessageMarkers;
import com.liaison.commons.message.glass.dom.MapItemType;
import com.liaison.commons.message.glass.dom.StatusCode;
import com.liaison.commons.message.glass.dom.TransactionVisibilityAPI;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.mailbox.enums.ExecutionState;

public class TransactionVisibilityClient {
	
	 public static final String PROPERTY_COM_LIAISON_LENS_HUB = "com.liaison.lens.hub";
	 public static final String MESSAGE_ERROR_INFO = "messageerrorinfo";

	 protected static DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();
	 private static final Logger logger = LogManager.getLogger(TransactionVisibilityClient.class);

	 private TransactionVisibilityAPI visibilityAPI;
	 
	 public TransactionVisibilityClient (String glassMessageId){
		 visibilityAPI = new TransactionVisibilityAPI();
		 visibilityAPI.setGlassMessageId(glassMessageId);
		 visibilityAPI.setVersion(String.valueOf(System.currentTimeMillis()));
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
		 
				  
		 logger.info(GlassMessageMarkers.GLASS_MESSAGE_MARKER, visibilityAPI);
		 logger.debug("TransactionVisibilityAPI with status {} logged for execution :{}",message.getStatus().value(),message.getExecutionId());	 
		 
	 }
}
