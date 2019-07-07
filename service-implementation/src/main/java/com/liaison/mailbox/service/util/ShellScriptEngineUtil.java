/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.util;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.logging.LogTags;
import com.liaison.mailbox.service.core.FileStageReplicationService;
import com.liaison.usermanagement.service.dto.DirectoryMessageDTO;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.liaison.mailbox.MailBoxConstants.FAILOVER_MSG_TYPE;
import static com.liaison.mailbox.MailBoxConstants.MESSAGE;

/**
 * Utilities for executing shell scripts.
 * 
 * @author OFS
 *
 */

public class ShellScriptEngineUtil {
    
    private static final Logger LOGGER = LogManager.getLogger(ShellScriptEngineUtil.class);
    private static final String OWNER = "owner";
    private static final String HOME_FOLDER_PATH = "homeFolderPath";
    private static final String USER_GROUP_NAME = "userGroupName";
    private static final String USER_NAME = "username";
    private static final String ARG_OWNER = "${owner}";
    private static final String ARG_HOME_FOLDER_PATH = "${homeFolderPath}";
    private static final String ARG_USER_GROUP_NAME = "${userGroupName}";
    private static final String ARG_USER_NAME = "${username}";
    private static final String COMMAND_ID = "id";
    private static final int EXIT_VALUE = 0;

    /**
     * This method executes shell script for creating directories.
     * 
     * @param scriptPath
     * @param homeFolderPath
     * @param username
     * @param userGroupName
     */
    public static void executeShellScript(String scriptPath, String homeFolderPath, String username,String userGroupName) {
        
        ByteArrayOutputStream outputStream = null;
        
        try {
            
            ThreadContext.clearMap();
            ThreadContext.put(LogTags.USER_PRINCIPAL_ID, username);
            LOGGER.debug("Entered shell script execution. The script to execute is " + scriptPath);
            // script file to be executed
            File shellScript = new File(scriptPath);
            if (!shellScript.exists()) {
                LOGGER.error("The given permission script is not available at " + scriptPath);
                return;
            }
            
            Map <String, String> args = new HashMap<>();
            CommandLine cmdLine = new CommandLine(shellScript);
            LOGGER.debug("Adding command line arguments");
            args.put(OWNER, username);
            args.put(HOME_FOLDER_PATH, homeFolderPath);
            args.put(USER_GROUP_NAME, userGroupName);
            cmdLine.addArgument(ARG_OWNER);
            cmdLine.addArgument(ARG_HOME_FOLDER_PATH);
            cmdLine.addArgument(ARG_USER_GROUP_NAME);
            cmdLine.setSubstitutionMap(args);
            
            Executor executor = new DefaultExecutor();
            executor.setExitValue(EXIT_VALUE);
            outputStream = new ByteArrayOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            executor.setStreamHandler(streamHandler);
            int exitValue = executor.execute(cmdLine);
            if (exitValue == EXIT_VALUE) {
                LOGGER.info("Script executed successfully");
                LOGGER.info(outputStream.toString());
            } else {
                LOGGER.error("Script execution failed");
                LOGGER.error(outputStream.toString());
            }
            
        } catch (IOException e) {
            LOGGER.error("Script execution failed " + e.getMessage(), e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close the PumpStreamHandler output stream " + e.getMessage());
                }
            }
            ThreadContext.clearMap();
        }
    }
    
    /**
     * This method executes shell script for deleting directories.
     * 
     * @param scriptPath
     * @param homeFolderPath
     * @param username
     */
    public static void executeDeletionShellScript(String scriptPath, String homeFolderPath, String username) {
        
        ByteArrayOutputStream outputStream = null;
        try {
            
            ThreadContext.clearMap();
            ThreadContext.put(LogTags.USER_PRINCIPAL_ID, username);
            
            LOGGER.debug("Entered deletion shell script execution. The script to execute is " + scriptPath);
            // script file to be executed
            File shellScript = new File(scriptPath);
            if (!shellScript.exists()) {
                LOGGER.error("The given deletion script is not available at " + scriptPath);
                return;
            }
            
            Map <String, String> args = new HashMap<String, String>();
            CommandLine cmdLine = new CommandLine(shellScript);
            LOGGER.debug("Adding command line arguments");
            args.put(HOME_FOLDER_PATH, homeFolderPath);
            cmdLine.addArgument(ARG_HOME_FOLDER_PATH);
            cmdLine.setSubstitutionMap(args);
            
            Executor executor = new DefaultExecutor();
            executor.setExitValue(EXIT_VALUE);
            outputStream = new ByteArrayOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            executor.setStreamHandler(streamHandler);
            int exitValue = executor.execute(cmdLine);
            if (exitValue == EXIT_VALUE) {
                LOGGER.info("Deletion Script executed successfully");
                LOGGER.info(outputStream.toString());
            } else {
                LOGGER.error("Deletion Script execution failed");
                LOGGER.error(outputStream.toString());
            }
            
        } catch (IOException e) {
            LOGGER.error("Script execution failed " + e.getMessage(), e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close the PumpStreamHandler output stream " + e.getMessage());
                }
            }
            ThreadContext.clearMap();
        }
    }

    public static boolean validateUser(String username) {

        ByteArrayOutputStream outputStream = null;
        try {

            ThreadContext.clearMap();
            ThreadContext.put(LogTags.USER_PRINCIPAL_ID, username);

            Map<String, String> args = new HashMap<>();
            CommandLine cmdLine = new CommandLine(COMMAND_ID);
            LOGGER.debug("Adding command line arguments");
            args.put(USER_NAME, username);
            cmdLine.addArgument(ARG_USER_NAME);
            cmdLine.setSubstitutionMap(args);

            Executor executor = new DefaultExecutor();
            executor.setExitValue(EXIT_VALUE);
            outputStream = new ByteArrayOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            executor.setStreamHandler(streamHandler);
            int exitValue = executor.execute(cmdLine);
            if (exitValue == EXIT_VALUE) {
                LOGGER.info("User validation successful - {}", outputStream.toString());
                return true;
            } else {
                LOGGER.error("Failed to validate the user - {}", outputStream.toString());
            }

        } catch (IOException e) {
            LOGGER.error("Failed to validate the user. Script execution failed " + e.getMessage(), e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close the PumpStreamHandler output stream " + e.getMessage());
                }
            }
            ThreadContext.clearMap();
        }

        return false;
    }

}