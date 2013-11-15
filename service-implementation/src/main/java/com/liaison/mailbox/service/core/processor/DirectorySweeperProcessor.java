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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.fs2.api.FS2Exception;
import com.liaison.fs2.api.FS2MetaSnapshot;
import com.liaison.fs2.api.FlexibleStorageSystem;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionStatus;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.dto.ConfigureJNDIDTO;
import com.liaison.mailbox.service.dto.directorysweeper.FileAttributesDTO;
import com.liaison.mailbox.service.dto.directorysweeper.MetaDataDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.FS2InstanceCreator;
import com.liaison.mailbox.service.util.HornetQJMSUtil;
import com.liaison.mailbox.service.util.MailBoxServiceUtil;
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

			// Directory Sweeper executed through JavaScript
			if (!MailBoxUtility.isEmpty(configurationInstance.getJavaScriptUri())) {

				ScriptEngineManager manager = new ScriptEngineManager();
				ScriptEngine engine = manager.getEngineByName("JavaScript");

				engine.eval(getJavaScriptString(configurationInstance.getJavaScriptUri()));
				Invocable inv = (Invocable) engine;

				// invoke the method in javascript
				inv.invokeFunction("init", this);

			} else {
				// Directory Sweeper executed through Java
				executeRequest();
			}

			modifyProcessorExecutionStatus(ExecutionStatus.COMPLETED);
		} catch (Exception e) {

			// TODO Re stage and update status in FSM
			modifyProcessorExecutionStatus(ExecutionStatus.FAILED);
			e.printStackTrace();
		}

	}

	private void executeRequest() throws Exception {

		// Get root from folders input_folder
		String inputLocation = getPayloadURI();
		String fileRenameFormat = getMailBoxProperties().getProperty(MailBoxConstants.FILE_RENAME_FORMAT_PROP_NAME);

		// Validation of the necessary properties
		if (MailBoxUtility.isEmpty(inputLocation)) {
			throw new MailBoxServicesException("The given input directroy location is empty.");
		}
		if (null == fileRenameFormat) {
			fileRenameFormat = MailBoxConstants.SWEEPED_FILE_EXTN;
		}

		// Sweeps the directory and constructs the list of file attributes dto.
		List<FileAttributesDTO> files = sweepDirectory(inputLocation, false, false, null, fileRenameFormat);

		// Read from mailbox property - grouping js location
		List<List<FileAttributesDTO>> fileGroups = groupingFiles(files);

		// Renaming the file
		markAsSweeped(files, fileRenameFormat);

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

		String groupingJsPath = getMailBoxProperties().getProperty(MailBoxConstants.GROUPING_JS_PROP_NAME);
		List<List<FileAttributesDTO>> fileGroups = new ArrayList<>();

		if (!MailBoxUtility.isEmpty(groupingJsPath)) {

			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");

			engine.eval(getJavaScriptString(groupingJsPath));
			Invocable inv = (Invocable) engine;

			// invoke the method in javascript
			inv.invokeFunction("init", files);

			// TODO grouping the files- based on the properties given in the mailbox.
			// TODO The input of the js should be List<FileAttributesDTO>
			// TODO The output from the js should be List<List<FileAttributesDTO>

		} else {

			if (files.isEmpty()) {
				LOGGER.info("There are no files available in the directory.");
			}

			List<FileAttributesDTO> fileGroup = new ArrayList<>();
			for (FileAttributesDTO file : files) {

				if (validateAdditionalGroupFile(fileGroup, file)) {
					fileGroup.add(file);
				} else {
					fileGroups.add(fileGroup);
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
	 * Method is used to rename the processed files using given file rename format
	 * 
	 * @param fileList
	 *            Files list.
	 * @param fileRenameFormat
	 *            The file rename format
	 * @return String which contains JSON string of the give file groups
	 * @throws IOException
	 * @throws JSONException
	 * @throws FS2Exception
	 * @throws URISyntaxException
	 */
	public void markAsSweeped(List<FileAttributesDTO> fileList, String fileRenameFormat) throws IOException,
			JSONException, FS2Exception, URISyntaxException {

		for (FileAttributesDTO file : fileList) {

			if (file.getFilePath().startsWith("fs2:")) {

				FlexibleStorageSystem FS2 = FS2InstanceCreator.getFS2Instance();
				FS2MetaSnapshot old = FS2.fetchObject(new URI(file.getFilePath()));
				URI uri = new URI(old + fileRenameFormat);
				FS2.move(old.getURI(), uri);
				LOGGER.info("Renaming the processed files");
				file.setFilePath(uri.toString());

			} else {

				Path oldPath = new File(file.getFilePath()).toPath();
				Path newPath = oldPath.getParent().resolve(oldPath.toFile().getName() + fileRenameFormat);
				move(oldPath, newPath);
				LOGGER.info("Renaming the processed files");
				file.setFilePath(newPath.toFile().getAbsolutePath());
			}

		}

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
		Files.move(file, target, StandardCopyOption.ATOMIC_MOVE);
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
	 */
	private String constructMetaDataJson(List<FileAttributesDTO> files)
			throws JsonGenerationException, JsonMappingException,
			IOException {

		List<List<FileAttributesDTO>> fileGroups = new ArrayList<>();
		fileGroups.add(files);
		MetaDataDTO meta = new MetaDataDTO();
		meta.setMetaData(fileGroups);

		String jsonResponse = MailBoxServiceUtil.convertObjectToJson(meta);
		return jsonResponse;
	}

}
