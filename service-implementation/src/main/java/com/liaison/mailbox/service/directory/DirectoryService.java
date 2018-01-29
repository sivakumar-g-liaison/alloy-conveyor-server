/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.directory;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ShellScriptEngineUtil;
import com.liaison.usermanagement.enums.DirectoryOperationTypes;
import com.liaison.usermanagement.service.dto.DirectoryMessageDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

import static com.liaison.mailbox.service.util.MailBoxUtil.CONFIGURATION;

/**
 * This class is used to create and delete directories based on usermanagement information.
 *
 * @author OFS
 *
 */
public class DirectoryService implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(DirectoryService.class);

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DirectoryService(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            this.executeDirectoryOperation(JAXBUtility.unmarshalFromJSON(message, DirectoryMessageDTO.class));
        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Invokes the shell script to create folder and assign permissions to the created folder.
     *
     * @param gatewayType gateway type
     * @param userName username
     * @throws IOException
     */
    private void invokeScriptToCreateFolderAndAssignPermission(String gatewayType, String userName) throws IOException {

        String folderPath = getHomeFolderPath(gatewayType, userName);
        // Invokes script to create folder and assign permissions
        // executing the script
        LOGGER.info("Invokes script to create folder and assign permissions for gateway type {} and user {}", gatewayType, userName);
        String scriptPath = CONFIGURATION.getString(MailBoxConstants.PERMISSION_SCRIPT_PATH);
        String sftpUserGroupName = CONFIGURATION.getString(MailBoxConstants.SFTP_USER_GROUP_NAME);
        ShellScriptEngineUtil.executeShellScript(scriptPath, folderPath, userName, sftpUserGroupName);

    }

    /**
     * Invokes the shell script to delete folder.
     *
     * @param gatewayType gateway type
     * @param userName username
     * @throws IOException
     */
    private void invokeScriptToDeleteHomeFolders(String gatewayType, String userName) throws IOException {

        // Invokes script to delete home folders
        // executing the script
        String homeFolderPath = getHomeFolderPath(gatewayType, userName);
        LOGGER.info("Invokes script to delete user home folders in path {} for user {}", homeFolderPath, userName);
        String scriptPath = CONFIGURATION.getString(MailBoxConstants.DELETION_SCRIPT_PATH);
        ShellScriptEngineUtil.executeDeletionShellScript(scriptPath, homeFolderPath, userName);

    }

    /**
     * Method to get home folder path from the given gatewaytype and userName
     *
     * @param gatewayType - gateway type of account0
     * @param userName - account userName
     * @return home folder path
     * @throws IOException
     */
    private String getHomeFolderPath(String gatewayType, String userName) throws IOException {

        LOGGER.debug("Retrieving home folder path for user {}", userName);
        switch (gatewayType) {

            case MailBoxConstants.FTP:
                return (MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.FTP_PATH)) + File.separatorChar + userName;
            case MailBoxConstants.FTPS:
                return (MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.FTPS_PATH)) + File.separatorChar + userName;
            case MailBoxConstants.SFTP:
                return (MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.SFTP_PATH)) + File.separatorChar + userName;
            default:
                throw new RuntimeException("Undefined gateway type");
        }
    }

    /**
     * Based on operation type invokes create/delete methods.
     *
     * @param message message from the queue
     * @throws IOException
     */
    public void executeDirectoryOperation(DirectoryMessageDTO message) throws IOException {

        if (DirectoryOperationTypes.CREATE.value().equals(message.getOperationType())) {
            invokeScriptToCreateFolderAndAssignPermission(message.getGatewayType(), message.getUserName().toLowerCase());
            //TODO post it to stream
        } else if (DirectoryOperationTypes.DELETE.value().equals(message.getOperationType())) {
            invokeScriptToDeleteHomeFolders(message.getGatewayType(), message.getUserName().toLowerCase());
            //TODO post it to stream
        } else {
            throw new RuntimeException("Invalid operation");
        }
    }

}
