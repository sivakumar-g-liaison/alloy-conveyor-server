/**
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

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.fs2.api.FS2Exception;
import com.liaison.fs2.api.FS2MetaSnapshot;
import com.liaison.fs2.api.FS2MetaSnapshotImpl;
import com.liaison.fs2.api.FS2ObjectAlreadyExistsException;
import com.liaison.fs2.api.FlexibleStorageSystem;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.dto.ConfigureJNDIDTO;
import com.liaison.mailbox.service.dto.directorysweeper.FileAttributesDTO;
import com.liaison.mailbox.service.dto.directorysweeper.FileGroupDTO;
import com.liaison.mailbox.service.dto.directorysweeper.SweepConditions;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.FS2InstanceCreator;
import com.liaison.mailbox.service.util.HornetQJMSUtil;
import com.liaison.mailbox.service.util.MailBoxUtility;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(DirectorySweeperProcessor.class);

	@SuppressWarnings("unused")
	private DirectorySweeperProcessor() {
		// to force creation of instance only by passing the processor entity
	}

	public DirectorySweeperProcessor(Processor configurationInstance) {
		super(configurationInstance);
	}

	@Override
	public void invoke() {

		try {
			executeRequest();
			modifyProcessorExecutionStatus(ExecutionStatus.COMPLETED);
		} catch (Exception e) {

			// TODO Re stage and update status in FSM
			modifyProcessorExecutionStatus(ExecutionStatus.FAILED);
			sendEmail(null, configurationInstance.getProcsrName() + ":" + e.getMessage(), e.getMessage(), "HTML");
			e.printStackTrace();
		}

	}

	private void executeRequest() throws Exception {

		// Get root from folders input_folder
		String inputLocation = getPayloadURI();
		String fileRenameFormat = getMailBoxProperties().getProperty(MailBoxConstants.FILE_RENAME_FORMAT_PROP_NAME);

		// Validation of the necessary properties
		if (MailBoxUtility.isEmpty(inputLocation)) {
			throw new MailBoxServicesException(Messages.PAYLOAD_LOCATION_NOT_CONFIGURED);
		}
		if (null == fileRenameFormat) {
			fileRenameFormat = MailBoxConstants.SWEEPED_FILE_EXTN;
		}

		// Sweeps the directory and constructs the list of file attributes dto.
		List<FileAttributesDTO> files = sweepDirectory(inputLocation, false, false, null, fileRenameFormat);

		if (files.isEmpty()) {
			LOGGER.info("The given directory is empty.");
		} else {

			// Read from mailbox property - grouping js location
			List<List<FileAttributesDTO>> fileGroups = groupingFiles(files);

			String sweepedFileLocation = getMailBoxProperties().getProperty(MailBoxConstants.SWEEPED_FILE_LOCATION);
			if (!MailBoxUtility.isEmpty(sweepedFileLocation)) {

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
	 */
	public List<FileAttributesDTO> sweepDirectory(String root, boolean includeSubDir, boolean listDirectoryOnly,
			SweepConditions sweepConditions, String fileRenameFormat) throws IOException, URISyntaxException,
			MailBoxServicesException, FS2Exception {

		Path rootPath = Paths.get(root);
		if (!Files.isDirectory(rootPath)) {
			throw new MailBoxServicesException(Messages.INVALID_DIRECTORY);
		}

		List<Path> result = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath, defineFilter(listDirectoryOnly))) {
			for (Path file : stream) {

				if (!file.getFileName().toString().contains(fileRenameFormat)) {
					result.add(file);
				}
			}
		} catch (IOException e) {
			throw e;
		}

		List<FileAttributesDTO> fileAttributes = new ArrayList<>();
		FileAttributesDTO attribute = null;
		for (Path path : result) {

			attribute = new FileAttributesDTO();
			attribute.setFilePath(path.toAbsolutePath().toString());

			BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
			attribute.setTimestamp(attr.creationTime().toString());
			attribute.setSize(attr.size());
			attribute.setFilename(path.toFile().getName());
			fileAttributes.add(attribute);
		}

		return fileAttributes;
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
	private List<List<FileAttributesDTO>> groupingFiles(List<FileAttributesDTO> files)
			throws ScriptException, IOException, URISyntaxException, NoSuchMethodException, MailBoxServicesException {

		String groupingJsPath = configurationInstance.getJavaScriptUri();
		List<List<FileAttributesDTO>> fileGroups = new ArrayList<>();

		if (!MailBoxUtility.isEmpty(groupingJsPath)) {

			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");

			engine.eval(getJavaScriptString(groupingJsPath));
			Invocable inv = (Invocable) engine;

			// invoke the method in javascript
			inv.invokeFunction("init", files);

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
	 * @param restUrl
	 *            The url of the rest service if it is rest.
	 * @param isRest
	 *            true if it is to post rest service
	 * @throws Exception
	 */
	private void postToQueue(String input) throws Exception {

		HornetQJMSUtil util = new HornetQJMSUtil();

		String providerURL = MailBoxUtility.getEnvironmentProperties().getProperty("providerurl");
		String queueName = MailBoxUtility.getEnvironmentProperties().getProperty("queuename");

		ConfigureJNDIDTO jndidto = new ConfigureJNDIDTO();
		jndidto.setInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
		jndidto.setProviderURL(providerURL);
		jndidto.setQueueName(queueName);
		jndidto.setUrlPackagePrefixes("org.jboss.naming");
		jndidto.setMessage(input);

		util.postMessage(jndidto);

	}

	/**
	 * Method is used to rename the processed files using given file rename format. If
	 * sweepedFileLocation is available in the mailbox files will be moved to the given location.
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
			throws IOException,
			JSONException, FS2Exception, URISyntaxException {

		if (MailBoxUtility.isEmpty(sweepedFileLocation)) {

			LOGGER.info("Renaming the processed files");
			for (FileAttributesDTO file : fileList) {

				Path oldPath = new File(file.getFilePath()).toPath();
				Path newPath = oldPath.getParent().resolve(oldPath.toFile().getName() + fileRenameFormat);

				// Creating meta snapshot
				FlexibleStorageSystem FS2 = FS2InstanceCreator.getFS2Instance();
				File fileLoc = new File(newPath.toFile().getAbsolutePath());
				FS2MetaSnapshot metaSnapShot = new FS2MetaSnapshotImpl(fileLoc.toURI(), new Date(), "DirectorySweeper");

				// Constructing the fs2 file
				try {
					FS2.createObjectEntry(oldPath.toUri(), metaSnapShot.toJSON(), null);

				} catch (FS2ObjectAlreadyExistsException e) {
					FS2.deleteRecursive(oldPath.toUri());
					FS2.createObjectEntry(oldPath.toUri(), metaSnapShot.toJSON(), null);
				}

				// Renaming the file at the end of the step when everything is done.
				move(oldPath, newPath);
				file.setFilePath(oldPath.toString());
				file.setGuid(MailBoxUtility.getGUID());
			}

		} else {

			LOGGER.info("Renaming the processed files with sweeped file location.");

			Path target = Paths.get(sweepedFileLocation);
			for (FileAttributesDTO file : fileList) {

				Path oldPath = new File(file.getFilePath()).toPath();
				Path newPath = target.resolve(oldPath.toFile().getName() + fileRenameFormat);

				// Creating meta snapshot
				FlexibleStorageSystem FS2 = FS2InstanceCreator.getFS2Instance();
				File fileLoc = new File(newPath.toFile().getAbsolutePath());
				FS2MetaSnapshot metaSnapShot = new FS2MetaSnapshotImpl(fileLoc.toURI(), new Date(), "DirectorySweeper");

				// Constructing the fs2 file
				try {
					FS2.createObjectEntry(oldPath.toUri(), metaSnapShot.toJSON(), null);

				} catch (FS2ObjectAlreadyExistsException e) {
					FS2.deleteRecursive(oldPath.toUri());
					FS2.createObjectEntry(oldPath.toUri(), metaSnapShot.toJSON(), null);
				}

				// Renaming the file at the end of the step when everything is done.
				move(oldPath, newPath);
				file.setFilePath(oldPath.toString());
				file.setGuid(MailBoxUtility.getGUID());
			}
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
	private String constructMetaDataJson(List<FileAttributesDTO> files)
			throws JsonGenerationException, JsonMappingException,
			IOException, JAXBException {

		FileGroupDTO group = new FileGroupDTO();
		group.getFileAttributes().addAll(files);

		String jsonResponse = MailBoxUtility.marshalToJSON(group);
		return jsonResponse;
	}

}
