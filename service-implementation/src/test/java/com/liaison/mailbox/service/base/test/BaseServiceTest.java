/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.base.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPRequest.HTTP_METHOD;
import com.liaison.commons.util.client.http.HTTPStringData;
import com.liaison.dto.enums.EntityStatus;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.PropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.FolderValidationRulesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorCredentialPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorFolderPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertyDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertyUITemplateDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.ValidationRulesDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;

import javax.naming.NamingException;

/**
 * Base Test class for initial setup and cleanup.
 *
 * @author OFS
 *
 */
public abstract class BaseServiceTest {

	private ByteArrayOutputStream output;
	private static String BASE_URL;
	private static String KMS_BASE_URL;
    private static String BASE_URL_DROPBOX;

	public static final String SUCCESS = Messages.SUCCESS.value();
	public static final String FAILURE = Messages.FAILURE.value();
	public static final String STATUS = "status";
    public static final String MESSAGE = "message";

	public static String USER_ID = "demouserjan22@liaison.dev";
	public static String PASSWORD = "TG9yZDAyZ2FuZXNoIQ==";
	public String tenancyKey = "G2_DEV_INT_MONIKER";
	public String serviceInstanceId = "3A7D4AE638B14113818651F92E67FD16";
	public String spectrumUri = "fs2://secure@dev-int/mailbox/payload/1.0/21F9B154FB54495A855EAC63E1CDC69B";
	public String response = "response";

    protected String aclManifest = "H4sIAAAAAAAAAO1YbW/aMBD+K5U/TqRNokACn5aW0EUroaLRJrWqIpMckVfHjpwQlVb973NeKKBWXcdWEVV8QbLvfPfwnO8ewyMCVgDlKaDBIwoF4ByiofxAA6SrWldR+4qq+7o6MPoD1Tg2e8Y16qCY8hmmboQGbEFpB6VYAMs31oKHkGXrjUUGolyhguEYC/wLs6+UYJJxdhxBgWqPERFZ7uEENo9d4O29BuTp0k5TSkKcE85q25NMTHE+5yJBg5vH50V9Gp3rMo3gFE5xBpEdlgjPOMvlVuUe8QQT9uwcDJ0fgev5wWR6Lg/WVn9ZMoXklu2517bvTrxnm8tyEAzTlxHGE8/97kyb9DKZNNrDseuh25IrUhAKMVQgBGR8IcIywJfSv1k2eaeQ5dOVx9pafgu4z6WD5KsgISgzwe9ASJcUREKyrOJIhi8wXaxir01N9J/fXN+5cK989HT71PlnLGXxEizrDYm8HPvFEuuyQnTG7xuC9ovmDpZKW5iJcI4lmDTd93WpZwqUTSRbIoOaoD2DSihNlZCSvZepAlJe3v/JyEnI2ZzEJ7Hgi3QHUCAEFwrjOZmvBvHHYGNypGZ7B1hB3FKJIST8aCJizMhDFV7bRSlGPVMdmYap2n1dNcyRZmjGmTOybN1ynJ7R3dCNAkDgoFq9JR3bo7ehciEqiLvw+N5RXvJxuTa9TWjn/dxvuF4600A3Jmd+oKraDiUb1zoQlDWIxepO/HXNXg+zKtMr1sCOEsI+teC3SdbaJfhtegp9ZsF/2e6nE8f1dnq/1ydfebs3ho95wLfr3h6my6Gf29XPrh/4Ekgof8LrvaMrYBGIrRfYTmJOGpEec0bkRVdCs6tapmYq4XxmKIY615SZZYZKV7Ow1Y90sGawMRH+COog/gfxbwUzn3tYyP5iMjFEV3Xdh5CWvcjCZfU/n2x8mfqm9PwNJYKk5vgUAAA=";

