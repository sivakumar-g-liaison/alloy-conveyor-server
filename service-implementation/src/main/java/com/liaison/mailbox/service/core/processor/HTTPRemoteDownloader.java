/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.processor;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Folder;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.FolderType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.processor.helper.ClientFactory;
import com.liaison.mailbox.service.dto.configuration.SweeperEventRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.queue.sender.SweeperEventSendQueue;
import com.liaison.mailbox.service.util.DirectoryCreationUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.liaison.mailbox.MailBoxConstants.BYTE_ARRAY_INITIAL_SIZE;

/**
 * Http remote downloader to perform pull operation, also it has support methods
 * for JavaScript.
 *
 * @author OFS
 */
public class HTTPRemoteDownloader extends AbstractProcessor implements MailBoxProcessorI {

    private static final Logger LOGGER = LogManager.getLogger(HTTPRemoteDownloader.class);

    private HTTPDownloaderPropertiesDTO httpDownloaderStaticProperties;

    @SuppressWarnings("unused")
    private HTTPRemoteDownloader() {
        // to force creation of instance only by passing the processor entity
    }

    public HTTPRemoteDownloader(Processor processor) {
        super(processor);
        try {
        	httpDownloaderStaticProperties = (HTTPDownloaderPropertiesDTO) getProperties();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
        // this.configurationInstance = processor;
    }

    @Override
    public void runProcessor(Object dto) {

        LOGGER.debug("Entering in invoke.");

        try {
            // HTTPRequest executed through JavaScript
            if (getProperties().isHandOverExecutionToJavaScript()) {
                // Use custom G2JavascriptEngine
                setMaxExecutionTimeout(((HTTPDownloaderPropertiesDTO) getProperties()).getScriptExecutionTimeout());
                JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);

            } else {
                // HTTPRequest executed through Java
                executeRequest();
            }

        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Java method to execute the HTTPRequest and write in FS location
     *
     */
    protected void executeRequest() {

        HTTPRequest request = (HTTPRequest) getClient();
        ByteArrayOutputStream responseStream = new ByteArrayOutputStream(BYTE_ARRAY_INITIAL_SIZE);
        request.setOutputStream(responseStream);

        HTTPResponse response = null;
        boolean failedStatus = false;
        long startTime = 0;
        // Set the payload value to http client input data for POST & PUT
        // request
        File[] files = null;
        try {

            LOGGER.info(constructMessage("Start run"));
            startTime = System.currentTimeMillis();
            if (MailBoxConstants.POST.equals(request.getMethod())
                    || MailBoxConstants.PUT.equals(request.getMethod())) {

                httpDownloaderStaticProperties = (HTTPDownloaderPropertiesDTO) getProperties();

                files = getFilesToUpload(false);
                if (null != files) {

                    for (File entry : files) {

                        try (InputStream contentStream = FileUtils.openInputStream(entry)) {

                            String contentType = httpDownloaderStaticProperties.getContentType();
                            request.inputData(contentStream, contentType);

                            response = request.execute();
                            LOGGER.debug("The response code received is {} for a request {} ", response.getStatusCode(),
                                    entry.getName());
                            if (response.getStatusCode() != 200) {

                                LOGGER.info("The response code received is {} ", response.getStatusCode());
                                LOGGER.info("Execution failure for ", entry.getAbsolutePath());
                                failedStatus = true;
                            } else {
                                deleteFile(entry);
                                totalNumberOfProcessedFiles++;
                            }
                        }
                    }
                    if (failedStatus) {
                        throw new MailBoxServicesException(Messages.HTTP_REQUEST_FAILED, Response.Status.BAD_REQUEST);
                    }
                }
                else {
                    LOGGER.info(constructMessage("The given HTTP downloader payload URI is Empty."));
                }
            } else {
                response = request.execute();
                writeResponseToMailBox(responseStream);
                totalNumberOfProcessedFiles++;
            }
            // to calculate the elapsed time for processing files
            long endTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Number of files processed {}"), totalNumberOfProcessedFiles);
            LOGGER.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
            LOGGER.info(constructMessage("End run"));

        } catch (MailBoxServicesException | IOException | LiaisonException |
                IllegalAccessException e) {
            LOGGER.error(constructMessage("Error occurred during http(s) download", seperator, e.getMessage()), e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public Object getClient() {
        return ClientFactory.getClient(this);
    }

    @Override
    public void cleanup() {
    }

    /**
     * This Method create local folders if not available and returns the path.
     *
     * * @param processorDTO it have details of processor
     */
    @Override
    public String createLocalPath() {

        String configuredPath = null;
        try {
            configuredPath = getWriteResponseURI();
            DirectoryCreationUtil.createPathIfNotAvailable(configuredPath);
            return configuredPath;

        } catch (IOException e) {
            throw new MailBoxConfigurationServicesException(Messages.LOCAL_FOLDERS_CREATION_FAILED, configuredPath,
                    Response.Status.BAD_REQUEST, e.getMessage());
        }

    }

    @Override
    public File[] getFilesToUpload(boolean recurseSubDirs) throws MailBoxServicesException {

        File[] files = null;

        if (configurationInstance.getFolders() != null) {

            for (Folder folder : configurationInstance.getFolders()) {

                FolderType foundFolderType = FolderType.findByCode(folder.getFldrType());

                if (null == foundFolderType) {
                    throw new MailBoxServicesException(Messages.FOLDERS_CONFIGURATION_INVALID, Response.Status.CONFLICT);
                } else if (FolderType.PAYLOAD_LOCATION.equals(foundFolderType)) {

                    LOGGER.debug("Started reading the payload files");
                    List<File> result = new ArrayList<>();
                    fetchFiles(replaceTokensInFolderPath(folder.getFldrUri()), result);

                    if (!result.isEmpty()) {
                        files = Arrays.copyOf(result.toArray(), result.toArray().length, File[].class);
                        LOGGER.debug("Completed reading the payload files");
                    }
                }
            }
        }
        return files;
    }

    /**
     * Method to list file from folder
     *
     * @throws MailBoxServicesException
     */
    private void fetchFiles(String path, List<File> files) throws MailBoxServicesException {

        if (MailBoxUtil.isEmpty(path)) {
            LOGGER.error("The given URI {} does not exist.", path);
            throw new MailBoxServicesException("The given URI '" + path + "' does not exist.", Response.Status.CONFLICT);
        }

        // Modified to support both file and directory.
        File location = new File(path);
        if (location.isFile()) {

            if (location.exists()) {
                files.add(location);
            } else {
                LOGGER.error("The given file {} does not exist.", path);
                throw new MailBoxServicesException("The given file '" + path + "' does not exist.", Response.Status.CONFLICT);
            }

        } else {

            if (!location.exists()) {
                LOGGER.error("The given directory {} does not exist.", path);
                throw new MailBoxServicesException("The given directory '" + path + "' does not exist.", Response.Status.CONFLICT);
            } else {

                // get all the files from a directory
                for (File file : location.listFiles()) {

                    if (file.isFile()) {
                        if (!MailBoxConstants.META_FILE_NAME.equals(file.getName())) {
                            files.add(file);
                        }
                    } else if (file.isDirectory() && !MailBoxConstants.PROCESSED_FOLDER.equals(file.getName())
                            && !MailBoxConstants.ERROR_FOLDER.equals(file.getName())) {
                        // recursively get all files from sub directory.
                        fetchFiles(file.getAbsolutePath(), files);
                    }
                }
            }
        }
    }

    /**
     * Method to use single file post into Sweeper event queue for store file into BOSS
     * 
     * @param file downloaded File
     * @return Globalprocessid.
     */
    @Override
    public String sweepFile(File file) {

        SweeperEventRequestDTO sweeperEventRequestDTO = new SweeperEventRequestDTO(file);
        sweeperEventRequestDTO.setLensVisibility(httpDownloaderStaticProperties.isLensVisibility());
        sweeperEventRequestDTO.setPipeLineID(httpDownloaderStaticProperties.getPipeLineID());
        sweeperEventRequestDTO.setSecuredPayload(httpDownloaderStaticProperties.isSecuredPayload());
        sweeperEventRequestDTO.setContentType(httpDownloaderStaticProperties.getContentType());
        sweeperEventRequestDTO.setMailBoxId(configurationInstance.getMailbox().getPguid());
        sweeperEventRequestDTO.setTtlMap(configurationInstance.getTTLUnitAndTTLNumber());
        sweeperEventRequestDTO.setGlobalProcessId(MailBoxUtil.getGUID());
        sweeperEventRequestDTO.setStorageType(MailBoxUtil.getStorageType(configurationInstance.getDynamicProperties()));

        try {
            String message = JAXBUtility.marshalToJSON(sweeperEventRequestDTO);
            SweeperEventSendQueue.post(message);
        } catch (Throwable e) {
            String msg = "Failed to sweeper the file "+ file.getName() + " and the error message is " + e.getMessage();
            LOGGER.error(msg, e);
        }

        return sweeperEventRequestDTO.getGlobalProcessId();
    }

    /**
     * Method to use list of downloaded files post into sweeper event queue
     * 
     * @param files  downloaded files
     * @param sweeperStaticPropertiesDTO   
     */
    @Override
    public String[] sweepFiles(File[] files) {

        List<String> globalProcessorIds = new ArrayList<String>();
        for (File file:files) {
            // sweep each file and add global processorIds.
            globalProcessorIds.add(sweepFile(file));
        }
        return globalProcessorIds.toArray(new String[globalProcessorIds.size()]);
    }
}