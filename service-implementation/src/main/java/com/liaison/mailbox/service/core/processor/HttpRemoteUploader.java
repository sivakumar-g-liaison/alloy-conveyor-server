package com.liaison.mailbox.service.core.processor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;
import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.fs2.api.FS2Exception;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * Http remote uploader to perform push operation, also it has support methods for JavaScript.
 * 
 * @author veerasamyn
 */
public class HttpRemoteUploader extends AbstractRemoteProcessor implements MailBoxProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpRemoteUploader.class);

	@SuppressWarnings("unused")
	private HttpRemoteUploader() {
		// to force creation of instance only by passing the processor entity
	}

	public HttpRemoteUploader(Processor configurationInstance) {
		super(configurationInstance);
	}

	/**
	 * Java method to execute the HTTPRequest and write in FS location
	 * 
	 * @throws MailBoxServicesException
	 * @throws FS2Exception
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws URISyntaxException
	 * @throws JAXBException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws SymmetricAlgorithmException
	 * @throws JSONException 
	 * @throws JsonParseException 
	 * 
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	public void executeRequest() throws MailBoxServicesException, LiaisonException, IOException, FS2Exception,
			URISyntaxException, JAXBException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
			SymmetricAlgorithmException, JsonParseException, JSONException {

		HTTPRequest request = (HTTPRequest) getClientWithInjectedConfiguration();
		ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
		request.setOutputStream(responseStream);

		// Set the pay load value to http client input data for POST & PUT
		// request
		File[] files = null;
		if ("POST".equals(request.getMethod()) || "PUT".equals(request.getMethod())) {

			files = getProcessorPayload();
			if (null != files) {

				StringBuffer buffer = new StringBuffer();
				for (File entry : files) {
					String content = FileUtils.readFileToString(entry, "UTF-8");
					buffer.append(content);
				}
				if (buffer.length() > 0) {
					request.inputData(buffer.toString());
				}
			}
		}

		HTTPResponse response = request.execute();
		LOGGER.info("The reponse code recived is {} ", response.getStatusCode());
		if (response.getStatusCode() != 200) {
			LOGGER.info("The reponse code recived is {} ", response.getStatusCode());
			throw new MailBoxServicesException(Messages.HTTP_REQUEST_FAILED);
		} else {

			if (null != files) {

				String processedFileLcoation = getDynamicProperties().getProperty(MailBoxConstants.PROCESSED_FILE_LOCATION);
				if (MailBoxUtility.isEmpty(processedFileLcoation)) {
					archiveFiles(files);
				} else {
					archiveFiles(files, processedFileLcoation);
				}
			}
		}
	}

	@Override
	public void invoke() {

		try {

			// HTTPRequest executed through JavaScript
			if (!MailBoxUtility.isEmpty(configurationInstance.getJavaScriptUri())) {

				ScriptEngineManager manager = new ScriptEngineManager();
				ScriptEngine engine = manager.getEngineByName("JavaScript");

				engine.eval(getJavaScriptString(configurationInstance.getJavaScriptUri()));
				Invocable inv = (Invocable) engine;

				// invoke the method in javascript
				inv.invokeFunction("init", this);
				// System.out.println(obj.toString());

			} else {
				// HTTPRequest executed through Java
				executeRequest();
			}
			modifyProcessorExecutionStatus(ExecutionStatus.COMPLETED);
		} catch (Exception e) {

			modifyProcessorExecutionStatus(ExecutionStatus.FAILED);
			sendEmail(null, configurationInstance.getProcsrName() + ":" + e.getMessage(), e, "HTML");
			e.printStackTrace();
			// TODO Re stage and update status in FSM
		}
	}
}
