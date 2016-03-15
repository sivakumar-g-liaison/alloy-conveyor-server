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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.SftpException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.sftp.G2SFTPClient;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.FSMEventDAOBase;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.processor.helper.ClientFactory;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.SFTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * SFTP remote uploader to perform push operation, also it has support methods
 * for JavaScript.
 *
 * @author OFS
 */
public class SFTPRemoteUploader extends AbstractProcessor implements MailBoxProcessorI {

	private static final Logger LOGGER = LogManager.getLogger(SFTPRemoteUploader.class);

    /*
     * Required for JS
     */
	private G2SFTPClient sftpClient;

	@SuppressWarnings("unused")
	private SFTPRemoteUploader() {
	}

	public SFTPRemoteUploader(Processor processor) {
		super(processor);
	}

	/**
	 * Java method to execute the SFTPrequest to upload the file or folder
	 *
	 */
	private void executeRequest(String executionId, MailboxFSM fsm) {

	    G2SFTPClient sftpRequest = null;
		try {

			LOGGER.info(constructMessage("Start run"));
			long startTime = System.currentTimeMillis();

			String path = getPayloadURI();

			if (MailBoxUtil.isEmpty(path)) {
                LOGGER.error(constructMessage("The given payload URI is Empty."));
				throw new MailBoxServicesException("The given payload URI is Empty.", Response.Status.CONFLICT);
			}


            File localDir = new File(path);
            File[] subFiles = localDir.listFiles();

            if (subFiles == null || subFiles.length == 0) {
                LOGGER.error(constructMessage("The given payload location {} doesn't have files to upload."), path);
                return;
            }

            sftpRequest = (G2SFTPClient) getClient();
            sftpRequest.setLogPrefix(constructMessage());
            sftpRequest.connect();
            
			if (sftpRequest.openChannel()) {

			    String remotePath = getWriteResponseURI();
				if (MailBoxUtil.isEmpty(remotePath)) {
					LOGGER.info(constructMessage("The given remote URI is Empty."));
					throw new MailBoxServicesException("The given remote URI is Empty.", Response.Status.CONFLICT);
				}

				//GMB-320 - Creates directory to the remote folder
				boolean isCreateFoldersInRemote = ((SFTPUploaderPropertiesDTO) getProperties()).isCreateFoldersInRemote();

                // check the given location exists in the remote server
				try {
				    sftpRequest.getNative().lstat(remotePath);
                } catch (SftpException ex) {
                    if (!isCreateFoldersInRemote) {
                        LOGGER.error(constructMessage("Unable to create directory {} because create folders in remote is not enabled."), remotePath);
                        throw new MailBoxServicesException("The remote directory {} is not exist.", Response.Status.CONFLICT);
                    } else {
                        createDirectoriesInRemote(sftpRequest, remotePath);
                    }

                }

                sftpRequest.changeDirectory(remotePath);

				LOGGER.info(constructMessage("Ready to upload files from local path {} to remote path {}"), path, remotePath);
				uploadDirectory(sftpRequest, path, remotePath, executionId, fsm, subFiles);

			}
			long endTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Number of files processed {}"), totalNumberOfProcessedFiles);
            LOGGER.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
            LOGGER.info(constructMessage("End run"));

		} catch (LiaisonException | MailBoxServicesException | IOException
				| SftpException | NoSuchFieldException
				| SecurityException | IllegalArgumentException | IllegalAccessException
				| JAXBException | URISyntaxException e) {
            LOGGER.error(constructMessage("Error occurred during sftp upload", seperator, e.getMessage()), e);
			throw new RuntimeException(e);
        } finally {
		    if (sftpRequest != null) {
                sftpRequest.disconnect();
            }
		}

	}

    /**
     * Java method to upload the file or folder
     *
     * @throws IOException
     * @throws LiaisonException
     * @throws SftpException
     * @throws MailBoxServicesException
     * @throws com.liaison.commons.exception.LiaisonException
     * @throws JAXBException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws URISyntaxException
     *
     */

