/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.messagebus.client.exceptions.ClientUnavailableException;
import com.liaison.commons.util.StringUtil;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAO;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAOBase;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.service.core.email.EmailInfoDTO;
import com.liaison.mailbox.service.core.email.EmailNotifier;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ExecutingProcessorsDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorExecutionStateResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.UpdateProcessorExecutionStateResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.UpdateProcessorsExecutionStateResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.topic.TopicMessageDTO;
import com.liaison.mailbox.service.topic.producer.MailBoxTopicMessageProducer;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.netflix.config.ConfigurationManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.liaison.mailbox.MailBoxConstants.MAILBOX_STUCK_PROCESSOR_RECEIVER;
import static com.liaison.mailbox.MailBoxConstants.MAILBOX_STUCK_PROCESSOR_TIME_UNIT;
import static com.liaison.mailbox.MailBoxConstants.MAILBOX_STUCK_PROCESSOR_TIME_VALUE;
import static com.liaison.mailbox.MailBoxConstants.STUCK_PROCESSORS_IN_RELAY;
import static com.liaison.mailbox.service.util.MailBoxUtil.getEnvironmentProperties;

/**
 * class which contains processor execution configuration information.
 */
public class ProcessorExecutionConfigurationService extends GridServiceRTDM<ProcessorExecutionState> {

    private static final Logger LOG = LogManager.getLogger(ProcessorExecutionConfigurationService.class);
    private static final String PROCESSORS = "Processors";
    private static final String EXECUTING_PROCESSORS = "running processors";

    /**
     * Method to get the executing processors
     * 
     * @param page
     * @param pageSize
     * @param sortInfo
     * @param filterText
     * @return
     */
    public GetProcessorExecutionStateResponseDTO getExecutingProcessors(String page, String pageSize, String sortInfo, String filterText) {
        
        LOG.debug("Entering into get all executing Processors.");
        GetProcessorExecutionStateResponseDTO serviceResponse = new GetProcessorExecutionStateResponseDTO();

        try {

            GridResult<ProcessorExecutionState> result = getGridItems(ProcessorExecutionState.class, filterText, sortInfo,
                    page, pageSize);
            List<ProcessorExecutionState> executingProcessors = result.getResultList();
            List<ExecutingProcessorsDTO> executingProcessorsDTO = new ArrayList<>();

            if (null == executingProcessors || executingProcessors.isEmpty()) {
                serviceResponse.setResponse(new ResponseDTO(Messages.NO_COMPONENT_EXISTS, EXECUTING_PROCESSORS, Messages.SUCCESS));
                serviceResponse.setProcessors(executingProcessorsDTO);
                return serviceResponse;
            }

            ExecutingProcessorsDTO executingProcessor = null;
            for (ProcessorExecutionState execPrcs : executingProcessors) {
                executingProcessor = new ExecutingProcessorsDTO();
                executingProcessor.copyFromEntity(execPrcs);
                executingProcessorsDTO.add(executingProcessor);
            }

            // response message construction
            serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, Messages.PROCESSORS_LIST.value(), Messages.SUCCESS));
            serviceResponse.setProcessors(executingProcessorsDTO);
            serviceResponse.setTotalItems((int) result.getTotalItems());

