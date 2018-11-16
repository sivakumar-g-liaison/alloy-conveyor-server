/**
 * Copyright 2018 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.processor;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.logging.LogTags;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.InboundFileDAO;
import com.liaison.mailbox.rtdm.dao.InboundFileDAOBase;
import com.liaison.mailbox.rtdm.model.InboundFile;
import com.liaison.mailbox.service.dto.GlassMessageDTO;
import com.liaison.mailbox.service.dto.SweeperStaticPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ConditionalSweeperPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.glass.util.ExecutionTimestamp;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.glass.util.MailboxGlassMessageUtil;
import com.liaison.mailbox.service.queue.sender.SweeperQueueSendClient;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * EnhancedConditionalSweeper
 *
 * <P>
 * ConditionalSweeper checks for existence of a trigger file inbound file
 * and only moves files matching trigger criteria.
 *
 */
public class EnhancedConditionalSweeper extends AbstractSweeper implements MailBoxProcessorI {

    private static final Logger LOGGER = LogManager.getLogger(EnhancedConditionalSweeper.class);

    private ConditionalSweeperPropertiesDTO staticProp;
    private String triggerFileName;
    private static final int TRIGGER_FILE = 1;

    public void setStaticProp(ConditionalSweeperPropertiesDTO staticProp) {
        this.staticProp = staticProp;
    }

    public void setTriggerFileName(String triggerFileName) {
        this.triggerFileName = triggerFileName;
    }

    @SuppressWarnings("unused")
    private EnhancedConditionalSweeper() {
        // to force creation of instance only by passing the processor entity
    }

    public EnhancedConditionalSweeper(Processor configurationInstance) {
        super(configurationInstance);
    }

    @Override
    public void runProcessor(Object dto) {
        setReqDTO((TriggerProcessorRequestDTO) dto);
        run();
    }

    private void run() {

        List<WorkTicket> postedWorkTickets;
        List<InboundFile> inboundFiles = null;
        InboundFileDAO inboundFileDAO = new InboundFileDAOBase();

        try {
            String inputLocation = getPayloadURI();
            this.setStaticProp((ConditionalSweeperPropertiesDTO) getProperties());

            if (MailBoxUtil.isEmpty(inputLocation)) {
                throw new MailBoxServicesException(Messages.LOCATION_NOT_CONFIGURED, MailBoxConstants.PAYLOAD_LOCATION, Response.Status.CONFLICT);
            }

            long startTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Start run"));

            InboundFile inprogressInboundFile = inboundFileDAO.findInprogressTriggerFile(payloadLocation, configurationInstance.getPguid());
            postedWorkTickets = new ArrayList<>();
            
            if (null != inprogressInboundFile) {

                setTriggerFileName(StringUtils.removeEnd(inprogressInboundFile.getFileName(), MailBoxConstants.INPROGRESS_EXTENTION));
                inboundFiles = inboundFileDAO.findInboundFilesForInprogressTriggerFile(payloadLocation, inprogressInboundFile.getPguid());

                postToInprogressFilesAsyncSweeper(inboundFiles, postedWorkTickets, inprogressInboundFile);

                inprogressInboundFile.setStatus(EntityStatus.INACTIVE.name());
                inprogressInboundFile.setFileName(triggerFileName);
                inprogressInboundFile.setModifiedBy(getReqDTO().getProfileName());
                inprogressInboundFile.setModifiedDate(MailBoxUtil.getTimestamp());
                inboundFileDAO.merge(inprogressInboundFile);
            }

            String triggerFileName = MailBoxUtil.isEmpty(staticProp.getTriggerFile())
                    ? MailBoxConstants.TRIGGER_FILE_REGEX
                    : staticProp.getTriggerFile();
            setTriggerFileName(triggerFileName);

            InboundFile triggerInboundFile = inboundFileDAO.findInboundFileForTriggerFile(inputLocation, triggerFileName, MailBoxUtil.DATACENTER_NAME, configurationInstance.getPguid());

            if (null != triggerInboundFile) {

                if (staticProp.isSweepSubDirectories()) {
                    inboundFiles = inboundFileDAO.findInboundFilesForConditionalSweeperByRecurse(payloadLocation, configurationInstance.getPguid(), triggerInboundFile.getPguid());
                } else {
                    inboundFiles = inboundFileDAO.findInboundFilesForConditionalSweeper(payloadLocation, configurationInstance.getPguid(), triggerInboundFile.getPguid());
                }

                LOGGER.info(constructMessage("InboundFiles size {}"), inboundFiles.size());
                
                List<InboundFile> inboundFilesToProcess = new ArrayList<>();

                for (InboundFile inboundFile : inboundFiles) {

                    if (!inboundFile.getProcessDc().equals(inboundFile.getOriginatingDc())) {
                        if (StorageUtilities.isPayloadExists(inboundFile.getFs2Uri())) {
                            inboundFilesToProcess.add(inboundFile);
                        }
                    } else {
                        inboundFilesToProcess.add(inboundFile);
                    }
                }

                // Update conditional sweeper entries parent guid;

                setTriggerFileName(triggerFileName);
                postToAsyncSweeper(inboundFilesToProcess, postedWorkTickets, triggerInboundFile);

                triggerInboundFile.setStatus(EntityStatus.INACTIVE.name());
                triggerInboundFile.setFileName(triggerFileName);
                triggerInboundFile.setModifiedBy(getReqDTO().getProfileName());
                triggerInboundFile.setModifiedDate(MailBoxUtil.getTimestamp());
                inboundFileDAO.merge(triggerInboundFile);

                long endTime = System.currentTimeMillis();

                LOGGER.info(constructMessage("Number of files processed {}"), postedWorkTickets.size());
                LOGGER.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
                LOGGER.info(constructMessage("End run"));
            } else {
                LOGGER.info(constructMessage("Trigger file {} isn't available in the root path {}"), triggerFileName, inputLocation);
            }

        } catch (IllegalAccessException | IOException | JAXBException | JSONException e) {
            throw new RuntimeException(e);
        }  finally {
            ThreadContext.clearMap();
        }
    }
    