	@BeforeClass
	public void initialSetUp() throws SQLException, NamingException, ClassNotFoundException {

		if (BASE_URL == null) {

			Properties prop = new Properties();
            try (InputStream is = new ByteArrayInputStream(ServiceUtils.readFileFromClassPath("config.properties").getBytes("UTF-8"))) {

                prop.load(is);

                setBASE_URL(prop.getProperty("BASE_URL"));
                setKMS_BASE_URL(prop.getProperty("KMS_BASE_URL"));
                setBASE_URL_DROPBOX(prop.getProperty("BASE_URL_DROPBOX"));
                System.setProperty("archaius.deployment.applicationId", prop.getProperty("APPLICATION_ID"));
                System.setProperty("archaius.deployment.environment", prop.getProperty("ENVIRONMENT"));
                System.setProperty("com.liaison.secure.properties.path", "invalid");
            } catch (Exception e) {

                setBASE_URL("http://localhost:8989/g2mailboxservice/config/mailbox");
                setKMS_BASE_URL("http://lsvlkms01d.liaison.dev:8989/key-management");
                setBASE_URL_DROPBOX("http://localhost:9095/g2mailboxservice/config/dropbox");
                System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
                System.setProperty("archaius.deployment.environment", "dev");
                System.setProperty("com.liaison.secure.properties.path", "invalid");
            }
        }

        InitInitialDualDBContext.init();

	}

	public ByteArrayOutputStream getOutput() {
		return output;
	}

	public void setOutput(ByteArrayOutputStream output) {
		this.output = output;
	}

	public String getBASE_URL() {
		return BASE_URL;
	}

	public void setBASE_URL(String bASE_URL) {
		BASE_URL = bASE_URL;
	}

	public static String getKMS_BASE_URL() {
		return KMS_BASE_URL;
	}

	public static void setKMS_BASE_URL(String kMS_BASE_URL) {
		KMS_BASE_URL = kMS_BASE_URL;
	}

	public static String getBASE_URL_DROPBOX() {
		return BASE_URL_DROPBOX;
	}

	public static void setBASE_URL_DROPBOX(String bASE_URL_DROPBOX) {
		BASE_URL_DROPBOX = bASE_URL_DROPBOX;
	}

	@AfterMethod
	public void finalCleanUp() {

	}

	/**
	 * Constructs HTTPRequest for integration tests.
	 *
	 * @param URL
	 *            The service URL
	 * @param method
	 *            HTTP verb
	 * @param input
	 *            The request JSON String
	 * @param logger
	 *            The logger
	 * @return HTTPRequest The HTTPRequest instance for the given URL and method
	 * @throws LiaisonException
	 * @throws IOException
	 */
	public HTTPRequest constructHTTPRequest(String URL, HTTP_METHOD method, String input, Logger logger)
			throws LiaisonException, IOException {

		URL url = new URL(URL);
		HTTPRequest request = new HTTPRequest(method, url);
		request.setLogger(logger);
		request.setSocketTimeout(60000);
		request.addHeader("Content-Type", "application/json");
		output = new ByteArrayOutputStream(4096);
		request.setOutputStream(output);
		if (input != null) {
			request.inputData(new HTTPStringData(input));
		}
		output.close();
		return request;
	}

    /**
     * Common method to get the response status and message from the response JSON String.
     *
     * @param responseString The JSON response String
     * @param serivceName The service object name
     * @param type response message type
     * @return String The status String
     * @throws JSONException
     */
    public String getResponse(String responseString, String serivceName, String type)
            throws JSONException {

        JSONObject rootJson = new JSONObject(responseString);
        JSONObject serviceJson = rootJson.getJSONObject(serivceName);
        JSONObject responseJson = serviceJson.getJSONObject(response);
        return responseJson.getString(type);
    }

	/**
	 * Method constructs the request JSON String into JSONObject.
	 *
	 * @param requestString
	 *            The requestJSON String.
	 * @param serivceName
	 *            The service object name
	 * @return {@link JSONObject}
	 * @throws JSONException
	 */
	public JSONObject getRequestJson(String requestString, String serivceName) throws JSONException {

		JSONObject rootJson = new JSONObject(requestString);
		JSONObject serviceJson = rootJson.getJSONObject(serivceName);
		return serviceJson;
	}