            LOG.debug("Exiting from get all executing Processors.");
            return serviceResponse;
        } catch (Exception e) {

            LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
            serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, EXECUTING_PROCESSORS, Messages.FAILURE,
                    e.getMessage()));
            return serviceResponse;
        }
        
    }
    
    /**
     * finds stuck processors and notify it
     */
    public void notifyStuckProcessors() {

        TimeUnit timeUnit = TimeUnit.valueOf(getEnvironmentProperties().getString(MAILBOX_STUCK_PROCESSOR_TIME_UNIT, TimeUnit.HOURS.name()));
        int value = getEnvironmentProperties().getInt(MAILBOX_STUCK_PROCESSOR_TIME_VALUE, 1);

        try {

            ProcessorExecutionStateDAO processorDao = new ProcessorExecutionStateDAOBase();
            List<ProcessorExecutionState> executingProcessors = processorDao.findExecutingProcessors(timeUnit, value);
            if (null == executingProcessors || executingProcessors.isEmpty()) {
                //no processors stuck in the processing state
                return;
            }

            //body construction
            StringBuilder emailBody = new StringBuilder();
            emailBody.append("The following processors are in PROCESSING state more than ");
            emailBody.append(value);
            emailBody.append(" ");
            emailBody.append(timeUnit.name().toLowerCase());
            emailBody.append("\n\n");
            emailBody.append("Processor GUID                 ");
            emailBody.append("----");
            emailBody.append("Last Running Node              ");
            emailBody.append("\n");

            for (ProcessorExecutionState processorState : executingProcessors) {

                emailBody.append(processorState.getProcessorId());
                emailBody.append("----");
                emailBody.append(processorState.getNodeInUse());
                emailBody.append("\n");
            }

            //email config
            EmailInfoDTO emailInfoDTO = new EmailInfoDTO();
            emailInfoDTO.setEmailBody(emailBody.toString());
            emailInfoDTO.setSubject(STUCK_PROCESSORS_IN_RELAY);
            List<String> email = new ArrayList<>();
            email.add(getEnvironmentProperties().getString(MAILBOX_STUCK_PROCESSOR_RECEIVER));
            emailInfoDTO.setToEmailAddrList(email);

            //sends email
            EmailNotifier.sendEmail(emailInfoDTO);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

    }

    /**
     * Method to update the state of executing processor
     *
     * @param processorId processor id
     * @param userId user login id
     */
    public UpdateProcessorExecutionStateResponseDTO updateExecutingProcessor(String processorId, String userId) {

        UpdateProcessorExecutionStateResponseDTO response = new UpdateProcessorExecutionStateResponseDTO();

        try {

            if (StringUtil.isNullOrEmptyAfterTrim(processorId)) {
                throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_ID_NOT_AVAILABLE, Response.Status.BAD_REQUEST);
            }

            this.interruptAndUpdateStatus(processorId, userId, null);
            response.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY,
                    "The processor execution status for processor with id : " + processorId + " is ", Messages.SUCCESS));
            return response;

        } catch (MailBoxConfigurationServicesException e) {

            LOG.error(Messages.REVISE_OPERATION_FAILED.name(), e);
            response.setResponse(new ResponseDTO(Messages.REVISE_OPERATION_FAILED, PROCESSORS, Messages.FAILURE, e
                    .getMessage()));
            return response;
        }
    }

    /**
     * Interrupts the thread and updates the processor status
     *
     * @param updateStatusOnly boolean to denote only update status
     * @param processorId processor id
     * @param userId user login id
     */
    public void interruptAndUpdateStatus(String processorId, String userId, String updateStatusOnly) {

        ProcessorExecutionStateDAOBase processorDao = new ProcessorExecutionStateDAOBase();

        ProcessorExecutionState processorExecutionState = processorDao.findByProcessorId(processorId);
        if (null == processorExecutionState) {
            throw new MailBoxConfigurationServicesException(
                    Messages.PROCESSOR_EXECUTION_STATE_NOT_EXIST,
                    processorId,
                    Response.Status.BAD_REQUEST);
        }

        if (Boolean.parseBoolean(updateStatusOnly)) {
            updateExecutionState(processorId, userId, processorExecutionState);
        } else {
            //post ticket to topic when it is not running in current node
            if (!MailBoxUtil.getNode().equals(processorExecutionState.getNodeInUse())) {
                postToTopic(userId, processorExecutionState);
            } else {
                updateExecutionState(processorId, userId, processorExecutionState);
            }
        }
    }

    /**
     * This method updates the processor status
     * 
     * @param processorId
     * @param userId
     * @param processorExecutionState
     */
    private void updateExecutionState(String processorId, String userId, ProcessorExecutionState processorExecutionState) {
        
        ProcessorExecutionStateDAOBase processorDao = new ProcessorExecutionStateDAOBase();

        String node = processorExecutionState.getNodeInUse();
        String threadName = processorExecutionState.getThreadName();

        
        if (ExecutionState.PROCESSING.value().equals(processorExecutionState.getExecutionStatus())) {

            //fetches the processor by using node and thread name
            //it returns the descending order
            List<ProcessorExecutionState> processorExecutionStates = processorDao.findProcessors(node, threadName);
            if (!processorExecutionStates.isEmpty()) {

                //fetches the first time from the list to verify that is the running processor
                ProcessorExecutionState tempProcessorExecState = processorExecutionStates.get(0);

                //compares the processor id and latest processor ran with that thread name
                //interrupt the thread if it matches
                if (processorId.equals(tempProcessorExecState.getProcessorId())) {
                    MailBoxUtil.interruptThread(processorExecutionState.getThreadName());
                    LOG.info("The thread {} interrupted for processor id {}", threadName, processorId);
                }

                //update the status and user who stopped this processor invocation
                processorExecutionState.setExecutionStatus(ExecutionState.FAILED.value());
                processorExecutionState.setModifiedBy(userId);
                processorDao.updateProcessorExecutionState(processorExecutionState);
                LOG.info("The processor execution status updated for processor id {}", processorId);
            }
        } else {
            throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_EXECUTION_STATE_NOT_PROCESSING,
                    processorId, Response.Status.BAD_REQUEST);
        }
    }

    /**
     * posts topic message
     *
     * @param userId user id
     * @param processorExecutionState processor exec state
     */
    public void postToTopic(String userId, ProcessorExecutionState processorExecutionState) {

        TopicMessageDTO message = new TopicMessageDTO();
        message.setProcessorId(processorExecutionState.getProcessorId());
        message.setNodeInUse(processorExecutionState.getNodeInUse());
        message.setThreadName(processorExecutionState.getThreadName());
        message.setUserId(userId);

        try {
            String msgToTopic = JAXBUtility.marshalToJSON(message);
            MailBoxTopicMessageProducer.getInstance().sendMessage(msgToTopic);
        } catch (JAXBException | IOException | ClientUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Overloaded version of interruptAndUpdateStatus
     * @param messageDTO TopicMessageDTO
     */
    public void interruptAndUpdateStatus(TopicMessageDTO messageDTO) {

        ProcessorExecutionStateDAOBase processorDao = new ProcessorExecutionStateDAOBase();
        ProcessorExecutionState processorExecutionState = processorDao.findByProcessorId(messageDTO.getProcessorId());
        if (null == processorExecutionState) {
            throw new MailBoxConfigurationServicesException(
                    Messages.PROCESSOR_EXECUTION_STATE_NOT_EXIST,
                    messageDTO.getProcessorId(),
                    Response.Status.BAD_REQUEST);
        }
        
        this.updateExecutionState(
                messageDTO.getProcessorId(),
                messageDTO.getUserId(),
                processorExecutionState);
    }
    
    /**
     * Method to update the processor state from "PROCESSING" to "FAILED" on starting the server.
     * 
     */
    public static void updateExecutionStateOnInit() {
        
        ProcessorExecutionStateDAO processorExecutionStateDAO = new ProcessorExecutionStateDAOBase();
        processorExecutionStateDAO.updateStuckProcessorsExecutionState(ConfigurationManager.getDeploymentContext().getDeploymentServerId());
    }
    
    /**
     * Method to update the state of executing processors to failed.
     *
     * @param processorIds
     * @param userId
     * @return response
     */
    public UpdateProcessorsExecutionStateResponseDTO updateExecutingProcessors(List<String> processorIds,
            String userId, String updateStatusOnly) {
        
        UpdateProcessorsExecutionStateResponseDTO response = new UpdateProcessorsExecutionStateResponseDTO();
            try {

                if (null == processorIds || processorIds.isEmpty()) {
                    throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_IDS_NOT_AVAILABLE, Response.Status.BAD_REQUEST);
                }
                for (String processorId : processorIds) {
                    this.interruptAndUpdateStatus(processorId, userId, updateStatusOnly);
                }
                if (Boolean.parseBoolean(updateStatusOnly)) {
                    response.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY,
                        "The processor execution status for processorIds are updated.", Messages.SUCCESS));
                } else {
                    response.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY,
                            "The running processors are interrupted and its processor execution status are updated.", Messages.SUCCESS));
                }
                return response;
            } catch (MailBoxConfigurationServicesException e) {
                
                LOG.error(Messages.REVISE_OPERATION_FAILED.name(), e);
                response.setResponse(new ResponseDTO(Messages.REVISE_OPERATION_FAILED, PROCESSORS, Messages.FAILURE, e.getMessage()));
                return response;
            }
    }
}