	public void uploadDirectory(G2SFTPClient sftpRequest, String localParentDir, String remoteParentDir, String executionId, MailboxFSM fsm, File[] subFiles)
			throws IOException, LiaisonException, SftpException, MailBoxServicesException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, JAXBException, URISyntaxException {

		// variable to hold the status of file upload request execution
		int replyCode = -1;
		SFTPUploaderPropertiesDTO staticProp = (SFTPUploaderPropertiesDTO) getProperties();
		FSMEventDAOBase eventDAO = new FSMEventDAOBase();

		Date lastCheckTime = new Date();
		String constantInterval = MailBoxUtil.getEnvironmentProperties().getString(MailBoxConstants.DEFAULT_INTERRUPT_SIGNAL_FREQUENCY_IN_SEC);
		boolean isCreateFoldersInRemote = staticProp.isCreateFoldersInRemote();

		for (File item : subFiles) {
		    
			//interrupt signal check has to be done only if execution Id is present
			if(!StringUtil.isNullOrEmptyAfterTrim(executionId) && ((new Date().getTime() - lastCheckTime.getTime())/1000) > Long.parseLong(constantInterval)) {
			    
				lastCheckTime = new Date();
				if(eventDAO.isThereAInterruptSignal(executionId)) {
				    
                    LOGGER.debug("The executor with execution id  " + executionId + " is gracefully interrupted");
					fsm.createEvent(ExecutionEvents.INTERRUPTED, executionId);
					fsm.handleEvent(fsm.createEvent(ExecutionEvents.INTERRUPTED));
					return;
				}
			}

			if (item.getName().equals(".") || item.getName().equals("..")) {
				// skip parent directory and the directory itself
				continue;
			}
			if (item.isDirectory()) {

				if (MailBoxConstants.PROCESSED_FOLDER.equals(item.getName()) ||
				        MailBoxConstants.ERROR_FOLDER.equals(item.getName())) {
					// skip processed folder
                    LOGGER.debug(constructMessage("skipping processed/error folder"));
					continue;
				}

				String remoteFilePath = remoteParentDir + File.separatorChar + item.getName();
				try {
				    sftpRequest.getNative().lstat(remoteFilePath);
                    LOGGER.debug(constructMessage("The remote directory {} exists."), remoteFilePath);
				} catch (SftpException ex) {
				    if (isCreateFoldersInRemote) {
				        sftpRequest.getNative().mkdir(remoteFilePath);
				        LOGGER.info(constructMessage("The remote directory {} is not exist.So created that."), remoteFilePath);
				    } else {
				        LOGGER.error(constructMessage("Unable to create directory {} because create folders in remote is not enabled."), remoteFilePath);
				        throw new MailBoxServicesException("The remote directory " + remoteFilePath + " is not exist.", Response.Status.CONFLICT);
				    }
				}
				
				// upload the sub directory
				sftpRequest.changeDirectory(remoteFilePath);
				String localDr = localParentDir + File.separatorChar + item.getName();
                uploadDirectory(sftpRequest, localDr, remoteFilePath, executionId, fsm, new File(localDr).listFiles());
				replyCode = MailBoxConstants.SFTP_FILE_TRANSFER_ACTION_OK;

			} else {

			    String currentFileName = item.getName();

			    //File Modification Check
			    if (MailBoxUtil.validateLastModifiedTolerance(item.toPath())) {
	                LOGGER.info(constructMessage("The file {} is still in progress, so it is skipped."), currentFileName);
	                continue;
	            }

				// Check if the file to be uploaded is included or not excluded
				if(!checkFileIncludeorExclude(staticProp.getIncludedFiles(),
				        currentFileName,
				        staticProp.getExcludedFiles())) {
				    continue;
				}

				//add status indicator if specified to indicate that uploading is in progress
				String statusIndicator = staticProp.getFileTransferStatusIndicator();
				String uploadingFileName = (!MailBoxUtil.isEmpty(statusIndicator)) ? currentFileName + "."
						+ statusIndicator : currentFileName;

				// upload the file
			    try (InputStream inputStream = new FileInputStream(item)) {
			        sftpRequest.changeDirectory(remoteParentDir);
					LOGGER.info(constructMessage("uploading file {} from local path {} to remote path {}"),
							currentFileName, localParentDir, remoteParentDir);
                    replyCode = sftpRequest.putFile(uploadingFileName, inputStream);
			    }

			    // Check whether the file uploaded successfully
				if (replyCode == MailBoxConstants.SFTP_FILE_TRANSFER_ACTION_OK) {

					totalNumberOfProcessedFiles++;
					LOGGER.info(constructMessage("File {} uploaded successfully"), currentFileName);

					// Renames the uploaded file to original extension if the fileStatusIndicator is given by User
					if (!MailBoxUtil.isEmpty(statusIndicator)) {
						int renameStatus = sftpRequest.renameFile(uploadingFileName, currentFileName);
						if (renameStatus == MailBoxConstants.SFTP_FILE_TRANSFER_ACTION_OK) {
							LOGGER.info(constructMessage("File {} renamed successfully"), currentFileName);
						} else {
							LOGGER.info(constructMessage("File {} renaming failed"), currentFileName);
						}
					}
					
					// delete files once successfully uploaded
					deleteFilesAfterSuccessfulUpload(item);
					StringBuilder message = new StringBuilder()
												.append("File ")
												.append(currentFileName)
												.append(" uploaded successfully")
												.append(" to remote path ")
												.append(remoteParentDir);
					// Glass Logging 
					logGlassMessage(message.toString(), item, ExecutionState.COMPLETED);
				} else {
					
					StringBuilder message = new StringBuilder()
												.append("Failed to upload file ")
												.append(currentFileName)
												.append(" from local path ")
												.append(localParentDir)
												.append(" to remote path ")
												.append(remoteParentDir);
					// Glass Logging 
					logGlassMessage(message.toString(), item, ExecutionState.FAILED);
				}
			}
		}
		// To delete the folder after uploading of all files inside this folder is done
		deleteFilesAfterSuccessfulUpload(new File(localParentDir));
	}

