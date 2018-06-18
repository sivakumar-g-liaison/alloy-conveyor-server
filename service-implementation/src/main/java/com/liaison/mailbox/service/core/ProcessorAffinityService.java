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

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.DatacenterDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetDatacenterResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
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

import static com.liaison.mailbox.MailBoxConstants.TOTAL_PERCENT;


/**
 * Class which has Processor affinity related operations.
 */
public class ProcessorAffinityService {

    private static final Logger LOGGER = LogManager.getLogger(ProcessorAffinityService.class);
    private static final String DATACENTER_PROCESSOR = "Datacenetres and Processors";

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

            //retrieve the datacenetre and value is the % of downloader processor that should run on that DC 
            if (MailBoxConstants.SECURE.equals(MailBoxUtil.CLUSTER_TYPE)) {
                updateProcessorDatacenter(datacenterMap, MailBoxConstants.SECURE);
                updateProcessorDatacenter(datacenterMap, MailBoxConstants.LOWSECURE);
            } else {
                updateProcessorDatacenter(datacenterMap, MailBoxConstants.LOWSECURE);
            }

        } catch (NumberFormatException exception) {
            throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
        }

        LOGGER.debug("Exit from supportProcessorProcessorAffinity () ");
    }

    /**
     * Updates the data center by cluster type
     *
     * @param datacenterMap
     * @param clusterType
     */
    private void updateProcessorDatacenter(Map<String, String> datacenterMap, String clusterType) {

        // fetch processor count
        ProcessorConfigurationDAO dao = new ProcessorConfigurationDAOBase();
        List<String> processorTypes = Arrays.asList(ProcessorType.REMOTEUPLOADER.getCode(),
                ProcessorType.REMOTEDOWNLOADER.getCode(),
                ProcessorType.SWEEPER.getCode(),
                ProcessorType.CONDITIONALSWEEPER.getCode());

        List<String> processorGuids = dao.getProcessorGuids(clusterType, processorTypes);
        int processorCount = processorGuids.size();
        if (0 == processorCount) {
            LOGGER.info("No processor exist for cluster type " + clusterType);
            return;
        }

        //Update the processors
        Map<String, String> uploaderProcessDcMap = dao.getProcessorDetails(clusterType, Collections.singletonList(ProcessorType.REMOTEUPLOADER.getCode()));
        Map<String, List<String>> countMap = new HashMap<>();
        int startValue = 0;
        for (String dc : datacenterMap.keySet()) {

            if (MailBoxUtil.isEmpty(datacenterMap.get(dc))) {
                throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
            }
            int count = (int) Math.ceil((Integer.parseInt(datacenterMap.get(dc)) * processorCount) / TOTAL_PERCENT);
            countMap.put(dc, processorGuids.subList(startValue, (startValue + count)));
            startValue = (startValue + count);
        }

        List<String> subList;
        for (String dc : countMap.keySet()) {
            subList = countMap.get(dc);
            if (CollectionUtils.isNotEmpty(subList)) {
                dao.updateProcessorDatacenter(dc, subList);
            }
        }

        //STAGED_FILE
        //Update processor dc if it is any switch on the ACON side
        updateStagedFileProcessDc(datacenterMap, clusterType, dao, uploaderProcessDcMap);
    }

    /**
     * Update Process dc of the staged files based on the percentage
     * @param datacenterMap
     * @param clusterType
     * @param dao
     * @param uploaderProcessDcMap
     */
    private void updateStagedFileProcessDc(Map<String, String> datacenterMap, String clusterType, ProcessorConfigurationDAO dao, Map<String, String> uploaderProcessDcMap) {

        String dcToUpdate = MailBoxUtil.getDcToUpdate(datacenterMap);
        if (MailBoxUtil.isEmpty(dcToUpdate)) {

            //after the default switch
            Map<String, List<String>> processorToUpdate = new HashMap<>();
            Map<String, String> uploaderProcessDcMapDefault = dao.getProcessorDetails(clusterType, Collections.singletonList(ProcessorType.REMOTEUPLOADER.getCode()));
            for (String key : uploaderProcessDcMapDefault.keySet()) {

                String value = uploaderProcessDcMapDefault.get(key);
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

            for (String dc : processorToUpdate.keySet()) {
                if (processorToUpdate.get(dc) != null) {
                    new StagedFileDAOBase().updateStagedFileProcessDCByProcessorGuid(processorToUpdate.get(dc), dc);
                }
            }

        } else {
            new StagedFileDAOBase().updateStagedFileProcessDC(dcToUpdate);
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
    public void updateDownloaderProcessDc(String existingProcessDC, String newProcessDC) {

        if (MailBoxUtil.isEmpty(existingProcessDC) || MailBoxUtil.isEmpty(newProcessDC)) {
            throw new MailBoxConfigurationServicesException(Messages.PROCESS_DC_EMPTY, Response.Status.BAD_REQUEST);
        }
        new ProcessorConfigurationDAOBase().updateDownloaderProcessDc(existingProcessDC, newProcessDC);
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
