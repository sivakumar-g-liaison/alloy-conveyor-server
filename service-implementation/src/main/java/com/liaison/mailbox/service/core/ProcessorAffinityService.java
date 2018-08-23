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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.rtdm.dao.DatacenterDAOBase;
import com.liaison.mailbox.rtdm.dao.InboundFileDAO;
import com.liaison.mailbox.rtdm.dao.InboundFileDAOBase;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAO;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAOBase;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.affinity.AffinityRetryDTO;
import com.liaison.mailbox.service.dto.configuration.DatacenterDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetDatacenterResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.queue.sender.RunningProcessorRetryQueue;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.liaison.mailbox.MailBoxConstants.TOTAL_PERCENT;


/**
 * Class which has Processor affinity related operations.
 */
public class ProcessorAffinityService implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(ProcessorAffinityService.class);
    private static final String DATACENTER_PROCESSOR = "Datacenetres and Processors";
    private static final List<String> PROCESSOR_TYPES = Arrays.asList(ProcessorType.REMOTEUPLOADER.getCode(),
            ProcessorType.REMOTEDOWNLOADER.getCode(),
            ProcessorType.SWEEPER.getCode(),
            ProcessorType.CONDITIONALSWEEPER.getCode());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ProcessorAffinityService() {

    }
    public ProcessorAffinityService(String message) {
        this.message = message;
    }

    /**
     * This is to support ACON processor affinity.
     * This is to split the traffic for the processor that need to run different datacenters to support
     * active active
     *
     * @param datacenterMap datacenter key and percentage map
     */
    public void supportProcessorProcessorAffinity(Map<String, String> datacenterMap) {

        LOGGER.debug("Enter into supportProcessorProcessorAffinity () ");
        try {

            //retrieve the data center and value is the % of processor that should run on that DC
            if (MailBoxConstants.SECURE.equals(MailBoxUtil.CLUSTER_TYPE)) {
                updateProcessorDatacenter(datacenterMap, MailBoxConstants.SECURE, null);
                updateProcessorDatacenter(datacenterMap, MailBoxConstants.LOWSECURE, null);
            } else {
                updateProcessorDatacenter(datacenterMap, MailBoxConstants.LOWSECURE, null);
            }

        } catch (NumberFormatException exception) {
            throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
        }

        LOGGER.debug("Exit from supportProcessorProcessorAffinity () ");
    }

    @Override
    public void run() {

        if (MailBoxUtil.isEmpty(message)) {
            LOGGER.info("Message is empty");
        } else {
            try {
                AffinityRetryDTO dto = MAPPER.readValue(message, AffinityRetryDTO.class);
                updateProcessorDatacenter(dto.getDatacenterMap(), dto.getClusterType(), dto.getProcessorGuids());
            } catch (Exception e) {
                LOGGER.error("failed to parse the message", e);
            }
        }
    }

    /**
     * Updates the data center by cluster type
     *
     * @param datacenterMap
     * @param clusterType
     */
    private void updateProcessorDatacenter(Map<String, String> datacenterMap, String clusterType, List<String> inprogressGuids) {

        // fetch processor count
        ProcessorConfigurationDAO dao = new ProcessorConfigurationDAOBase();
        ProcessorExecutionStateDAO executionStateDAO = new ProcessorExecutionStateDAOBase();

        List<String> processorGuids = CollectionUtils.isEmpty(inprogressGuids) ? dao.getProcessorGuids(clusterType, PROCESSOR_TYPES) : inprogressGuids;
        int processorCount = processorGuids.size();
        if (0 == processorCount) {
            LOGGER.info("No processor exist for cluster type {}", clusterType);
            return;
        }

        //update datacenter
        datacenterUpdate(datacenterMap);

        List<ProcessorExecutionState> runningProcessors  = executionStateDAO.findExecutingProcessors();
        List<String> runningProcessorsGuid = runningProcessors.stream().map(ProcessorExecutionState::getProcessorId).collect(Collectors.toList());

        //excluded the running processor guids
        boolean retry = processorGuids.removeAll(runningProcessorsGuid);
        processorCount = processorGuids.size();
        if (processorGuids.isEmpty()) {
            LOGGER.info("All the processors are running {}", clusterType);
            postToRetryQueue(datacenterMap, clusterType, runningProcessorsGuid);
            return;
        }

        //Update the processors
        Map<String, List<String>> processorMap = new HashMap<>();
        int startValue = 0;
        for (String dc : datacenterMap.keySet()) {

            if (MailBoxUtil.isEmpty(datacenterMap.get(dc))) {
                throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
            }
            int count = (Integer.parseInt(datacenterMap.get(dc)) * processorCount) / TOTAL_PERCENT;
            processorMap.put(dc, processorGuids.subList(startValue, (startValue + count)));
            startValue = (startValue + count);
        }

        List<String> subList;
        for (String dc : processorMap.keySet()) {
            subList = processorMap.get(dc);
            if (CollectionUtils.isNotEmpty(subList)) {
                dao.updateProcessorDatacenter(dc, subList);
            }
        }

        //STAGED_FILE
        //Update processor dc if it is any switch on the ACON side
        //collect the uploader map which is in-progress
        Map<String, String> uploaderProcessDcMap = dao.getProcessorDetails(clusterType, Collections.singletonList(ProcessorType.REMOTEUPLOADER.getCode()));
        if (CollectionUtils.isNotEmpty(inprogressGuids)) {
            List<String> uploaderGuid = uploaderProcessDcMap.keySet().stream().filter(s -> (processorGuids.stream().anyMatch(s::equals))).collect(Collectors.toList());
            uploaderProcessDcMap = uploaderGuid.stream().filter(uploaderProcessDcMap::containsKey).collect(Collectors.toMap(Function.identity(), uploaderProcessDcMap::get));
        }
        updateStagedFileProcessDc(datacenterMap, clusterType, dao, uploaderProcessDcMap, inprogressGuids);

        //INBOUND_FILE
        //Update process dc if there is any swich on the ACON side
        //collect the sweepers map which is in in-progress
        Map<String, String> sweeperProcessMap = dao.getProcessorDetails(clusterType, Arrays.asList(ProcessorType.SWEEPER.getCode(), ProcessorType.CONDITIONALSWEEPER.getCode()));
        if (CollectionUtils.isNotEmpty(inprogressGuids)) {
            List<String> sweeperGuids = sweeperProcessMap.keySet().stream().filter(s -> (processorGuids.stream().anyMatch(s::equals))).collect(Collectors.toList());
            sweeperProcessMap = sweeperGuids.stream().filter(sweeperProcessMap::containsKey).collect(Collectors.toMap(Function.identity(), sweeperProcessMap::get));
        }
        updateInboundFileProcessDc(datacenterMap, clusterType, dao, sweeperProcessMap, inprogressGuids);

        //post to retry queue
        if (retry) {
            postToRetryQueue(datacenterMap, clusterType, runningProcessorsGuid);
        }
    }

    private void postToRetryQueue(Map<String, String> datacenterMap, String clusterType, List<String> runningProcessorsGuid) {
        try {

            AffinityRetryDTO dto = new AffinityRetryDTO();
            dto.setClusterType(clusterType);
            dto.setProcessorGuids(runningProcessorsGuid);
            dto.setDatacenterMap(datacenterMap);

            //Post to Queue
            RunningProcessorRetryQueue.post(MAPPER.writeValueAsString(dto), 5000);
        } catch (Exception e) {
            LOGGER.error("Unable to retry the in-progress processors {}", String.join(",", runningProcessorsGuid));
            LOGGER.error("Unable to retry the in-progress processors", e);
        }
    }

    /**
     *  Updates the datacenter table which is used by further processing files
     * @param datacenterMap
     */
    private void datacenterUpdate(Map<String, String> datacenterMap) {

        String dcToUpdate = MailBoxUtil.getDcToUpdate(datacenterMap);
        Map<String, String> updateDc = new HashMap<>();
        if (MailBoxUtil.isEmpty(dcToUpdate)) {
            datacenterMap.keySet().forEach(dc -> updateDc.put(dc, dc));
        } else {
            datacenterMap.keySet().stream().filter(dc -> !dcToUpdate.equalsIgnoreCase(dc)).forEach(dc -> updateDc.put(dc, dcToUpdate));
        }
        new DatacenterDAOBase().updateDatacenter(updateDc);
    }

    /**
     * Update Process dc of the staged files based on the percentage
     * @param datacenterMap
     * @param clusterType
     * @param dao
     * @param uploaderProcessDcMap
     */
    private void updateStagedFileProcessDc(Map<String, String> datacenterMap,
                                           String clusterType,
                                           ProcessorConfigurationDAO dao,
                                           Map<String, String> uploaderProcessDcMap,
                                           List<String> inprogressGuids) {

        StagedFileDAO stagedFileDAOBase = new StagedFileDAOBase();
        String dcToUpdate = MailBoxUtil.getDcToUpdate(datacenterMap);
        if (!MailBoxUtil.isEmpty(dcToUpdate)) {
            //TODO exclude the processors
            //TODO include some processor instead of everything on the retry phase
            //stagedFileDAOBase.updateStagedFileProcessDC(dcToUpdate);
            //Update the processor which are not running at the moment
            List<String> pguids = new ArrayList<>(uploaderProcessDcMap.keySet());
            if (!pguids.isEmpty()) {
                stagedFileDAOBase.updateStagedFileProcessDCByProcessorGuid(pguids, dcToUpdate);
            }
        } else {

            //after the default switch
            Map<String, List<String>> processorToUpdate = new HashMap<>();
            Map<String, String> uploaderProcessDcMapDefault = dao.getProcessorDetails(clusterType, Collections.singletonList(ProcessorType.REMOTEUPLOADER.getCode()));
            //Ignore other processors if it is retry
            if (inprogressGuids != null && !inprogressGuids.isEmpty()) {
                List<String> uploaderGuid = uploaderProcessDcMapDefault.keySet().stream().filter(s -> (inprogressGuids.stream().anyMatch(s::equals))).collect(Collectors.toList());
                uploaderProcessDcMapDefault = uploaderGuid.stream().filter(uploaderProcessDcMapDefault::containsKey).collect(Collectors.toMap(Function.identity(), uploaderProcessDcMapDefault::get));
            }

            calculateProcessorToUpdate(uploaderProcessDcMap, processorToUpdate, uploaderProcessDcMapDefault);

            for (String dc : processorToUpdate.keySet()) {
                List<String> pguids = processorToUpdate.get(dc);
                if (pguids != null && !pguids.isEmpty()) {
                    stagedFileDAOBase.updateStagedFileProcessDCByProcessorGuid(pguids, dc);
                }
            }
        }

    }

    /**
     * Update Process dc of the staged files based on the percentage
     * @param datacenterMap
     * @param clusterType
     * @param dao
     * @param uploaderProcessDcMap
     */
    private void updateInboundFileProcessDc(Map<String, String> datacenterMap,
                                           String clusterType,
                                           ProcessorConfigurationDAO dao,
                                           Map<String, String> uploaderProcessDcMap,
                                           List<String> inprogressGuids) {

        InboundFileDAO inboundFileDAOBase = new InboundFileDAOBase();
        String dcToUpdate = MailBoxUtil.getDcToUpdate(datacenterMap);
        if (!MailBoxUtil.isEmpty(dcToUpdate)) {
            //TODO exclude the processors
            //TODO include some processor instead of everything on the retry phase
            //Update the processor which are not running at the moment
            List<String> pguids = new ArrayList<>(uploaderProcessDcMap.keySet());
            if (!pguids.isEmpty()) {
                inboundFileDAOBase.updateInboundFileProcessDCByProcessorGUid(pguids, dcToUpdate);
            }
        } else {

            //after the default switch
            Map<String, List<String>> processorToUpdate = new HashMap<>();
            Map<String, String> sweeperProcessDCMap = dao.getProcessorDetails(clusterType, Arrays.asList(ProcessorType.SWEEPER.getCode(), ProcessorType.CONDITIONALSWEEPER.getCode()));
            //Ignore other processors if it is retry
            if (CollectionUtils.isNotEmpty(inprogressGuids)) {
                List<String> sweeperGuids = sweeperProcessDCMap.keySet().stream().filter(s -> (inprogressGuids.stream().anyMatch(s::equals))).collect(Collectors.toList());
                sweeperProcessDCMap = sweeperGuids.stream().filter(sweeperProcessDCMap::containsKey).collect(Collectors.toMap(Function.identity(), sweeperProcessDCMap::get));
            }

            calculateProcessorToUpdate(uploaderProcessDcMap, processorToUpdate, sweeperProcessDCMap);
            for (String dc : processorToUpdate.keySet()) {
                List<String> pguids = processorToUpdate.get(dc);
                if (pguids != null && !pguids.isEmpty()) {
                    inboundFileDAOBase.updateInboundFileProcessDCByProcessorGUid(pguids, dc);
                }
            }
        }

    }

    private void calculateProcessorToUpdate(Map<String, String> uploaderProcessDcMap,
                                            Map<String, List<String>> processorToUpdate,
                                            Map<String, String> processorLatestMap) {

        for (String key : processorLatestMap.keySet()) {

            String value = processorLatestMap.get(key);
            if (!uploaderProcessDcMap.get(key).equals(value)) {
                List<String> list = processorToUpdate.get(value);
                if (list == null) {
                    List<String> temp = new ArrayList<>();
                    temp.add(key);
                    processorToUpdate.put(value, temp);
                } else {
                    list.add(key);
                    processorToUpdate.put(value, list);
                }
            }

        }
    }

    /**
     * This method is used to update the process_dc to the current_Dc where the process_dc is null
     */
    public void updateProcessDc() {
        new ProcessorConfigurationDAOBase().updateProcessDc();
    }

    /**
     * This method is used to update the downloader process_dc
     */
    public void updateProcessorProcessDc(String existingProcessDC, String newProcessDC) {

        if (MailBoxUtil.isEmpty(existingProcessDC) || MailBoxUtil.isEmpty(newProcessDC)) {
            throw new MailBoxConfigurationServicesException(Messages.PROCESS_DC_EMPTY, Response.Status.BAD_REQUEST);
        }
        new ProcessorConfigurationDAOBase().updateProcessorProcessDc(existingProcessDC, newProcessDC, PROCESSOR_TYPES);
    }

    /**
     * Method fetch the Datacenter's and corresponding processors details.
     *
     * @return GetDatacenterResponseDTO
     */
    public GetDatacenterResponseDTO getDatacenters() {

        LOGGER.debug("Enter into getDatacenters () ");

        GetDatacenterResponseDTO serviceResponse = new GetDatacenterResponseDTO();

        try {

            ProcessorConfigurationDAO dao = new ProcessorConfigurationDAOBase();
            List<ProcessorDTO> prsDTOList = null;
            List<DatacenterDTO> dcDTOList = new ArrayList<>();
            List<Processor> processors = null;
            ProcessorDTO processorDTO = null;
            // fetch the all datacenters
            List<String> datacenters = dao.getAllDatacenters();
            DatacenterDTO dcDTO = null;
            for (String name : datacenters) {

                if (null == name) {
                    continue;
                }
                dcDTO = new DatacenterDTO();
                dcDTO.setName(name);
                // fetch the all Processor's
                processors = dao.findProcessorsByDatacenter(name);
                prsDTOList = new ArrayList<>();

                for (Processor processor : processors) {
                    processorDTO = new ProcessorDTO();
                    processorDTO.copyFromEntity(processor, false);
                    prsDTOList.add(processorDTO);
                }

                dcDTO.setProcessors(prsDTOList);
                dcDTO.setTotalItems(prsDTOList.size());
                dcDTOList.add(dcDTO);
            }

            serviceResponse.setDatacenetrs(dcDTOList);
            serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, DATACENTER_PROCESSOR,
                    Messages.SUCCESS));

            LOGGER.debug("Exit from getDatacenters () ");

        } catch (Exception e) {
            LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
            serviceResponse.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, DATACENTER_PROCESSOR,
                    Messages.FAILURE, e.getMessage()));
        }

        return serviceResponse;
    }
}
