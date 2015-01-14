package com.liaison.mailbox.service.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

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
import com.liaison.mailbox.service.dto.configuration.response.GetTransferProfilesResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.queue.sender.SweeperQueue;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;

public class DropboxFileTransferService {

	private static final Logger LOG = LogManager.getLogger(DropboxFileTransferService.class);
	
	public static final String TRANSFER_PROFILE = "Tranfer Profiles";
	
	public Response uploadContentAsyncToSpectrum(HttpServletRequest request, String profileId, Map<String, String>responseHeaders) throws Exception {
		
		LOG.debug("Entering into uploadContentAsyncToSpectrum service.");
		
		String aclManifest = responseHeaders.get(MailBoxConstants.ACL_MANIFEST_HEADER);
		String aclSignature =responseHeaders.get(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER);
		String aclSignerGuid =responseHeaders.get(MailBoxConstants.ACL_SIGNER_GUID_HEADER);
		String token = responseHeaders.get(MailBoxConstants.AUTH_TOKEN);
				
		String tenancyKey = null;
		LOG.info("Retrieving tenancy keys from acl-manifest");
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
			throw new MailBoxServicesException("There are no Dropbox Processor available", Response.Status.NOT_FOUND);
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
		ResponseBuilder builder = Response.ok().header(MailBoxConstants.ACL_MANIFEST_HEADER, aclManifest)
				.header(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER, aclSignature).header(MailBoxConstants.ACL_SIGNER_GUID_HEADER, aclSignerGuid)
				.header(MailBoxConstants.AUTH_TOKEN, token)
				.type(MediaType.TEXT_PLAIN)
				.entity("Files Successfully uploaded")
				.status(Response.Status.OK);
		LOG.debug("Exit from uploadContentAsyncToSpectrum service.");
		return builder.build();	
		
	}
	
	public GetTransferProfilesResponseDTO getTransferProfiles(HttpServletRequest request) {
		
		GetTransferProfilesResponseDTO serviceResponse = new GetTransferProfilesResponseDTO();
		List <ProfileDTO> transferProfiles = new ArrayList<ProfileDTO>();
		// Dummy json holding 4 records
		for (int i = 0; i < 5; i++)  {
			ProfileDTO transferProfile = new ProfileDTO();
			transferProfile.setId("Dummy Profile id" + i);
			transferProfile.setName("Dummy Profile Name" +  i);
			transferProfiles.add(transferProfile);
		}
		
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
