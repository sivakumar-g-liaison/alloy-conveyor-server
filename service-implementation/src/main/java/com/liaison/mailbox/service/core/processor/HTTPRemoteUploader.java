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
import com.liaison.commons.logging.LogTags;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.core.processor.helper.ClientFactory;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.dto.remote.uploader.RelayFile;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.liaison.mailbox.MailBoxConstants.BYTE_ARRAY_INITIAL_SIZE;

/**
 * Http remote uploader to perform push operation, also it has support methods
 * for JavaScript.
 *
 * @author veerasamyn
 */
public class HTTPRemoteUploader extends AbstractRemoteUploader {

    private static final Logger LOGGER = LogManager.getLogger(HTTPRemoteUploader.class);

    @SuppressWarnings("unused")
    private HTTPRemoteUploader() {
        // to force creation of instance only by passing the processor entity
    }

    public HTTPRemoteUploader(Processor configurationInstance) {
        super(configurationInstance);
    }

    /**
     * Java method to execute the HTTPRequest and write in FS location
     */
    public void executeRequest() {


        try {

            HTTPUploaderPropertiesDTO httpUploaderStaticProperties = (HTTPUploaderPropertiesDTO) getProperties();

            // retrieve required properties
            String httpVerb = httpUploaderStaticProperties.getHttpVerb();

            LOGGER.info(constructMessage("Start run"));
            long startTime = System.currentTimeMillis();

            if (MailBoxConstants.POST.equals(httpVerb) || MailBoxConstants.PUT.equals(httpVerb)) {

                //Checks the local path has files to upload
                String path = validateLocalPath();
                boolean recursiveSubdirectories = httpUploaderStaticProperties.isRecurseSubDirectories();
                setDirectUpload(httpUploaderStaticProperties.isDirectUpload());

                Object[] files = (this.canUseFileSystem())
                        ? getFilesToUpload(recursiveSubdirectories)
                        : getRelayFiles(recursiveSubdirectories);
                if (files == null || files.length == 0) {
                    LOGGER.info(constructMessage("The given payload location {} doesn't have files to upload."), path);
                    return;
                }

                for (Object file : files) {

                    if (file instanceof File) {
                        uploadFile((File) file);
                    } else {
                        try {
                            ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, ((RelayFile) file).getGlobalProcessId());
                            uploadFile((RelayFile) file);
                        } finally {
                            ThreadContext.clearMap();
                        }
                    }
                }

            }

            // to calculate the elapsed time for processing files
            long endTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Number of files processed {}"), totalNumberOfProcessedFiles);
            LOGGER.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
            LOGGER.info(constructMessage("End run"));
        } catch (IOException | LiaisonException | IllegalAccessException e) {
            throw new RuntimeException(constructMessage("Error occurred during http(s) upload", seperator, e.getMessage()), e);
        }

    }

    /**
     * Uploads a payload which reads from disk
     *
     * @param file
     * @throws IOException
     * @throws IllegalAccessException
     * @throws LiaisonException
     */
    private void uploadFile(File file) throws IOException, IllegalAccessException, LiaisonException {

        HTTPRequest request;
        HTTPResponse response;

        // retrieve required properties
        HTTPUploaderPropertiesDTO httpUploaderStaticProperties = (HTTPUploaderPropertiesDTO) getProperties();
        String contentType = httpUploaderStaticProperties.getContentType();

        try (InputStream contentStream = FileUtils.openInputStream(file);
             ByteArrayOutputStream responseStream = new ByteArrayOutputStream(BYTE_ARRAY_INITIAL_SIZE)) {

            LOGGER.info(constructMessage("uploading file {}"), file.getName());

            request = (HTTPRequest) getClient();
            request.setOutputStream(responseStream);
            request.inputData(contentStream, contentType);

            response = request.execute();
            LOGGER.debug(constructMessage("The response code received is {} for a request {} "),
                    response.getStatusCode(),
                    file.getName());
            if (!MailBoxUtil.isSuccessful(response.getStatusCode())) {

                LOGGER.warn(constructMessage("The response code received is {} "), response.getStatusCode());
                LOGGER.warn(constructMessage("Execution failure for "), file.getAbsolutePath());
                String msg = String.format("Failed to upload a file %s  status code received %s and the reason is %s", file.getName(), response.getStatusCode(), response.getReasonPhrease());
                logToLens(msg, file, ExecutionState.FAILED);
                throw new RuntimeException(msg);

            } else {

                deleteFile(file);
                String msg = "File " +
                        file.getName() +
                        " uploaded successfully";
                logToLens(msg, file, ExecutionState.COMPLETED);
                totalNumberOfProcessedFiles++;
            }
        }

    }

    /**
     * Uploads a payload which reads from storage system
     *
     * @param file RelayFile
     * @throws IOException
     * @throws IllegalAccessException
     * @throws LiaisonException
     */
    private void uploadFile(RelayFile file) throws IOException, IllegalAccessException, LiaisonException {

        HTTPRequest request = null;
        HTTPResponse response = null;

        // retrieve required properties
        HTTPUploaderPropertiesDTO httpUploaderStaticProperties = (HTTPUploaderPropertiesDTO) getProperties();
        String contentType = httpUploaderStaticProperties.getContentType();

        InputStream contentStream = null;
        ByteArrayOutputStream responseStream = null;
        try {

            contentStream = file.getPayloadInputStream();
            responseStream = new ByteArrayOutputStream(BYTE_ARRAY_INITIAL_SIZE);
            LOGGER.info(constructMessage("uploading file from fs2 uri {} and file name {}"), file.getPayloadUri(), file.getName());

            request = (HTTPRequest) getClient();
            request.setOutputStream(responseStream);
            request.inputData(contentStream, contentType);

            response = request.execute();
            LOGGER.debug(constructMessage("The response code received is {} for a request {} "),
                    response.getStatusCode(),
                    file.getName());
            if (!MailBoxUtil.isSuccessful(response.getStatusCode())) {

                LOGGER.warn(constructMessage("The response code received is {} "), response.getStatusCode());
                LOGGER.warn(constructMessage("Execution failure for "), file.getAbsolutePath());
                String msg = String.format("Failed to upload a file %s  status code received %s and the reason is %s", file.getName(), response.getStatusCode(), response.getReasonPhrease());
                logToLens(msg, file, ExecutionState.FAILED);
                throw new RuntimeException(msg);

            } else {

                // deletes the file if it is staged using file system
                file.delete();
                String msg = "File " +
                        file.getName() +
                        " uploaded successfully";
                logToLens(msg, file, ExecutionState.COMPLETED);
                totalNumberOfProcessedFiles++;
            }
        } finally {
            if (contentStream != null) {
                contentStream.close();
            }
            if (responseStream != null) {
                responseStream.close();
            }
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
     * This Method create local folders if not available.
     *
     * * @param processorDTO it have details of processor
     *
     */
    @Override
    public void createLocalPath() {

        String configuredPath = null;
        try {
            configuredPath = getPayloadURI();
            createPathIfNotAvailable(configuredPath);

        } catch (IOException e) {
            throw new MailBoxConfigurationServicesException(Messages.LOCAL_FOLDERS_CREATION_FAILED, configuredPath,
                    Response.Status.BAD_REQUEST, e.getMessage());
        }

    }

    @Override
    public void doDirectUpload(String fileName, String folderPath, String globalProcessId) {

        try {

            boolean isHandOverExecutionToJavaScript;
            int scriptExecutionTimeout;
            try {
                isHandOverExecutionToJavaScript = getProperties().isHandOverExecutionToJavaScript();
                scriptExecutionTimeout = ((HTTPUploaderPropertiesDTO) getProperties()).getScriptExecutionTimeout();
            } catch (IllegalArgumentException | IllegalAccessException | IOException e) {
                throw new RuntimeException(e);
            }

            //Settings required for javascript file direct upload
            if (isHandOverExecutionToJavaScript) {

                setFileName(fileName);
                setFolderPath(folderPath);
                setGlobalProcessId(globalProcessId);
                setMaxExecutionTimeout(scriptExecutionTimeout);
                JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);
            } else {

                LOGGER.info(constructMessage("Ready to upload file {} from local path {} to remote "),
                        fileName,
                        folderPath);

                if (this.canUseFileSystem()) {
                    uploadFile(new File(folderPath + File.separatorChar + fileName));
                } else {

                    StagedFileDAO dao = new StagedFileDAOBase();
                    StagedFile stagedFile = dao.findStagedFileByGpid(globalProcessId);

                    RelayFile file = new RelayFile();
                    file.copy(stagedFile);
                    uploadFile(file);
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected int getScriptExecutionTimeout() throws IOException, IllegalAccessException {
        return ((HTTPUploaderPropertiesDTO) getProperties()).getScriptExecutionTimeout();
    }

}