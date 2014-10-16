/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.processor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.script.ScriptException;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.liaison.fs2.api.CoreFS2Utils;
import com.liaison.fs2.api.FS2MetaSnapshot;
import com.liaison.fs2.api.FS2MetaSnapshotImpl;
import com.liaison.fs2.api.FS2Metadata;
import com.liaison.fs2.api.FS2ObjectHeaders;
import com.liaison.fs2.api.FlexibleStorageSystem;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.fs2.api.exceptions.FS2ObjectAlreadyExistsException;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.com.liaison.queue.MailboxToServiceBrokerWorkTicket;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.dto.directorysweeper.FileAttributesDTO;
import com.liaison.mailbox.service.dto.directorysweeper.FileGroupDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.FS2InstanceCreator;
import com.liaison.mailbox.service.util.JavaScriptEngineUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * DirectorySweeper
 * 
 * <P>
 * DirectorySweeper sweeps the files from mail box and creates meta data about file and post it to
 * the queue.
 * 
 * @author veerasamyn
 */

public class DirectorySweeperProcessor extends AbstractRemoteProcessor implements MailBoxProcessor {

	private static final Logger LOGGER = LogManager.getLogger(DirectorySweeperProcessor.class);

	private String pipeLineID;
	private List<Path> inProgressFiles = new ArrayList<>();

	public void setPipeLineID(String pipeLineID) {
		this.pipeLineID = pipeLineID;
	}

	@SuppressWarnings("unused")
	private DirectorySweeperProcessor() {
		// to force creation of instance only by passing the processor entity
	}

	public DirectorySweeperProcessor(Processor configurationInstance) {
		super(configurationInstance);
	}