	/**
	 * Construct dummy mailbox DTO for testing.
	 *
	 * @param uniqueValue
	 * @return
	 */
	public MailBoxDTO constructDummyMailBoxDTO(Long uniqueValue, boolean isCreate) {

		MailBoxDTO mailBoxDTO = new MailBoxDTO();
		PropertyDTO property = new PropertyDTO();

		if (isCreate) {

			mailBoxDTO.setName("MBX_TEST" + uniqueValue);
			mailBoxDTO.setDescription("MBX_TEST_DESCRIPTION" + uniqueValue);
			mailBoxDTO.setShardKey("MBX_SHARD_KEY" + uniqueValue);
			mailBoxDTO.setTenancyKey(tenancyKey);
			mailBoxDTO.setStatus(EntityStatus.ACTIVE.name());
			mailBoxDTO.setModifiedBy("unknown-user");

			property.setName("MBX_SIZE");
			property.setValue("1024");

		} else {

			mailBoxDTO.setName("MBX_REV_TEST" + uniqueValue);
			mailBoxDTO.setDescription("MBX_REV_TEST_DESCRIPTION" + uniqueValue);
			mailBoxDTO.setShardKey("MBX_REV_SHARD_KEY" + uniqueValue);
			mailBoxDTO.setStatus(EntityStatus.ACTIVE.name());
			mailBoxDTO.setTenancyKey("MBX_TENANCY_KEY" + uniqueValue);
			mailBoxDTO.setModifiedBy("unknown-user");

			property.setName("MBX_REV_SIZE");
			property.setValue("1024");

		}

		mailBoxDTO.getProperties().add(property);
		return mailBoxDTO;
	}

    public ReviseProcessorRequestDTO constructReviseProcessorDTO(String guid, MailBoxDTO mbxDTO) throws IOException {

        ReviseProcessorRequestDTO procRequestDTO = new ReviseProcessorRequestDTO();
        ProcessorDTO procDTO = setProcessorDTO(guid, mbxDTO);
        constructProcessorProperties(procDTO);
        procRequestDTO.setProcessor(procDTO);
        return procRequestDTO;
    }

	/**
     * Construct dummy mailbox DTO for testing.
     *
     * @param uniqueValue
     * @return
	 * @throws IOException
     */
    public AddProcessorToMailboxRequestDTO constructDummyProcessorDTO(String mailboxGuid, MailBoxDTO mbxDTO) throws IOException {

        AddProcessorToMailboxRequestDTO procRequestDTO = new AddProcessorToMailboxRequestDTO();
        ProcessorDTO procDTO = setProcessorDTO(mailboxGuid, mbxDTO);
        constructProcessorProperties(procDTO);
        procRequestDTO.setProcessor(procDTO);
        return procRequestDTO;
    }

    private ProcessorDTO setProcessorDTO(String mailboxGuid, MailBoxDTO mbxDTO) {

        ProcessorDTO procDTO = new ProcessorDTO();
        procDTO.setMailboxName(mbxDTO.getName());
        procDTO.setLinkedMailboxId(mailboxGuid);
        procDTO.setName("testProcessor" + System.currentTimeMillis());
        procDTO.setStatus("ACTIVE");
        procDTO.setType("REMOTEDOWNLOADER");
        procDTO.setProtocol("FTP");
        procDTO.setModifiedBy("unknown-user");
        procDTO.setJavaScriptURI("ftp://test:6060");
        return procDTO;
    }

    /**
     * Construct dummy mailbox DTO for testing.
     *
     * @param uniqueValue
     * @return
     * @throws IOException
     */
    public AddProcessorToMailboxRequestDTO constructHttpProcessorDTO(String mailboxGuid, MailBoxDTO mbxDTO) throws IOException {

        AddProcessorToMailboxRequestDTO procRequestDTO = new AddProcessorToMailboxRequestDTO();
        ProcessorDTO procDTO = setHttpProcessorDTO(mailboxGuid, mbxDTO);
        constructHttpProcessorProperties(procDTO);
        procRequestDTO.setProcessor(procDTO);
        return procRequestDTO;
    }

