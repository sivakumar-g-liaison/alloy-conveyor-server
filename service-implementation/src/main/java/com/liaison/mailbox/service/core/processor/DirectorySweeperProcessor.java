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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.commons.util.client.http.HTTPStringData;
import com.liaison.fs2.api.FS2Exception;
import com.liaison.fs2.api.FS2MetaSnapshot;
import com.liaison.fs2.api.FlexibleStorageSystem;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.dto.configuration.PropertyDTO;
import com.liaison.mailbox.service.dto.directorysweeper.FileAttributesDTO;
import com.liaison.mailbox.service.dto.directorysweeper.MetaDataDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.FS2InstanceCreator;
import com.liaison.mailbox.service.util.HTTPStringOutputStream;
import com.liaison.mailbox.service.util.MailBoxServiceUtil;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * DirectorySweeper
 * 
 * <P>
 * DirectorySweeper sweeps the files from mail box and creates meta data about file and post it to
 * the rest service.
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
				Object obj = inv.invokeFunction("init", this);
				System.out.println(obj.toString());

			} else {
				// Directory Sweeper executed through Java
				executeRequest();
			}

		} catch (Exception e) {

			modifyProcessorExecutionStatus();
			e.printStackTrace();
			// TODO Re stage and update status in FSM
		}

	}

	private void executeRequest() throws IOException, JSONException, NoSuchMethodException, ScriptException, URISyntaxException,
			MailBoxServicesException, FS2Exception, LiaisonException {

		// Get root from folders input_folder
		String inputLocation = getPayloadURI();
		String fileRenameFormat = getPropertyValue(MailBoxConstants.FILE_RENAME_FORMAT_PROP_NAME);
		String url = getPropertyValue(MailBoxConstants.URL);
		if (MailBoxUtility.isEmpty(inputLocation)) {
			throw new MailBoxServicesException("The given input location is not available in the system.");
		}
		if (null == fileRenameFormat) {
			fileRenameFormat = MailBoxConstants.SWEEPED_FILE_EXTN;
		}

		// Sweeps the directory and constructs the list of file attributes dto.
		List<FileAttributesDTO> files = sweepDirectory(inputLocation, false, false, null);

		// Read from mailbox property - grouping js location
		String groupingJsPath = getPropertyValue(MailBoxConstants.GROUPING_JS_PROP_NAME);
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

		}

		markAsSweeped(files, fileRenameFormat);

		String jsonResponse = constructMetaDataJson(files);

		// TODO Need to send the constructed json to rest service
		LOGGER.info("Returns json response.{}", new JSONObject(jsonResponse).toString(2));

		postToRestService(jsonResponse, url);
	}

	/**
	 * Dummy method to post meta json to rest service.
	 * 
	 * @param input
	 * @param restUrl
	 * @throws LiaisonException
	 * @throws MalformedURLException
	 */
	private void postToRestService(String input, String restUrl) throws LiaisonException, MalformedURLException {

		URL url = new URL(restUrl);
		HTTPRequest request = new HTTPRequest(HTTP_METHOD.POST, url, LOGGER);
		request.setSocketTimeout(60000);
		request.addHeader("Content-Type", "application/json");
		HTTPStringOutputStream output = new HTTPStringOutputStream();
		request.setOutputStream(output);
		if (input != null) {
			request.inputData(new HTTPStringData(input));
		}
		request.execute();

		String response = output.toString();
		if (Messages.SUCCESS.value().equals(response)) {
			LOGGER.info("Successfully posted to the rest listener.");
		} else {
			LOGGER.info("Failed to post to the rest listener.");
		}

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

		FlexibleStorageSystem FS2 = FS2InstanceCreator.getFS2Instance();
		URI uri = null;
		Path newPath = null;
		Path oldPath = null;

		for (FileAttributesDTO file : fileList) {

			if (file.getFilePath().startsWith("fs2:")) {

				FS2MetaSnapshot old = FS2.fetchObject(new URI(file.getFilePath()));
				uri = new URI(old + fileRenameFormat);
				FS2.move(old.getURI(), uri);
				LOGGER.info("Renaming the processed files");
				file.setFilePath(uri.toString());

			} else {

				oldPath = new File(file.getFilePath()).toPath();
				newPath = oldPath.getParent().resolve(oldPath.toFile().getName() + fileRenameFormat);
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

	/**
	 * Method is used to get the property value by give name for mailbox.
	 * 
	 * @param propName
	 *            The property name
	 * @return propertyValue The property value
	 * @throws MailBoxServicesException
	 */
	private String getPropertyValue(String propName) throws MailBoxServicesException {

		for (PropertyDTO prop : getMailBoxProperties()) {
			if (propName.equals(prop.getName())) {
				return prop.getValue();
			}
		}

		return null;
	}

}