	@Override
	public void invoke(String executionId,MailboxFSM fsm) throws Exception {

		if (!MailBoxUtil.isEmpty(configurationInstance.getJavaScriptUri())) {
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
			// Use custom G2JavascriptEngine
			JavaScriptEngineUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), "init", this,LOGGER);
		} else {
			executeRequest();
		}
	}

	private void executeRequest() throws Exception {

		// Get root from folders input_folder
		String inputLocation = getPayloadURI();
		String fileRenameFormat = getDynamicProperties().getProperty(MailBoxConstants.FILE_RENAME_FORMAT_PROP_NAME);
		fileRenameFormat = (fileRenameFormat == null) ? MailBoxConstants.SWEEPED_FILE_EXTN : fileRenameFormat;

		long timeLimit = MailBoxUtil.getEnvironmentProperties().getLong(MailBoxConstants.LAST_MODIFIED_TOLERANCE);

		// Validation of the necessary properties
		if (MailBoxUtil.isEmpty(inputLocation)) {
			throw new MailBoxServicesException(Messages.PAYLOAD_LOCATION_NOT_CONFIGURED, Response.Status.CONFLICT);
		}

		List<FileAttributesDTO> files = (inProgressFiles.isEmpty())
				? sweepDirectory(inputLocation, false, false, fileRenameFormat, timeLimit)
				: validateInprogressFiles(inProgressFiles, timeLimit);

		if (files.isEmpty()) {
			LOGGER.info("There are no files to process.");
		} else {

			// Read from mailbox property - grouping js location
			List<List<FileAttributesDTO>> fileGroups = groupingFiles(files);

			String sweepedFileLocation = processMountLocation(getDynamicProperties().getProperty(
					MailBoxConstants.SWEEPED_FILE_LOCATION));
			if (!MailBoxUtil.isEmpty(sweepedFileLocation)) {

				// If the given sweeped file location is not available then system will create that.
				Path path = Paths.get(sweepedFileLocation);
				if (!Files.isDirectory(path)) {
					Files.createDirectories(path);
				}
			}

			// Renaming the file
			markAsSweeped(files, fileRenameFormat, sweepedFileLocation);

			if (fileGroups.isEmpty()) {
				LOGGER.info("The file group is empty.");
			} else {

				for (List<FileAttributesDTO> fileGroup : fileGroups) {

					String jsonResponse = constructMetaDataJson(fileGroup);
					LOGGER.info("Returns json response.{}", new JSONObject(jsonResponse).toString(2));
					postToQueue(jsonResponse);
				}

			}
		}

		// call again when in-progress file list is not empty
		if (!inProgressFiles.isEmpty()) {
			executeRequest();
		}
	}


	/**
	 * Method is used to retrieve all the files attributes from the given mailbox. This method
	 * supports both FS2 and Java File API
	 * 
	 * @param root
	 *            The mailbox root directory
	 * @param includeSubDir
	 * @param listDirectoryOnly
	 * @param sweepConditions
	 * @return List of FileAttributes
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws MailBoxServicesException
	 * @throws FS2Exception
	 * @throws JAXBException
	 */
	public List<FileAttributesDTO> sweepDirectory(String root, boolean includeSubDir, boolean listDirectoryOnly,
			String fileRenameFormat, long timeLimit) throws IOException, URISyntaxException,
			MailBoxServicesException, FS2Exception, JAXBException {

		Path rootPath = Paths.get(root);
		if (!Files.isDirectory(rootPath)) {
			throw new MailBoxServicesException(Messages.INVALID_DIRECTORY, Response.Status.BAD_REQUEST);
		}

		List<Path> result = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath, defineFilter(listDirectoryOnly))) {
			for (Path file : stream) {

				if (!file.getFileName().toString().contains(fileRenameFormat)) {

					if (validateLastModifiedTolerance(timeLimit, file)) {
						LOGGER.info("The file {} is in progress. So added in the in-progress list.", file.getFileName());
						inProgressFiles.add(file);
						continue;
					}
					result.add(file);
				}
			}
		} catch (IOException e) {
			throw e;
		}

		return getFileAttributes(result);
	}

	/**
	 * Method to get the pipe line id from the remote processor properties.
	 * 
	 * @return
	 * @throws JAXBException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private String getPipeLineID() throws JAXBException, JsonParseException, JsonMappingException, IOException {

		if (MailBoxUtil.isEmpty(this.pipeLineID)) {

			RemoteProcessorPropertiesDTO properties = MailBoxUtil.unmarshalFromJSON(configurationInstance.getProcsrProperties(),
					RemoteProcessorPropertiesDTO.class);
			this.setPipeLineID(properties.getPipeLineID());
		}

		return this.pipeLineID;
	}

	/**
	 * Grouping the files based on the payload threshold and no of files threshold.
	 * 
	 * @param files
	 *            Group of all files in a given directory.
	 * @return List of group of files
	 * @throws ScriptException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws NoSuchMethodException
	 * @throws MailBoxServicesException
	 */
	private List<List<FileAttributesDTO>> groupingFiles(List<FileAttributesDTO> files) throws ScriptException,
	IOException, URISyntaxException, NoSuchMethodException, MailBoxServicesException, Exception {

		String groupingJsPath = configurationInstance.getJavaScriptUri();
		List<List<FileAttributesDTO>> fileGroups = new ArrayList<>();

		if (!MailBoxUtil.isEmpty(groupingJsPath)) {

			// Use custom G2JavascriptEngine
			JavaScriptEngineUtil.executeJavaScript(groupingJsPath, "init", files, LOGGER);

		} else {

			if (files.isEmpty()) {
				LOGGER.info("There are no files available in the directory.");
			}

			List<FileAttributesDTO> fileGroup = new ArrayList<>();
			for (FileAttributesDTO file : files) {

				if (validateAdditionalGroupFile(fileGroup, file)) {
					fileGroup.add(file);
				} else {
					if (!fileGroup.isEmpty()) {
						fileGroups.add(fileGroup);
					}
					fileGroup = new ArrayList<>();
					fileGroup.add(file);

				}
			}

			if (!fileGroup.isEmpty()) {
				fileGroups.add(fileGroup);
			}

		}
		return fileGroups;
	}





    /**
	 * Method to post meta data to rest service/ queue.
	 * 
	 * @param input
	 *            The input message to queue.
	 */
	private void postToQueue(String input)   {
        MailboxToServiceBrokerWorkTicket.getInstance().pushMessages(input);


	}

	/**
	 * Method is used to rename the processed files using given file rename
	 * format. If sweepedFileLocation is available in the mailbox files will be moved to the given
	 * location.
	 * 
	 * @param fileList
	 *            Files list.
	 * @param fileRenameFormat
	 *            The file rename format
	 * @throws IOException
	 * @throws JSONException
	 * @throws FS2Exception
	 * @throws URISyntaxException
	 */
	public void markAsSweeped(List<FileAttributesDTO> fileList, String fileRenameFormat, String sweepedFileLocation)
			throws IOException, JSONException, FS2Exception, URISyntaxException {

		Path target = null;
		Path oldPath = null;
		Path newPath = null;
		FlexibleStorageSystem FS2 = null;
		FS2MetaSnapshot metaSnapShot = null;

		if (!MailBoxUtil.isEmpty(sweepedFileLocation)) {
			target = Paths.get(sweepedFileLocation);
		}

		LOGGER.info("Renaming the processed files");
		for (FileAttributesDTO file : fileList) {

			oldPath = new File(file.getFs2Path()).toPath();
			newPath = (target == null) ? oldPath.getParent().resolve(oldPath.toFile().getName() + fileRenameFormat)
					: target.resolve(oldPath.toFile().getName() + fileRenameFormat);

			// Creating meta snapshot
			FS2 = FS2InstanceCreator.getFS2Instance();
			FS2Metadata metadata = new FS2Metadata(CoreFS2Utils.genURIFromPath(newPath.toString()), true, new Date(),
					"DirectorySweeper", new FS2ObjectHeaders(), null);
			metaSnapShot = new FS2MetaSnapshotImpl(metadata);
			
			// Constructing the fs2 file
			try {
				FS2.createObjectEntry(CoreFS2Utils.genURIFromPath(oldPath.toString()), new FS2ObjectHeaders(), null);

			} catch (FS2ObjectAlreadyExistsException e) {
				FS2.deleteRecursive(oldPath.toUri());
				FS2.createObjectEntry(oldPath.toUri(), new FS2ObjectHeaders(), null);
			}

			// Renaming the file at the end of the step when everything is done.
			move(oldPath, newPath);
			//file.setFs2Path(CoreFS2Utils.genURIFromPath(oldPath.toFile().getAbsolutePath()).toString());
			//GSB-1353- After discussion with Joshua and Sean
			file.setFs2Path(CoreFS2Utils.genURIFromPath(newPath.toFile().getAbsolutePath()).toString());
			file.setGuid(MailBoxUtil.getGUID());
		}

		LOGGER.info("Renaming the processed files - done");

	}

	/**
	 * Method is used to move the file to the sweeped folder.
	 *
	 * @param file
	 *            The source location
	 * @param target
	 *            The target location
	 * @throws IOException
	 */
	private void move(Path file, Path target) throws IOException {
		Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * Method is used to construct the MetaData JSON from the file attributes dto list.
	 *
	 * @param files
	 *            The file attributes list
	 * @return String MetaData JSON string
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws JAXBException
	 */
	private String constructMetaDataJson(List<FileAttributesDTO> files) throws JsonGenerationException,
	JsonMappingException, IOException, JAXBException {

		FileGroupDTO group = new FileGroupDTO();
		group.getFileAttributes().addAll(files);

		String jsonResponse = MailBoxUtil.marshalToJSON(group);
		return jsonResponse;
	}

	/**
	 * Returns file attributes from java.io.File
	 *
	 * @param result
	 *            files
	 * @return list of file attributes
	 * @throws JAXBException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private List<FileAttributesDTO> getFileAttributes(List<Path> result) throws JAXBException, IOException {

		List<FileAttributesDTO> fileAttributes = new ArrayList<>();
		FileAttributesDTO attribute = null;
		BasicFileAttributes attr = null;
		for (Path path : result) {

			attribute = new FileAttributesDTO();
			attribute.setFs2Path(path.toAbsolutePath().toString());
			attribute.setPipeLineID(getPipeLineID());

			attr = Files.readAttributes(path, BasicFileAttributes.class);
			attribute.setTimestamp(attr.creationTime().toString());
			attribute.setSize(attr.size());
			attribute.setFilename(path.toFile().getName());
			fileAttributes.add(attribute);
		}
		return fileAttributes;
	}

	/**
	 * Checks whether a file is modified with in the given time limit
	 * 
	 * @param timelimit
	 *            to check the file is modified with in the given time limit
	 * @param file
	 *            File object
	 * @return true if it is updated with in the given time limit, false otherwise
	 */
	public boolean validateLastModifiedTolerance(long timelimit, Path file) {

		long system = System.currentTimeMillis();
		long lastmo = file.toFile().lastModified();

		if (((system - lastmo)/1000) < timelimit) {
			return true;
		}

		return false;
	}

	/**
	 * Checks recently in-progress files are completed or not
	 * 
	 * @param inprogressFiles
	 *            list of recently update files
	 * @param timelimit
	 *            period of the recent modifications
	 * @return list of file attribute dto
	 * @throws JAXBException
	 * @throws IOException
	 */
	private List<FileAttributesDTO> validateInprogressFiles(List<Path> inprogressFiles, long timelimit)
			throws JAXBException, IOException {

		List<Path> files = new ArrayList<>();
		for (Path file : inprogressFiles) {

			if (!validateLastModifiedTolerance(timelimit, file)) {
				LOGGER.info("There are no changes in the file {} recently. So it is added in the sweeper list.", file.getFileName());
				files.add(file);
				continue;
			}
			LOGGER.info("The file {} is still in progress. So it is removed from the in-progress list.", file.getFileName());
		}

		inprogressFiles.clear();//Clearing the recently updated files after the second call.
		return getFileAttributes(files);
	}

}