    private ProcessorDTO setHttpProcessorDTO(String mailboxGuid, MailBoxDTO mbxDTO) {

        ProcessorDTO procDTO = new ProcessorDTO();
        procDTO.setMailboxName(mbxDTO.getName());
        procDTO.setLinkedMailboxId(mailboxGuid);
        procDTO.setName("testProcessor" + System.currentTimeMillis());
        procDTO.setStatus("ACTIVE");
        procDTO.setType("HTTPASYNCPROCESSOR");
        procDTO.setProtocol("HTTP");
        return procDTO;
    }

    private void constructProcessorProperties(ProcessorDTO procDTO) throws IOException {

        ProcessorPropertyUITemplateDTO propDTO = new ProcessorPropertyUITemplateDTO();
        List<ProcessorPropertyDTO> staticProperties = new ArrayList<ProcessorPropertyDTO>();
        List<ProcessorFolderPropertyDTO> folderProperties = new ArrayList<ProcessorFolderPropertyDTO>();
        List<ProcessorCredentialPropertyDTO> procCredentialPropDTO = new ArrayList<ProcessorCredentialPropertyDTO>();
        ValidationRulesDTO validationRules = setValidationRules();
        ProcessorPropertyDTO procURLPropDTO = setProcessorURLPropertyDTO(validationRules);
        ProcessorFolderPropertyDTO procFolderPropDTO = setProcessorFolderPropertyDTO();
        setProcessorCredentialPropertyDTO();
        folderProperties.add(procFolderPropDTO);
        staticProperties.add(procURLPropDTO);
        propDTO.setStaticProperties(staticProperties);
        propDTO.setFolderProperties(folderProperties);
        propDTO.setCredentialProperties(procCredentialPropDTO);
        procDTO.setProcessorPropertiesInTemplateJson(propDTO);
    }

    private void constructHttpProcessorProperties(ProcessorDTO procDTO) throws IOException {

        ProcessorPropertyUITemplateDTO propDTO = new ProcessorPropertyUITemplateDTO();
        List<ProcessorPropertyDTO> staticProperties = new ArrayList<ProcessorPropertyDTO>();
        List<ProcessorFolderPropertyDTO> folderProperties = new ArrayList<ProcessorFolderPropertyDTO>();
        List<ProcessorCredentialPropertyDTO> procCredentialPropDTO = new ArrayList<ProcessorCredentialPropertyDTO>();
        setValidationRules();
        ProcessorPropertyDTO procURLPropDTO = setProcessorHttpURLPropertyDTO();
        ProcessorFolderPropertyDTO procFolderPropDTO = setProcessorFolderPropertyDTO();
        setProcessorCredentialPropertyDTO();
        folderProperties.add(procFolderPropDTO);
        staticProperties.add(procURLPropDTO);
        propDTO.setStaticProperties(staticProperties);
        propDTO.setFolderProperties(folderProperties);
        propDTO.setCredentialProperties(procCredentialPropDTO);
        procDTO.setProcessorPropertiesInTemplateJson(propDTO);
    }

    private ProcessorPropertyDTO setProcessorHttpURLPropertyDTO() {

        List<String> options = new ArrayList<String>();
        options.add("false");
        options.add("true");
        ProcessorPropertyDTO procPropDTO = new ProcessorPropertyDTO();
        procPropDTO.setName("lensVisibility");
        procPropDTO.setDisplayName("LENS Visibility");
        procPropDTO.setType("select");
        procPropDTO.setValue("Invisible");
        procPropDTO.setMandatory(true);
        procPropDTO.setDynamic(false);
        procPropDTO.setValueProvided(false);
        procPropDTO.setDefaultValue("Invisible");
        procPropDTO.setValidationRules(null);
        procPropDTO.setOptions(options);
        return procPropDTO;
    }

    private ProcessorPropertyDTO setProcessorURLPropertyDTO(ValidationRulesDTO validationRules) {

        ProcessorPropertyDTO procPropDTO = new ProcessorPropertyDTO();
        procPropDTO.setName("url");
        procPropDTO.setDisplayName("URL");
        procPropDTO.setType("textarea");
        procPropDTO.setReadOnly(false);
        procPropDTO.setValue("20");
        procPropDTO.setMandatory(true);
        procPropDTO.setDynamic(false);
        procPropDTO.setValueProvided(true);
        procPropDTO.setDefaultValue("21");
        procPropDTO.setValidationRules(validationRules);
        return procPropDTO;
    }