    /**
     * This method groups the file path, generate work ticket and post the work tickets to sweeper.
     * 
     * @param filePathList
     * @param postedWorkTickets
     * @param triggerInboundFile
     * @throws IOException
     * @throws IllegalAccessException
     * @throws JSONException
     * @throws JAXBException
     */
    private void postToAsyncSweeper(List<InboundFile> filePathList, List<WorkTicket> postedWorkTickets, InboundFile triggerInboundFile)
            throws IllegalAccessException, IOException, JAXBException, JSONException {

        List<WorkTicket> workTicketsToPost = new ArrayList<>();
        List<WorkTicket> workTickets;
        List<List<InboundFile>> groupedFilePathList = groupingFilesForInboundFile(filePathList);
        InboundFileDAO inboundFileDAO = new InboundFileDAOBase();

        if (!CollectionUtils.isEmpty(groupedFilePathList)) {

            String pipeLineID = getPipeLineID();

            for (List<InboundFile> filePaths : groupedFilePathList) {

                workTickets = generateWorkTicketsForInboundFile(filePaths, pipeLineID);
                if (!workTickets.isEmpty()) {
 
                    if (!staticProp.isAllowEmptyFiles()) {

                        for (WorkTicket workTicket : workTickets) {

                            if (isPayloadValid(workTicket)) {
                                workTicketsToPost.add(workTicket);
                            } else {

                                // Interrupt signal for empty files
                                if (MailBoxUtil.isInterrupted(Thread.currentThread().getName())) {
                                    LOGGER.warn(constructMessage("The executor is gracefully interrupted."));
                                    return;
                                }

                                LOGGER.warn(constructMessage("The file {} is empty and empty files not allowed"), workTicket.getFileName());
                                logToLens(workTicket, null, ExecutionState.VALIDATION_ERROR, triggerInboundFile.getPguid(), null);
                                inboundFileDAO.updateInboundFileStatusByGuid(workTicket.getGlobalProcessId(), EntityStatus.INACTIVE.name(), getReqDTO().getProfileName());
                            }
                        }

                    } else {
                        workTicketsToPost.addAll(workTickets);
                    }
                }
            }

            LOGGER.info("There are {} files to process", workTicketsToPost.size());
            
            if (workTicketsToPost.size() > 0) {
                
                inboundFileDAO.updateParentGuidForConditionalSweeper(workTicketsToPost.stream().map(WorkTicket::getGlobalProcessId).collect(Collectors.toList()), triggerInboundFile.getPguid());
                triggerInboundFile.setTriggerFile(TRIGGER_FILE);
                triggerInboundFile.setFileName(triggerFileName + MailBoxConstants.INPROGRESS_EXTENTION);
                inboundFileDAO.merge(triggerInboundFile);
                
                updateAdditionalContextInWorkTickets(workTicketsToPost, triggerInboundFile, 0, workTicketsToPost.size());
                asyncSweeper(workTicketsToPost, triggerInboundFile);
                postedWorkTickets.addAll(workTicketsToPost);
                workTicketsToPost.clear();

                SweeperQueueSendClient.post(constructBatchWorkticket(triggerInboundFile), false);
            }

        } else {
            LOGGER.warn("javascript filter api returned empty results");
        }
    }

    /**
     * This method groups the file path, generate work ticket and post the work tickets to sweeper.
     * 
     * @param filePathList
     * @param postedWorkTickets
     * @throws IOException
     * @throws IllegalAccessException
     * @throws JSONException
     * @throws JAXBException
     */
    private void postToInprogressFilesAsyncSweeper(List<InboundFile> filePathList, List<WorkTicket> postedWorkTickets, InboundFile inboundFile)
            throws IllegalAccessException, IOException, JAXBException, JSONException {

        List<WorkTicket> workTickets;
        List<InboundFile> fileToProcess = filePathList.stream().filter(list -> list.getStatus().equals(EntityStatus.ACTIVE.name())).collect(Collectors.toList());
        int processFileCount = fileToProcess.size();
        int totalFileCount = filePathList.size();
        int currentFileCount = totalFileCount - processFileCount;
        
        
        if (!CollectionUtils.isEmpty(fileToProcess)) {
            
            String pipeLineID = getPipeLineID();
            workTickets = generateWorkTicketsForInboundFile(fileToProcess, pipeLineID);
            updateAdditionalContextInWorkTickets(workTickets, inboundFile, currentFileCount, totalFileCount);
            asyncSweeper(workTickets, inboundFile);
            postedWorkTickets.addAll(workTickets);
            SweeperQueueSendClient.post(constructBatchWorkticket(inboundFile), false);
            
        } else {
            LOGGER.warn("No files found to process in inprogess list");
        }
    }

