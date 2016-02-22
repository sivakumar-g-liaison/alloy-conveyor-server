/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.integration.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.base.test.InitInitialDualDBContext;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.core.ProfileConfigurationService;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.dto.configuration.FolderDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorLegacyDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 *
 * @author OFS
 *
 */
public class ProcessorConfigurationServiceLegacyIT extends BaseServiceTest {

	private String serviceInstanceId = "5D9C3B487184426E9F9629EFEE7C5913";
    private String aclManifest = "H4sIAAAAAAAAAO1YbW/aMBD+K5U/TqRNokACn5aW0EUroaLRJrWqIpMckVfHjpwQlVb973NeKKBWXcdWEVV8QbLvfPfwnO8ewyMCVgDlKaDBIwoF4ByiofxAA6SrWldR+4qq+7o6MPoD1Tg2e8Y16qCY8hmmboQGbEFpB6VYAMs31oKHkGXrjUUGolyhguEYC/wLs6+UYJJxdhxBgWqPERFZ7uEENo9d4O29BuTp0k5TSkKcE85q25NMTHE+5yJBg5vH50V9Gp3rMo3gFE5xBpEdlgjPOMvlVuUe8QQT9uwcDJ0fgev5wWR6Lg/WVn9ZMoXklu2517bvTrxnm8tyEAzTlxHGE8/97kyb9DKZNNrDseuh25IrUhAKMVQgBGR8IcIywJfSv1k2eaeQ5dOVx9pafgu4z6WD5KsgISgzwe9ASJcUREKyrOJIhi8wXaxir01N9J/fXN+5cK989HT71PlnLGXxEizrDYm8HPvFEuuyQnTG7xuC9ovmDpZKW5iJcI4lmDTd93WpZwqUTSRbIoOaoD2DSihNlZCSvZepAlJe3v/JyEnI2ZzEJ7Hgi3QHUCAEFwrjOZmvBvHHYGNypGZ7B1hB3FKJIST8aCJizMhDFV7bRSlGPVMdmYap2n1dNcyRZmjGmTOybN1ynJ7R3dCNAkDgoFq9JR3bo7ehciEqiLvw+N5RXvJxuTa9TWjn/dxvuF4600A3Jmd+oKraDiUb1zoQlDWIxepO/HXNXg+zKtMr1sCOEsI+teC3SdbaJfhtegp9ZsF/2e6nE8f1dnq/1ydfebs3ho95wLfr3h6my6Gf29XPrh/4Ekgof8LrvaMrYBGIrRfYTmJOGpEec0bkRVdCs6tapmYq4XxmKIY615SZZYZKV7Ow1Y90sGawMRH+COog/gfxbwUzn3tYyP5iMjFEV3Xdh5CWvcjCZfU/n2x8mfqm9PwNJYKk5vgUAAA=";
    private String ftpURL = "ftp://10.146.18.10:21";
    private String ftpsURL = "ftps://10.146.18.15:21";
    private String sftpURL = "sftp://10.146.18.20:22";
    private String password = "5E701F715FDE4162938F413FECF53910";
    private String ftpUserId = "ftp_user_dev-int";
    private String ftpsUserId = "ftps_user_dev-int";
    private String sftpUserId = "sftp_user_dev-int";
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
    	System.setProperty("com.liaison.secure.properties.path", "invalid");
        System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
        System.setProperty("archaius.deployment.environment", "test");
        InitInitialDualDBContext.init();
    }

    /**
     * Method constructs Processor with valid data.
     * @throws MailBoxConfigurationServicesException
     *
     * @throws Exception
     */
    @Test(enabled=false)
	public void testCreateandReadProcessorUsingPguid() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        
        //Adding Profile
  		AddProfileRequestDTO profileRequestDTO = new AddProfileRequestDTO();
  		ProfileDTO profileDTO = constructDummyProfileDTO(System.currentTimeMillis());
  		profileRequestDTO.setProfile(profileDTO);
  		
  		ProfileConfigurationService profileService = new ProfileConfigurationService();
  		AddProfileResponseDTO profileResponse = profileService.createProfile(profileRequestDTO);
  		
  		Assert.assertEquals(SUCCESS, profileResponse.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = MailBoxUtil.unmarshalFromJSON(ServiceUtils.readFileFromClassPath("requests/processor/create_processor_legacy.json"), AddProcessorToMailboxRequestDTO.class);
        procRequestDTO.getProcessorLegacy().setLinkedMailboxId(response.getMailBox().getGuid());
        
        List<String> profiles = new ArrayList<String>();
        profiles.add(profileDTO.getName());
        procRequestDTO.getProcessorLegacy().setLinkedProfiles(profiles);
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId);

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessorLegacy().getName(), procGetResponseDTO.getProcessor().getName());
        Assert.assertEquals(procRequestDTO.getProcessorLegacy().getStatus(), procGetResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessorLegacy().getType(), procGetResponseDTO.getProcessor().getType());
        Assert.assertEquals(procRequestDTO.getProcessorLegacy().getProtocol(), procGetResponseDTO.getProcessor().getProtocol());

    }

    /**
     * @throws MailBoxConfigurationServicesException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     */
    @Test(enabled=false)
    public void testCreateAndReviseProcessor() throws MailBoxConfigurationServicesException, JsonParseException, JsonMappingException, JAXBException, IOException {
    	
    	// Adding Mailbox
    	AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
    	MailBoxDTO mailboxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
    	requestDTO.setMailBox(mailboxDTO);
    	
    	MailBoxConfigurationService service = new MailBoxConfigurationService();
    	AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);
    	
    	Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
    	String mailboxId = response.getMailBox().getGuid();
    	
    	// Adding Processor
    	AddProcessorToMailboxRequestDTO processorCreateRequestDTO = new AddProcessorToMailboxRequestDTO();
    	ProcessorLegacyDTO processorLegacy = constructLegacyProcessorDTO(ProcessorType.REMOTEDOWNLOADER.getCode(), Protocol.FTP.name(), EntityStatus.ACTIVE.value());
    	processorLegacy.setLinkedMailboxId(mailboxId);
    	processorCreateRequestDTO.setProcessorLegacy(processorLegacy);
    	ProcessorConfigurationService processorService = new ProcessorConfigurationService();
    	AddProcessorToMailboxResponseDTO processorResponse = processorService.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId);
    	Assert.assertEquals(SUCCESS, processorResponse.getResponse().getStatus());
    	
    	// revise Processor
    	ReviseProcessorRequestDTO processorReviseRequestDTO = new ReviseProcessorRequestDTO();
    	processorLegacy.setGuid(processorResponse.getProcessor().getGuId());
    	processorLegacy.setDescription("description modified");
    	processorLegacy.setName("Processor Modified" + System.currentTimeMillis());
    	//processorLegacy.setStatus(EntityStatus.INACTIVE.value());
    	
    	processorLegacy.getRemoteProcessorProperties().setConnectionTimeout(60000);
    	processorLegacy.getRemoteProcessorProperties().setRetryAttempts(3);
    	processorReviseRequestDTO.setProcessorLegacy(processorLegacy);
    	ReviseProcessorResponseDTO processorReviseResponse =  processorService.reviseProcessor(processorReviseRequestDTO, response.getMailBox().getGuid(), processorResponse.getProcessor().getGuId());
    	Assert.assertEquals(SUCCESS, processorReviseResponse.getResponse().getStatus());
    	
    	// read Processor
    	GetProcessorResponseDTO processorReadResponse = processorService.getProcessor(processorReviseResponse.getProcessor().getGuId());
    	Assert.assertEquals(SUCCESS, processorReadResponse.getResponse().getStatus());
    	Assert.assertEquals(processorLegacy.getDescription(), processorReadResponse.getProcessor().getDescription());
    	Assert.assertEquals(processorLegacy.getName(), processorReadResponse.getProcessor().getName());
    	Assert.assertEquals(processorLegacy.getStatus(), processorReadResponse.getProcessor().getStatus());
    }
    
    /**
     * Method to create sweeper processor with valid data.
     * 
     * @throws MailBoxConfigurationServicesException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JAXBException
     * @throws IOException
     */
    @Test
    public void testCreateSweeperProcessor() throws MailBoxConfigurationServicesException, JsonParseException, JsonMappingException, JAXBException, IOException {
        
        // Adding Mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mailboxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mailboxDTO);
        
        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);
        
        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
        String mailboxId = response.getMailBox().getGuid();
        
        // Adding Processor
        AddProcessorToMailboxRequestDTO processorCreateRequestDTO = new AddProcessorToMailboxRequestDTO();
        ProcessorLegacyDTO processorLegacy = constructLegacyProcessorDTO(ProcessorType.SWEEPER.getCode(), Protocol.SWEEPER.name(), EntityStatus.ACTIVE.value());
        processorLegacy.setLinkedMailboxId(mailboxId);
        processorCreateRequestDTO.setProcessorLegacy(processorLegacy);
        ProcessorConfigurationService processorService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO processorResponse = processorService.createProcessor(mailboxId, processorCreateRequestDTO, serviceInstanceId);
        Assert.assertEquals(SUCCESS, processorResponse.getResponse().getStatus());
        
    }
    
    private ProcessorLegacyDTO constructLegacyProcessorDTO(String type, String protocol, String status) {
    	
    	ProcessorLegacyDTO processorLegacyDTO = new ProcessorLegacyDTO();
    	processorLegacyDTO.setName("Processor" + System.currentTimeMillis());
    	processorLegacyDTO.setType(type);
    	processorLegacyDTO.setProtocol(protocol);
    	processorLegacyDTO.setStatus(status);
    	processorLegacyDTO.setDescription("test description");
    	processorLegacyDTO.setFolders(setFolderDetails(protocol, type));
    	processorLegacyDTO.setCredentials(setCredentialDetails(protocol));
    	processorLegacyDTO.setLinkedProfiles(setLinkedProfileDetails());
    	processorLegacyDTO.setRemoteProcessorProperties(constructLegacyProperties(type, protocol));
    	return processorLegacyDTO;
    	
    }
    
    private RemoteProcessorPropertiesDTO constructLegacyProperties(String type, String protocol) {
    	
    	RemoteProcessorPropertiesDTO legacyProperties = new RemoteProcessorPropertiesDTO();
    	switch(protocol.toLowerCase()) {
    	
    	case "ftp":
        	legacyProperties.setUrl(ftpURL);
        	legacyProperties.setPort(21);
    		break;
    	case "ftps":
    		legacyProperties.setUrl(ftpsURL);
    		legacyProperties.setPort(22);
    		legacyProperties.setPassive(true);
    		break;
    	case "sftp":
    		legacyProperties.setUrl(sftpURL);
    		legacyProperties.setPort(22);
    		break;
    	case "sweeper":
    		legacyProperties.setDeleteFileAfterSweep(true);
    		legacyProperties.setPipeLineID("pipeline" + System.currentTimeMillis());
    	}
    	return legacyProperties;
    }
    
    private FolderDTO constructDummyFolderDTO(String folderType, String folderURI) {
    	FolderDTO folderDTO = new FolderDTO();
    	folderDTO.setFolderType(folderType);
    	folderDTO.setFolderURI(folderURI);
    	folderDTO.setFolderDesc("Test Description"); 
    	return folderDTO;
    }
    
    private Set<FolderDTO> setFolderDetails(String protocol, String type) {
    	
    	Set<FolderDTO> folders = new HashSet<>();
    	switch(protocol.toLowerCase()) {
    		
    		case "ftp":
    			
    			if (type.equals(ProcessorType.REMOTEDOWNLOADER.getCode())) {
    				
        			folders.add(constructDummyFolderDTO("PAYLOAD_LOCATION", "/inbox"));
        			folders.add(constructDummyFolderDTO("RESPONSE_LOCATION", "/data/ftp/ftp_user_dev-int/outbox" ));
    			} else if (type.equals(ProcessorType.REMOTEUPLOADER.getCode())) {
    				
        			folders.add(constructDummyFolderDTO("PAYLOAD_LOCATION", "/data/ftp/ftp_user_dev-int/outbox"));
        			folders.add(constructDummyFolderDTO("RESPONSE_LOCATION", "/inbox" ));
    			}  			
    			break;
    		case "ftps":
    			
    			if (type.equals(ProcessorType.REMOTEDOWNLOADER.getCode())) {
    				
        			folders.add(constructDummyFolderDTO("PAYLOAD_LOCATION", "/inbox"));
        			folders.add(constructDummyFolderDTO("RESPONSE_LOCATION", "/data/ftps/ftps_user_dev-int/outbox" ));
    			} else if (type.equals(ProcessorType.REMOTEUPLOADER.getCode())) {
    				
        			folders.add(constructDummyFolderDTO("PAYLOAD_LOCATION", "/data/ftps/ftps_user_dev-int/outbox"));
        			folders.add(constructDummyFolderDTO("RESPONSE_LOCATION", "/inbox" ));
    			}  			
    			break;
    		case "sftp":
    			
    			if (type.equals(ProcessorType.REMOTEDOWNLOADER.getCode())) {
    				
        			folders.add(constructDummyFolderDTO("PAYLOAD_LOCATION", "/inbox"));
        			folders.add(constructDummyFolderDTO("RESPONSE_LOCATION", "/data/sftp/sftp_user_dev-int/outbox" ));
    			} else if (type.equals(ProcessorType.REMOTEUPLOADER.getCode())) {
    				
        			folders.add(constructDummyFolderDTO("PAYLOAD_LOCATION", "/data/sftp/sftp_user_dev-int/outbox"));
        			folders.add(constructDummyFolderDTO("RESPONSE_LOCATION", "/inbox" ));
    			}  			
    			break;
    		case "sweeper":
        		folders.add(constructDummyFolderDTO("PAYLOAD_LOCATION", "/data/ftp/ftp_user_dev-int/inbox"));
        		break;
    	   	case "filewriter":
        		folders.add(constructDummyFolderDTO("FILE_WRITE_LOCATIN", "/data/ftp/ftp_user_dev-int/inbox"));
        		break;
    	}
    	return folders;
    }
    
    private Set<CredentialDTO> setCredentialDetails(String protocol) {
    	
    	Set<CredentialDTO> credentials = new HashSet<>();
    	String credentialType = CredentialType.LOGIN_CREDENTIAL.getCode(); 
    	switch(protocol.toLowerCase()) {
		
		case "ftp":
			credentials.add(constructDummyCredentialDTO(ftpUserId, password, credentialType, null));
			break;
		case "ftps":
			credentials.add(constructDummyCredentialDTO(ftpsUserId, password, credentialType, null));
			break;
		case "sftp":
			credentials.add(constructDummyCredentialDTO(sftpUserId, password, credentialType, null));
			break;
    	}
    	return credentials;
    }
    
    private CredentialDTO constructDummyCredentialDTO(String userId, String password, String credentialType, String idpURI) {
    	
    	CredentialDTO credentail = new CredentialDTO();
    	credentail.setUserId(userId);
    	credentail.setPassword(password);
    	credentail.setCredentialType(credentialType);
    	credentail.setIdpURI(idpURI);
    	return credentail;
    	
    }
    
    private List<String> setLinkedProfileDetails() {
    	
    	List<String> profiles = new ArrayList<>();
    	
    	// create a profile
    	AddProfileRequestDTO profileRequest = new AddProfileRequestDTO();
    	ProfileDTO profile = constructDummyProfileDTO(System.currentTimeMillis());
    	profileRequest.setProfile(profile);
    	ProfileConfigurationService profileService = new ProfileConfigurationService();
    	AddProfileResponseDTO response = profileService.createProfile(profileRequest);
    	
    	Assert.assertEquals(SUCCESS, response.getResponse().getStatus());
    	
    	profiles.add(profile.getName());
    	return profiles;
    	
    }
    
}
        