    private ProcessorFolderPropertyDTO setProcessorFolderPropertyDTO() {

        ProcessorFolderPropertyDTO procPropDTO = new ProcessorFolderPropertyDTO();
        FolderValidationRulesDTO validationRules = setFolderValidationRulesDTO();
        procPropDTO.setFolderURI("ftp://test:6060");
        procPropDTO.setFolderDisplayType("Remote Payload Location");
        procPropDTO.setFolderType("PAYLOAD_LOCATION");
        procPropDTO.setFolderDesc("test folder desc");
        procPropDTO.setMandatory(false);
        procPropDTO.setReadOnly(false);
        procPropDTO.setValueProvided(true);
        procPropDTO.setValidationRules(validationRules);
        return procPropDTO;
    }

    private ProcessorCredentialPropertyDTO setProcessorCredentialPropertyDTO() {

        ProcessorCredentialPropertyDTO procCredentialPropDTO = new ProcessorCredentialPropertyDTO();
        procCredentialPropDTO.setCredentialURI("");
        procCredentialPropDTO.setCredentialType("LOGIN_CREDENTIAL");
        procCredentialPropDTO.setCredentialDisplayType("Login Credential");
        procCredentialPropDTO.setUserId("");
        procCredentialPropDTO.setPassword("");
        procCredentialPropDTO.setIdpType("");
        procCredentialPropDTO.setIdpURI("");
        procCredentialPropDTO.setValueProvided(false);
        return procCredentialPropDTO;
    }

    private FolderValidationRulesDTO setFolderValidationRulesDTO() {

        FolderValidationRulesDTO validationRules = new FolderValidationRulesDTO();
        validationRules.setFolderDescPattern("");
        validationRules.setFolderDescPattern("");
        validationRules.setMinLength("");
        validationRules.setMaxLength("");
        return validationRules;
    }

    private ValidationRulesDTO setValidationRules() {

        ValidationRulesDTO validationRules = new ValidationRulesDTO();
        validationRules.setPattern("");
        validationRules.setMaxLength("");
        validationRules.setMinLength("");
        return validationRules;
    }

    /**
     * Construct dummy profile DTO for testing.
     *
     * @param uniqueValue
     * @return profileDTO
     */
    public ProfileDTO constructDummyProfileDTO(Long uniqueValue) {

    	ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setName("PROFILE_TEST" + uniqueValue);
		return profileDTO;
    }
    
    /**
     * Method to construct folder properties.
     *
     * @param processorProperties
     * @param payloadLocation
     * @param targetLocation
     * @return ProcessorPropertyUITemplateDTO
     */
    public static ProcessorPropertyUITemplateDTO constructFolderProperties(
            ProcessorPropertyUITemplateDTO processorProperties, String payloadLocation, String targetLocation) {

        // constructing folderDTO
        List<ProcessorFolderPropertyDTO> folderList = new ArrayList<ProcessorFolderPropertyDTO>();
        ProcessorFolderPropertyDTO payloadFolderPropertyDto = new ProcessorFolderPropertyDTO();
        ProcessorFolderPropertyDTO responseFolderPropertyDto = new ProcessorFolderPropertyDTO();

        payloadFolderPropertyDto.setFolderType("PAYLOAD_LOCATION");
        payloadFolderPropertyDto.setFolderURI(targetLocation + System.nanoTime());
        payloadFolderPropertyDto.setFolderDesc("Payload Location");
        folderList.add(payloadFolderPropertyDto);

        responseFolderPropertyDto.setFolderType("RESPONSE_LOCATION");
        responseFolderPropertyDto.setFolderURI(payloadLocation + System.nanoTime());
        responseFolderPropertyDto.setFolderDesc("Response Location");
        folderList.add(responseFolderPropertyDto);

        processorProperties.setFolderProperties(folderList);
        return processorProperties;
    }

}
