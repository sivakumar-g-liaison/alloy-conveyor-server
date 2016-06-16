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
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.processor.helper.ClientFactory;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Http remote uploader to perform push operation, also it has support methods
 * for JavaScript.
 *
 * @author veerasamyn
 */
public class HTTPRemoteUploader extends AbstractRemoteUploader {

    private static final Logger LOGGER = LogManager.getLogger(HTTPRemoteUploader.class);
    private boolean executionStatus = false;

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
    public void executeRequest(String executionId, MailboxFSM fsm) {

        String constantInterval = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_INTERRUPT_SIGNAL_FREQUENCY_IN_SEC);

        try {

            HTTPUploaderPropertiesDTO httpUploaderStaticProperties = (HTTPUploaderPropertiesDTO) getProperties();

            // retrieve required properties
            String httpVerb = httpUploaderStaticProperties.getHttpVerb();

            LOGGER.info(constructMessage("Start run"));
            long startTime = System.currentTimeMillis();

            if (MailBoxConstants.POST.equals(httpVerb) || MailBoxConstants.PUT.equals(httpVerb)) {

                //Checks the local path has files to upload
                String path = validateLocalPath();
                File localDir = new File(path);
                //TODO recurseSubDirs
                File[] files = getFilesToUpload(false);
                if (files == null || files.length == 0) {
                    LOGGER.info(constructMessage("The given payload location {} doesn't have files to upload."), path);
                    return;
                }

                Date lastCheckTime = new Date();
                for (File file : files) {

                    // interrupt signal check has to be done only if execution Id is present
                    if (!StringUtil.isNullOrEmptyAfterTrim(executionId)
                            && ((new Date().getTime() - lastCheckTime.getTime()) / 1000) > Long.parseLong(constantInterval)) {
                        if (isThereAnInterruptSignal(executionId, fsm)) {
                            return;
                        }
                        lastCheckTime = new Date();
                    }

                    uploadFile(file);

                }

                if (executionStatus) {
                    throw new MailBoxServicesException(Messages.HTTP_REQUEST_FAILED, Response.Status.BAD_REQUEST);
                }

            }

            // to calculate the elapsed time for processing files
            long endTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Number of files processed {}"), totalNumberOfProcessedFiles);
            LOGGER.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
            LOGGER.info(constructMessage("End run"));
        } catch (IOException | LiaisonException | IllegalAccessException e) {
            LOGGER.error(constructMessage("Error occurred during http(s) upload", seperator, e.getMessage()), e);
            throw new RuntimeException(e);
        }

    }

    private void uploadFile(File file) throws IOException, IllegalAccessException, LiaisonException {

        HTTPRequest request = null;
        HTTPResponse response = null;

        // retrieve required properties
        HTTPUploaderPropertiesDTO httpUploaderStaticProperties = (HTTPUploaderPropertiesDTO) getProperties();
        String contentType = httpUploaderStaticProperties.getContentType();

        try (InputStream contentStream = FileUtils.openInputStream(file);
             ByteArrayOutputStream responseStream = new ByteArrayOutputStream(4096)) {

            LOGGER.info(constructMessage("uploading file {}"), file.getName());

            request = (HTTPRequest) getClient();
            request.setOutputStream(responseStream);
            request.inputData(contentStream, contentType);

            response = request.execute();
            LOGGER.debug(constructMessage("The response code received is {} for a request {} "),
                    response.getStatusCode(),
                    file.getName());
            if (!MailBoxUtil.isSuccessCode(response.getStatusCode())) {

                LOGGER.warn(constructMessage("The response code received is {} "), response.getStatusCode());
                LOGGER.warn(constructMessage("Execution failure for "), file.getAbsolutePath());

                executionStatus = true;
                String msg = "Failed to upload a file " + file.getName();
                logToLens(msg, file, ExecutionState.FAILED);

            } else {

                deleteFile(file);
                StringBuilder msg = new StringBuilder()
                        .append("File ")
                        .append(file.getName())
                        .append(" uploaded successfully");
                logToLens(msg.toString(), file, ExecutionState.COMPLETED);
                totalNumberOfProcessedFiles++;
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



}