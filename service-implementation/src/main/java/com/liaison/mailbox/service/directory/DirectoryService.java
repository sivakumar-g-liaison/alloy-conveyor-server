/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.directory;

import java.io.IOException;
import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ShellScriptEngineUtil;
import com.liaison.usermanagement.service.dto.DirectoryMessageDTO;

public class DirectoryService {
    
    private static final Logger LOGGER = LogManager.getLogger(DirectoryService.class);

    /**
     * Invokes the shell script to create folder and assign permissions to the created folder.
     *
     * @param gatewayType
     * @param username
     * @throws IOException
     * @throws MailBoxServicesException
     */
    private void invokeScriptToCreateFolderAndAssignPermission(String gatewayType, String userName) throws IOException, MailBoxServicesException {

        // username should be in lowercase while folder creation
        userName = userName.toLowerCase();

        String folderPath = getHomeFolderPath(gatewayType, userName);
        // Invokes script to create folder and assign permissions
        // executing the script
        LOGGER.info("Invokes script to create folder and assign permissions for gateway type {} and user {}", gatewayType, userName);
        String scriptPath = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.PERMISSION_SCRIPT_PATH);
        String sftpUserGroupName = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.SFTP_USER_GROUP_NAME);
        ShellScriptEngineUtil.executeShellScript(scriptPath, folderPath, userName, sftpUserGroupName);

    }
    
    /**
    * Invokes the shell script to delete folder.
    *
    * @param gatewayType
    * @param username
    * @throws IOException
    * @throws MailBoxServicesException
    */
   private void invokeScriptToDeleteHomeFolders(String gatewayType, String userName) throws IOException, MailBoxServicesException {

       userName = userName.toLowerCase();
       // Invokes script to delete home foldess
       // executing the script
       String homeFolderPath = getHomeFolderPath(gatewayType, userName);
       LOGGER.info("Invokes script to delete user home folders in path",homeFolderPath);
       String scriptPath = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DELETION_SCRIPT_PATH);
       ShellScriptEngineUtil.executeDeletionShellScript(scriptPath, homeFolderPath);

   }
    
    /**
     * Method to get home folder path from the given gatewaytype and userName
     *
     * @param gatewayType - gateway type of account
     * @param userName - account userName
     * @return home folder path
     * @throws IOException
     */
    private String getHomeFolderPath(String gatewayType, String userName) throws IOException {

        LOGGER.debug("retrieving home folder path for user {}", userName);
        switch (gatewayType) {

            case MailBoxConstants.FTP:
                return (MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.FTP_PATH)) + File.separatorChar + userName;
            case MailBoxConstants.FTPS:
                return (MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.FTPS_PATH)) + File.separatorChar + userName;
            case MailBoxConstants.SFTP:
                return (MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.SFTP_PATH)) + File.separatorChar + userName;
            default :
                throw new RuntimeException("undefined gateway type");
        }
    }

    /**
     * Based on operation type invokes create/delete methods.
     * @param message
     * @throws IOException 
     * @throws MailBoxServicesException 
     */
    public void executeDirectoryOperation(DirectoryMessageDTO message) throws MailBoxServicesException, IOException {
        
        if (DirectoryOperationTypes.CREATE.value().equals(message.getOperationType())) {
            invokeScriptToCreateFolderAndAssignPermission(message.getGatewayType(), message.getUserName());
        } else if (DirectoryOperationTypes.DELETE.value().equals(message.getOperationType())) {
            invokeScriptToDeleteHomeFolders(message.getGatewayType(), message.getUserName());
        } else {
            throw new RuntimeException("Invalid operation");
        }
    }
  
}