	@Override
	public void runProcessor(Object dto, MailboxFSM fsm) {

		LOGGER.debug("Entering in invoke.");

		try {

		    setReqDTO((TriggerProcessorRequestDTO) dto);
			// SFTPRequest executed through JavaScript
			if (getProperties().isHandOverExecutionToJavaScript()) {

				fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));

				// Use custom G2JavascriptEngine
				JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);

			} else {
				// SFTPRequest executed through Java
				executeRequest(getReqDTO().getExecutionId(), fsm);
			}
		} catch (IOException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	 }

	public boolean checkFileExistence() throws Exception {

		LOGGER.debug ("Entering file Existence check for SFTP Uploader processor");
		boolean isFileExists = false;
		G2SFTPClient sftpRequest = (G2SFTPClient) getClient();
		sftpRequest.connect();

		if (sftpRequest.openChannel()) {

		    String remotePath = getWriteResponseURI();
			if (MailBoxUtil.isEmpty(remotePath)) {
                LOGGER.debug("The given remote URI is Empty.");
				throw new MailBoxServicesException("The given remote URI is Empty.", Response.Status.CONFLICT);
			}

			List <String> files = sftpRequest.listFiles(remotePath);
			isFileExists = (null != files && !files.isEmpty());
		}

		sftpRequest.disconnect();
		LOGGER.debug("File Eixstence check completed for SFTP Uploader. File exists - {}", isFileExists);
		return isFileExists;
	}

	@Override
	public Object getClient() {
	    sftpClient = (G2SFTPClient) ClientFactory.getClient(this);
	    return sftpClient;
	}

	@Override
	public void cleanup() {
	    if (null != sftpClient) {
	        sftpClient.disconnect();
	    }
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
			throw new MailBoxConfigurationServicesException(Messages.LOCAL_FOLDERS_CREATION_FAILED,
					configuredPath, Response.Status.BAD_REQUEST);
		}

	}

    @Override
    public void logToLens(String msg, File file, ExecutionState status) {
        logGlassMessage(msg, file, status);
    }

    /**
     * Create directories in the remote server if it is not available
     * 
     * @param sftpRequest
     * @param remotePath
     * @throws SftpException
     */
    private void createDirectoriesInRemote(G2SFTPClient sftpRequest, String remotePath) throws SftpException {

        for (String directory : remotePath.split(File.separatorChar == '\\' ? "\\\\" : File.separator)) {

            if (directory.isEmpty()) {// For when path starts with /
                continue;
            }

            // Info logs are required to track the folders creation and it won't log frequently
            try {
                sftpRequest.getNative().lstat(directory);
                LOGGER.debug(constructMessage("The remote directory {} already exists."), directory);
            } catch (SftpException ex) {
                LOGGER.info(constructMessage("The remote directory {} doesn't exist."), directory);
                sftpRequest.getNative().mkdir(directory);
                LOGGER.info(constructMessage("Created remote directory {}"), directory);
            }

        }

    }
}
