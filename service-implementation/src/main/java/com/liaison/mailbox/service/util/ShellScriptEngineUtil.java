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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.service.exception.MailBoxServicesException;

/**
 * 
 * @author OFS
 *
 */
public class ShellScriptEngineUtil {

	private static final Logger LOGGER = LogManager.getLogger(ShellScriptEngineUtil.class);

	public static void executeShellScript(String scriptPath, String homeFolderPath, String username,String userGroupName) throws  MailBoxServicesException {

		ByteArrayOutputStream outputStream = null;
		try {

			LOGGER.debug("entered shell script execution. The script to execute is " + scriptPath);
			// script file to be executed
			File shellScript = new File(scriptPath);
			if (!shellScript.exists()) {
				LOGGER.error("The given permission script is not available at " + scriptPath);
				return;
			}

			Map <String, String> args = new HashMap<String, String>();
			CommandLine cmdLine = new CommandLine(shellScript);
			LOGGER.debug("adding command line arguments");
			args.put("owner", username);
			args.put("homeFolderPath", homeFolderPath);
			args.put("userGroupName", userGroupName);
			cmdLine.addArgument("${owner}");
			cmdLine.addArgument("${homeFolderPath}");
			cmdLine.addArgument("${userGroupName}");
			cmdLine.setSubstitutionMap(args);

			Executor executor = new DefaultExecutor();
			executor.setExitValue(0);
			outputStream = new ByteArrayOutputStream();
			PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
			executor.setStreamHandler(streamHandler);
			int exitValue = executor.execute(cmdLine);
			if (exitValue == 0) {
				LOGGER.info("Script executed successfully");
				LOGGER.info(outputStream.toString());
			} else {
				LOGGER.error("Script execution failed");
				LOGGER.error(outputStream.toString());
				throw new MailBoxServicesException("Account creation failed because script execution got failed", Response.Status.INTERNAL_SERVER_ERROR);
			}

		} catch (IOException e) {
			LOGGER.error("script execution failed " + e.getMessage());
			throw new MailBoxServicesException("Account creation failed because script execution got failed", Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					LOGGER.error("Failed to close the PumpStreamHandler output stream " + e.getMessage());
				}
			}
		}

	}
	
   public static void executeDeletionShellScript(String scriptPath, String homeFolderPath) throws  MailBoxServicesException {

        ByteArrayOutputStream outputStream = null;
        try {

            LOGGER.debug("entered deletion shell script execution. The script to execute is " + scriptPath);
            // script file to be executed
            File shellScript = new File(scriptPath);
            if (!shellScript.exists()) {
                LOGGER.error("The given deletion script is not available at " + scriptPath);
                return;
            }

            Map <String, String> args = new HashMap<String, String>();
            CommandLine cmdLine = new CommandLine(shellScript);
            LOGGER.debug("adding command line arguments");
            args.put("homeFolderPath", homeFolderPath);
            cmdLine.addArgument("${homeFolderPath}");
            cmdLine.setSubstitutionMap(args);

            Executor executor = new DefaultExecutor();
            executor.setExitValue(0);
            outputStream = new ByteArrayOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            executor.setStreamHandler(streamHandler);
            int exitValue = executor.execute(cmdLine);
            if (exitValue == 0) {
                LOGGER.info("Deletion Script executed successfully");
                LOGGER.info(outputStream.toString());
            } else {
                LOGGER.error("Deletion Script execution failed");
                LOGGER.error(outputStream.toString());
                throw new MailBoxServicesException("Account deletion failed because script execution got failed", Response.Status.INTERNAL_SERVER_ERROR);
            }

        } catch (IOException e) {
            LOGGER.error("script execution failed " + e.getMessage());
            throw new MailBoxServicesException("Account deletion failed because script execution got failed", Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close the PumpStreamHandler output stream " + e.getMessage());
                }
            }
        }

    }


}
