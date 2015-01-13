package com.liaison.mailbox.service.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.fs2.api.FS2MetaSnapshot;
import com.liaison.fs2.api.FS2ObjectHeaders;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.DropBoxProcessor;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorDTO;
import com.liaison.mailbox.service.dto.configuration.ProfileDTO;
import com.liaison.mailbox.service.dto.configuration.TenancyKeyDTO;
import com.liaison.mailbox.service.dto.configuration.response.DropboxFileTransferResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetTransferProfilesResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.queue.sender.SweeperQueue;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;

public class DropboxFileTransferService {

	private static final Logger LOG = LogManager.getLogger(DropboxFileTransferService.class);
	
	public static final String TRANSFER_PROFILE = "Tranfer Profile";
	
	public DropboxFileTransferResponseDTO uploadContentAsyncToSpectrum(HttpServletRequest request, String profileId) throws Exception {
		
		LOG.debug("Entering into uploadContentAsyncToSpectrum service.");
		DropboxFileTransferResponseDTO serviceResponse = new DropboxFileTransferResponseDTO();
		
		try {
			// retrieve token from request
			String authenticationToken = request.getHeader(MailBoxConstants.AUTH_TOKEN);
			
			// validate token
			authenticationToken = validateToken(authenticationToken);
			
			// retrieve acl manifest from request
			String aclManifest = request.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER);
			
			String tenancyKey = null;
			// retrieve the tenancy key from acl manifest
			List<TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifest);
			if (tenancyKeys.isEmpty()) {
				LOG.error("retrieval of tenancy key from acl manifest failed");
				throw new MailBoxServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED, Response.Status.BAD_REQUEST);
			} else {
				tenancyKey = tenancyKeys.get(0).getGuid();
			}	
			
			List <String> specificProcessorTypes = new ArrayList<String>();
			specificProcessorTypes.add(DropBoxProcessor.class.getCanonicalName());
			ProcessorConfigurationDAO processorDAO = new ProcessorConfigurationDAOBase();
			List <Processor> processors = processorDAO.findProcessorsOfSpecificTypeByProfileAndTenancyKey(profileId, tenancyKey, specificProcessorTypes);
			
			if (processors.isEmpty()) {
				//TODO throw exception if no processors are available.
				LOG.error("There are no processors available for the profile id - {} with tenancykey - {}", profileId, tenancyKey);
			}
			ProcessorDTO processorDTO = null;
			for (Processor processor : processors) {
				
				processorDTO = new ProcessorDTO();
				processorDTO.copyFromEntity(processor);
				
				// retrieving the httplistener pipeline id from remote processor properties
				String pipeLineId = processorDTO.getRemoteProcessorProperties().getHttpListenerPipeLineId();
				boolean isSecuredPayload = processorDTO.getRemoteProcessorProperties().isSecuredPayload();
				String mailboxPguid = processor.getMailbox().getPguid();
			
				generateWorkTicketAndPostToQueue(request, mailboxPguid, isSecuredPayload, pipeLineId);	
			}
			
			// response message construction
			
			LOG.debug("Exit from uploadContentAsyncToSpectrum service.");
			return serviceResponse;
				
			
		} catch (MailBoxServicesException e) {
			LOG.error(Messages.FILE_TRANSFER_FAILED.name(), e);
			serviceResponse.setResponse(new ResponseDTO(Messages.FILE_TRANSFER_FAILED, null, Messages.FAILURE, e
					.getMessage()));
			return serviceResponse;
		}
		
	}
	
	public GetTransferProfilesResponseDTO getTransferProfiles(HttpServletRequest request) {
		
		GetTransferProfilesResponseDTO serviceResponse = new GetTransferProfilesResponseDTO();
		List <ProfileDTO> transferProfiles = new ArrayList<ProfileDTO>();
		ProfileDTO transferProfile = new ProfileDTO();
		transferProfile.setId("Dummy Profile id");
		transferProfile.setName("Dummy Profile Name");
		transferProfiles.add(transferProfile);
		serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, TRANSFER_PROFILE, Messages.SUCCESS));
		serviceResponse.setTranferProfiles(transferProfiles);
		return serviceResponse;
	}
	
	private void generateWorkTicketAndPostToQueue(HttpServletRequest request, String mailboxPguid, boolean isSecurePayload, String pipeLineId) throws Exception {
		
		// generate workticket
		WorkTicket workTicket = createWorkTicket(request, mailboxPguid, pipeLineId);
		//store payload to spectrum
		storePayload(request, workTicket, isSecurePayload);		
		constructMetaDataJson(workTicket);
	}
	
	private String validateToken(String authenticationToken) {
		String newAuthenticationToken = "test token";
		return newAuthenticationToken;
	}
	
	private WorkTicket createWorkTicket(HttpServletRequest request, String mailboxPguid, String pipeLineId) {
		
		LOG.debug("Generating workticket for Mailbox - {}", mailboxPguid);
		WorkTicket workTicket = new WorkTicket();
		workTicket.setAdditionalContext("httpMethod", request.getMethod());
		workTicket.setAdditionalContext("httpQueryString", request.getQueryString());
		workTicket.setAdditionalContext("httpRemotePort", request.getRemotePort());
		workTicket.setAdditionalContext("httpCharacterEncoding", (request.getCharacterEncoding() != null ? request.getCharacterEncoding() : ""));
		workTicket.setAdditionalContext("httpRemoteUser", (request.getRemoteUser() != null ? request.getRemoteUser() : "unknown-user"));
		workTicket.setAdditionalContext("mailboxId", mailboxPguid);
		workTicket.setAdditionalContext("httpRemoteAddress", request.getRemoteAddr());
		workTicket.setAdditionalContext("httpRequestPath", request.getRequestURL().toString());
		workTicket.setAdditionalContext("httpContentType", request.getContentType());
		workTicket.setProcessMode(ProcessMode.ASYNC);
		workTicket.setPipelineId(pipeLineId);
		workTicket.setGlobalProcessId(MailBoxUtil.getGUID());
		workTicket.setCreatedTime(new Date());
		copyRequestHeadersToWorkTicket(request, workTicket);
		return workTicket;
		
	}
	
	/**
	 * Method to construct FS2ObjectHeaders from the given workTicket
	 *
	 * @param workTicket
	 * @return FS2ObjectHeaders
	 * @throws IOException
	 * @throws MailBoxServicesException
	 */
	// TODO : This can be moved to a separate workticket util
	private FS2ObjectHeaders constructFS2Headers(WorkTicket workTicket) {

		FS2ObjectHeaders fs2Header = new FS2ObjectHeaders();
		fs2Header.addHeader(MailBoxConstants.KEY_GLOBAL_PROCESS_ID, workTicket.getGlobalProcessId());
		fs2Header.addHeader(MailBoxConstants.KEY_PIPELINE_ID, workTicket.getPipelineId());
		fs2Header.addHeader(MailBoxConstants.KEY_TENANCY_KEY, (MailBoxConstants.PIPELINE_FULLY_QUALIFIED_PACKAGE + ":" + workTicket.getPipelineId()));
		LOG.debug("FS2 Headers set are {}", fs2Header.getHeaders());
		return fs2Header;
	}
	
	// TODO : This can be moved to a separate workticket util
	private void copyRequestHeadersToWorkTicket (HttpServletRequest request , WorkTicket workTicket)	{

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements())
		{
			String headerName = headerNames.nextElement();
			List<String> headerValues = new ArrayList<>();
			Enumeration<String> values = request.getHeaders(headerName);

			while (values.hasMoreElements())
			{
				headerValues.add(values.nextElement());
			}

			workTicket.addHeaders(headerName,  headerValues);
		}

	}
	
	/**
	 * This method will persist payload in spectrum.
	 *
	 * @param request
	 * @param workTicket
	 * @throws IOException
	 */
	// TODO : This can be moved to a separate workticket util
	protected void storePayload(HttpServletRequest request,
			WorkTicket workTicket, boolean isSecuredPayload) throws Exception {

	  try (InputStream payloadToPersist = request.getInputStream()) {

              FS2ObjectHeaders fs2Header = constructFS2Headers(workTicket);
              FS2MetaSnapshot metaSnapShot = StorageUtilities.persistPayload(payloadToPersist, workTicket.getGlobalProcessId(),
                            fs2Header, isSecuredPayload);
              LOG.info("The received path uri is {} ", metaSnapShot.getURI().toString());
              //Hack
              workTicket.setPayloadSize(Long.valueOf(metaSnapShot.getHeader(MailBoxConstants.KEY_RAW_PAYLOAD_SIZE)[0]));
              workTicket.setPayloadURI(metaSnapShot.getURI().toString());
	    }
	}
	
	protected void constructMetaDataJson(WorkTicket workTicket) throws Exception {
		String workTicketJson = JAXBUtility.marshalToJSON(workTicket);
		postToQueue(workTicketJson);
	}

	protected void postToQueue(String message) throws Exception {
        SweeperQueue.getInstance().sendMessages(message);
        LOG.debug("DropBoxUPloadAsync postToQueue, message: {}", message);

	}

}