    /**
     * Async conditional sweeper posts workticket to queue for inbound files
     *
     * @param workTickets worktickets
     * @throws IOException
     * @throws JAXBException
     * @throws JSONException
     */
    private void asyncSweeper(List<WorkTicket> workTickets, InboundFile inboundFile)
            throws IOException, JAXBException, JSONException {

        final Date lensStatusDate = new Date();

        // Read from mailbox property - grouping js location
        InboundFileDAO inboundFileDao = new InboundFileDAOBase();

        // first corner timestamp
        ExecutionTimestamp firstCornerTimeStamp = ExecutionTimestamp.beginTimestamp(GlassMessage.DEFAULT_FIRST_CORNER_NAME);

        for (WorkTicket wrkTicket : workTickets) {

            if (MailBoxUtil.isInterrupted(Thread.currentThread().getName())) {
                LOGGER.warn(constructMessage("The executor is gracefully interrupted."));
                return;
            }

            LOGGER.debug("Persist workticket to spectrum");

            SweeperStaticPropertiesDTO staticPropertiesDTO = new SweeperStaticPropertiesDTO();
            staticPropertiesDTO.setContentType(staticProp.getContentType());
            staticPropertiesDTO.setLensVisibility(staticProp.isLensVisibility());
            staticPropertiesDTO.setPipeLineID(staticProp.getPipeLineID());
            staticPropertiesDTO.setSecuredPayload(staticProp.isSecuredPayload());

            persistWorkticket(wrkTicket, staticPropertiesDTO);

            String workTicketToSb = JAXBUtility.marshalToJSON(wrkTicket);
            LOGGER.debug(constructMessage("Workticket posted to SB queue.{}"), new JSONObject(workTicketToSb).toString(2));
            SweeperQueueSendClient.post(workTicketToSb, false);

            try {
                ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, wrkTicket.getGlobalProcessId());

                logToLens(wrkTicket, firstCornerTimeStamp, ExecutionState.PROCESSING, inboundFile.getPguid(), lensStatusDate);
                LOGGER.info(constructMessage("Global PID",
                        seperator,
                        wrkTicket.getGlobalProcessId(),
                        " submitted for file ",
                        wrkTicket.getFileName()));

                inboundFileDao.updateInboundFileStatusByGuid(wrkTicket.getGlobalProcessId(), EntityStatus.INACTIVE.name(), getReqDTO().getProfileName());
            } finally {
                ThreadContext.clearMap();
            }

            logToLens(inboundFile.getPguid(), staticProp.getPipeLineID());
        }
    }

    /**
     * Method to construct batch work ticket.
     * 
     * @param inboundFile
     * @return
     * @throws IllegalAccessException
     * @throws IOException
     * @throws JAXBException
     */
    private String constructBatchWorkticket(InboundFile inboundFile) throws IllegalAccessException, IOException, JAXBException {

        WorkTicket workTicket = constructWorkTicket(inboundFile.getParentGlobalProcessId(),  inboundFile.getFs2Uri());
        return JAXBUtility.marshalToJSON(workTicket);
    }
    
    /**
     * Grouping the files based on the payload threshold and no of files threshold.
     *
     * @param workTickets Group of all workTickets in a WorkTicketGroup.
     */
    private void updateAdditionalContextInWorkTickets(List<WorkTicket> workTickets, InboundFile inboundFile, int currentFileCount, int totalFileCount) {

        if (workTickets.isEmpty()) {
            LOGGER.info(constructMessage("There are no files available in the directory."));
        }

        sortWorkTicket(workTickets, staticProp.getSort());
        for (WorkTicket workTicket : workTickets) {

            currentFileCount++;

            Map<String, Object> additionalContext = workTicket.getAdditionalContext();
            additionalContext.put(MailBoxConstants.KEY_FILE_COUNT, currentFileCount + MailBoxConstants.FILE_COUNT_SEPARATOR + totalFileCount);
            additionalContext.put(MailBoxConstants.KEY_TRIGGER_FILE_NAME, triggerFileName);
            additionalContext.put(MailBoxConstants.KEY_TRIGGER_FILE_PARENT_GPID, inboundFile.getPguid());
            additionalContext.put(MailBoxConstants.KEY_FILE_GROUP, true);
            additionalContext.put(MailBoxConstants.KEY_TRIGGER_FILE_URI, inboundFile.getFs2Uri());
            workTicket.setAdditionalContext(additionalContext);
        }

    }

    @Override
    public Object getClient() {
        return null;
    }

    @Override
    public void cleanup() {
    }

    @Override
    public boolean isClassicSweeper() {
        return false;
    }
}
