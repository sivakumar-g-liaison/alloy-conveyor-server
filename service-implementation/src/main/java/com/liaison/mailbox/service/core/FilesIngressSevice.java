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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.FolderType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.IngressFilesDTO;
import com.liaison.mailbox.service.dto.configuration.FilesIngressProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ConditionalSweeperPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SweeperPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.response.FileIngressResponceDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;

/**
 * Class which does ingress files related services.
 *
 */
public class FilesIngressSevice {

    private static final Logger LOGGER = LogManager.getLogger(FilesIngressSevice.class);
    private static final String INGRESS_FILES = "Ingress files";

    /**
     * This method will get processors, creates the FileIngressResponceDTO and return the same.
     * 
     * @return
     */
    public FileIngressResponceDTO listIngressFiles() {

        FileIngressResponceDTO fileIngressResponceDTO = new FileIngressResponceDTO();

        try {

            ProcessorConfigurationDAO processorConfigurationDAO = new ProcessorConfigurationDAOBase();
            List<Processor> processors = processorConfigurationDAO.listSweeperProcessors();

            List<FilesIngressProcessorDTO> filesIngressProcessorList = new ArrayList<>();
            FilesIngressProcessorDTO filesIngressProcessorDTO;
            List<IngressFilesDTO> ingressFilesDTO;
            boolean isSweepSubDirectories;
            String payloadUri;

            for (Processor processor : processors) {

                payloadUri = getPayloadUri(processor);
                if (null == payloadUri) {
                    continue;
                }

                ingressFilesDTO = new ArrayList<>();

                filesIngressProcessorDTO = new FilesIngressProcessorDTO();
                filesIngressProcessorDTO.setProcessorGuid(processor.getPguid());
                filesIngressProcessorDTO.setProcessorName(processor.getProcsrName());
                filesIngressProcessorDTO.setStatus(processor.getProcsrStatus());
                filesIngressProcessorDTO.setPayloadUri(payloadUri);

                try {

                    if (ProcessorType.SWEEPER.equals(processor.getProcessorType())) {
                        isSweepSubDirectories = ((SweeperPropertiesDTO) ProcessorPropertyJsonMapper.getProcessorBasedStaticPropsFromJson(
                                processor.getProcsrProperties(), processor)).isSweepSubDirectories();
                    } else {
                        isSweepSubDirectories = ((ConditionalSweeperPropertiesDTO) ProcessorPropertyJsonMapper.getProcessorBasedStaticPropsFromJson(
                                processor.getProcsrProperties(), processor)).isSweepSubDirectories();
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to convert json to processor properties DTO. " + processor.getProcsrProperties());
                    continue;
                }

                listFiles(ingressFilesDTO, Paths.get(payloadUri), isSweepSubDirectories);
                filesIngressProcessorDTO.setFiles(ingressFilesDTO);

                filesIngressProcessorList.add(filesIngressProcessorDTO);

            }

            fileIngressResponceDTO.setProcessors(filesIngressProcessorList);
            fileIngressResponceDTO.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, INGRESS_FILES, Messages.SUCCESS));
            return fileIngressResponceDTO;
        } catch (Exception e) {
            LOGGER.error(Messages.READ_OPERATION_FAILED.name(), e);
            fileIngressResponceDTO.setResponse(new ResponseDTO(Messages.READ_OPERATION_FAILED, INGRESS_FILES,
                    Messages.FAILURE, e.getMessage()));
            return fileIngressResponceDTO;
        }
    }


    /**
     * To return payload uri form processor folder
     * 
     * @param processor
     * @return
     */
    private String getPayloadUri(Processor processor) {

        if (processor.getFolders() != null) {

            for (Folder folder : processor.getFolders()) {

                FolderType foundFolderType = FolderType.findByCode(folder.getFldrType());
                if (null == foundFolderType) {
                    throw new MailBoxServicesException(Messages.FOLDERS_CONFIGURATION_INVALID, Response.Status.CONFLICT);
                } else if (FolderType.PAYLOAD_LOCATION.equals(foundFolderType)) {
                    return folder.getFldrUri();
                }
            }
        }

        return null;
    }


    /**
     * List files from root path
     * 
     * @param ingressFiles
     * @param rootPath
     * @param isSweepSubDirectories
     */
    private void listFiles(List<IngressFilesDTO> ingressFiles, Path rootPath, boolean isSweepSubDirectories) {

        IngressFilesDTO ingressFilesDTO;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath)) {

            for (Path file : stream) {

                if (Files.isDirectory(file)) {
                    if (isSweepSubDirectories) {
                        listFiles(ingressFiles, file, isSweepSubDirectories);
                    }
                    continue;
                }

                ingressFilesDTO = new IngressFilesDTO();
                ingressFilesDTO.setPath(file.toString());
                ingressFilesDTO.setCreatedTime(new Date(Files.readAttributes(file, BasicFileAttributes.class).creationTime().to(TimeUnit.MILLISECONDS)));
                ingressFiles.add(ingressFilesDTO);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to list files", e.getMessage());
        }
    }
}
