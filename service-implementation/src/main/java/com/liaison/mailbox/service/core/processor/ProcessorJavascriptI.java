/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.processor;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.service.dto.SweeperStaticPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.StaticProcessorPropertiesDTO;
import com.liaison.mailbox.service.dto.remote.uploader.RelayFile;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;

import javax.xml.bind.JAXBException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

/**
 * Interface exposes API to run the processors via javascript.
 *
 * @author OFS
 */
public interface ProcessorJavascriptI extends MailBoxProcessorI {

    /**
     * This will return a HTTP, FTP, and HTTPS or FTPS client based on the processor type.
     *
     * @return
     * @throws Exception
     */
    Object getClient();


    /**
     * Returns the processor properties as JSON
     *
     * @return
     */
    String getPropertiesJson();

    /**
     * Returns array of files from the configured payload location. It reads from the local directory.
     *
     * @return
     */
    File[] getFilesToUpload(boolean recurseSubDirs);

    /**
     * Returns array of files from the configured payload location. It reads from the db and returns virtual file.
     *
     * @return
     */
    RelayFile[] getRelayFiles(boolean recurseSubDirs);
    
    /**
     * Returns the trigger file from the configured payload location.
     * 
     * @return
     */
    File getTriggerFile(String triggerFileName);

    /**
     * Returns the relay trigger file from the configured payload location.
     * 
     * @return
     */
    RelayFile getRelayTriggerFile(String triggerFileName);
    
    /**
     * Delete the trigger file from the configured payload location.
     * 
     * @return
     */
    void deleteTriggerFile(File triggerFile);

    /**
     * Delete the relay trigger file from the configured payload location.
     * 
     * @return
     */
    void deleteRelayTriggerFile(RelayFile relayFile);

    /**
     * Returns true for sweepers and false for enhanced sweepers
     * 
     * @return
     */
    boolean isClassicSweeper();

    /**
     * Returns the location to write the payload
     *
     * @return
     * @throws IOException
     * @throws MailBoxServicesException
     */
    String getWriteResponseURI() throws MailBoxServicesException, IOException;

    /**
     * Writes the response stream to the file.
     * The file name format would be "PROCESSOR_NAME + System.nanoTime()"
     *
     * @param response
     * @throws MailBoxServicesException
     * @throws FS2Exception
     * @throws IOException
     * @throws URISyntaxException
     */
    void writeResponseToMailBox(ByteArrayOutputStream response) throws URISyntaxException, IOException, FS2Exception, MailBoxServicesException;

    /**
     * Writes the response stream to the file using the given file name.
     *
     * @param response
     * @param filename
     * @throws MailBoxServicesException
     * @throws FS2Exception
     * @throws IOException
     * @throws URISyntaxException
     */
    void writeResponseToMailBox(ByteArrayOutputStream response, String filename) throws URISyntaxException, IOException, FS2Exception, MailBoxServicesException;

    /**
     * Adds or Updates the dynamic properties to a processor. The input should be a Json string which should contain name value pair.
     *
     * @param dynamicProperties
     * @throws IOException
     * @throws JAXBException
     */
    void addorUpdateCustomProperty(String dynamicProperties) throws JAXBException, IOException;

    /**
     * Returns the list of custom properties of the processor known only to java script
     *
     * @return
     * @throws IOException
     * @throws JAXBException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    Properties getCustomProperties();

    /**
     * Returns array of credentials of the processor
     *
     * @return
     */
    CredentialDTO[] getCredentials() throws MailBoxConfigurationServicesException, SymmetricAlgorithmException;

    /**
     * Send email notifications
     *
     * @param toEmailAddrList The extra receivers. The default receiver will be available in the mailbox.
     * @param subject         The notification subject
     * @param emailBody       The body of the notification
     * @param type            The notification type(TEXT/HTML).
     */
    void sendEmail(List<String> toEmailAddrList, String subject, String emailBody, String type);

    /**
     * Send email notifications
     *
     * @param toEmailAddrList The extra receivers. The default receiver will be available in the mailbox.
     * @param subject         The notification subject
     * @param emailBody       The body of the notification
     * @param type            The notification type(TEXT/HTML).
     * @param isOverwrite     To overwrite the configured mail address in the mailbox.
     */
    void sendEmail(List<String> toEmailAddrList, String subject, String emailBody, String type, boolean isOverwrite);

    /**
     * Returns the properties of the Mailbox
     *
     * @return
     */
    Properties getMailBoxProperties();


    /**
     * To Retrieve the Payload URI
     *
     * @return Payload URI String
     * @throws MailBoxConfigurationServicesException
     * @throws MailBoxServicesException
     */
    String getPayloadURI() throws MailBoxServicesException, IOException;

    /**
     * To store the input stream in storage
     *
     * @param stream response stream
     * @param file   relay file instance
     * @throws Exception
     */
    String persistResponse(InputStream stream, RelayFile file);

    /**
     * To store the response stream in storage
     *
     * @param stream response stream
     * @param file   relay file instance
     * @return
     */
    String persistResponse(ByteArrayOutputStream stream, RelayFile file);

    /**
     * API to set the response uri in the context
     *
     * @param responseUri fs2 uri
     */
    void setResponseUri(String responseUri);

    /**
     * Method to do clean up activities once JS completes the execution of a processor
     */
    void cleanup();

    /**
     * Info level logging
     *
     * @param msg
     */
    void logInfo(String msg);

    /**
     * Error level logging
     *
     * @param msg
     */
    void logError(String msg);

    /**
     * Error level logging
     *
     * @param error
     */
    void logError(Object error);

    /**
     * Error level logging
     *
     * @param error
     */
    void logError(Throwable error);

    /**
     * Debug level logging
     *
     * @param msg
     */
    void logDebug(String msg);

    /**
     * Method to logs the TVAPI status and activity messages to LENS
     *
     * @param msg    Message String to be logged in LENS event log
     * @param file   File corresponding to the log
     * @param status Status of the LENS logging
     */
    void logToLens(String msg, File file, ExecutionState status);

    /**
     * Method to logs the TVAPI status and activity messages to LENS
     *
     * @param msg    Message String to be logged in LENS event log
     * @param file   File corresponding to the log
     * @param status Status of the LENS logging
     * @param e      Exception
     */
    void logToLens(String msg, File file, ExecutionState status, Exception e);

    /**
     * Method to logs the TVAPI status and activity messages to LENS
     *
     * @param msg    Message String to be logged in LENS event log
     * @param file   File corresponding to the log
     * @param status Status of the LENS logging
     */
    void logToLens(String msg, RelayFile file, ExecutionState status);

    /**
     * Method to logs the TVAPI status and activity messages to LENS
     *
     * @param msg    Message String to be logged in LENS event log
     * @param file   File corresponding to the log
     * @param status Status of the LENS logging
     * @Param e      Exception
     */
    void logToLens(String msg, RelayFile file, ExecutionState status, Exception e);
    
    void asyncSweeperProcessForSingleFile(File file, SweeperStaticPropertiesDTO staticProp);
    
    void asyncSweeperProcessForMultipleFiles(String targetLocation, String pipeLineId, boolean securePayload, boolean lensVisibility);

}
