/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.rest;

import com.google.gson.Gson;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.dropbox.DropboxAuthenticationService;
import com.liaison.mailbox.service.dropbox.DropboxFileTransferService;
import com.liaison.mailbox.service.dropbox.filter.ConveyorAuthZ;
import com.liaison.mailbox.service.dto.configuration.response.DropboxTransferContentResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.FileTransferMetaDTO;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.response.DropboxAuthAndGetManifestResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.WorkTicketUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * This is the gateway for the mailbox processor configuration services.
 *
 * @author OFS
 */
@AppConfigurationResource
@Path("config/dropbox/transferContent")
@Api(value = "config/dropbox/transferContent", description = "Gateway for the dropbox services.")
public class DropboxFileTransferResource extends AuditedResource {

    private static final Logger LOG = LogManager.getLogger(DropboxFileTransferResource.class);

    @POST
    @ApiOperation(value = "upload content to spectrum", notes = "update details of existing mailbox", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.DropboxTransferContentResponseDTO.class)
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    @ConveyorAuthZ
    public Response uploadContentAsync(@Context final HttpServletRequest serviceRequest,
                                       @QueryParam(value = "transferProfileId") final String transferProfileId,
                                       @HeaderParam(MailBoxConstants.MANIFEST_DTO) String manifestJson,
                                       @HeaderParam(MailBoxConstants.UM_AUTH_TOKEN) String token) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() throws Exception {

                LOG.debug("Entering into uploadContentAsyncToSpectrum service.");

                DropboxAuthAndGetManifestResponseDTO responseEntity = null;
                DropboxAuthAndGetManifestRequestDTO dropboxAuthAndGetManifestRequestDTO = null;
                DropboxAuthenticationService authService = new DropboxAuthenticationService();
                DropboxFileTransferService fileTransferService = new DropboxFileTransferService();
                // to calculate elapsed times of each individual task in this rest call
                long actualStartTime = System.currentTimeMillis();
                long startTime = 0;
                long endTime = 0;

                String loginId = "unknown";

                try {

                    LOG.info(MailBoxUtil.constructMessage(null, null, "file transfer for the transfer profile {} - Start"), transferProfileId);

                    // start time to calculate elapsed time for retrieving necessary details from headers
                    startTime = System.currentTimeMillis();
                    String fileName = null;
                    if (!MailBoxUtil.isEmpty(serviceRequest.getHeader(MailBoxConstants.UPLOAD_FILE_NAME))) {
                        fileName = URLDecoder.decode(serviceRequest.getHeader(MailBoxConstants.UPLOAD_FILE_NAME),
                                StandardCharsets.UTF_8.displayName());
                    }

                    // get login id and auth token from mailbox token
                    loginId = serviceRequest.getHeader(MailBoxConstants.DROPBOX_LOGIN_ID);
                    GEMManifestResponse manifestResponse = new Gson().fromJson(manifestJson, GEMManifestResponse.class);

                    // creating work ticket
                    WorkTicket workTicket = WorkTicketUtil.createWorkTicket(getRequestProperties(serviceRequest, null),
                            getRequestHeaders(serviceRequest),null);

                    // to calculate elapsed time for getting manifest
                    endTime = System.currentTimeMillis();
                    LOG.debug("Calculating elapsed time for creating workticket");
                    MailBoxUtil.calculateElapsedTime(startTime, endTime);

                    // to calculate elapsed time for work ticket creation
                    startTime = System.currentTimeMillis();

                    // creating file transfer helper dto
                    FileTransferMetaDTO fileTransferDTO = new FileTransferMetaDTO();
                    fileTransferDTO.setFileContent(serviceRequest.getInputStream());
                    fileTransferDTO.setTransferProfileId(transferProfileId);
                    fileTransferDTO.setFileName(fileName);
                    fileTransferDTO.setLoginId(loginId);

                    // calling service to upload content to spectrum
                    DropboxTransferContentResponseDTO dropboxContentTransferDTO = fileTransferService.transferFile(
                            workTicket, fileTransferDTO, manifestResponse.getManifest());

                    // to calculate elapsed time for getting manifest
                    endTime = System.currentTimeMillis();
                    LOG.debug("TIME SPENT IN SERVICE LAYER - RETRIEVE PROCESSOR PROPS + POST TO SPECTRUM + BUILD WORK TICKET + POST TO QUEUE");
                    MailBoxUtil.calculateElapsedTime(startTime, endTime);
                    String responseBody = MailBoxUtil.marshalToJSON(dropboxContentTransferDTO);

                    // response message construction
                    ResponseBuilder builder = constructResponse(loginId, token, manifestResponse, responseBody);

                    // to calculate elapsed time for getting manifest
                    endTime = System.currentTimeMillis();
                    LOG.info(MailBoxUtil.constructMessage(null, null, "TOTAL TIME TAKEN TO TRANSFER FILE {} IS {}"), workTicket.getFileName(), endTime
                            - actualStartTime);
                    LOG.info(MailBoxUtil.constructMessage(null, null, "file transfer for the transfer profile {} - End"), transferProfileId);
                    MailBoxUtil.calculateElapsedTime(actualStartTime, endTime);
                    LOG.debug("Exit from uploadContentAsyncToSpectrum service.");

                    return builder.build();
                } catch (IOException | JAXBException e) {
                    throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage(), e);
                }
            }
        };
        worker.actionLabel = "DropboxFileTransferResource.uploadContentAsync()";

        // hand the delegate to the framework for calling
        return process(serviceRequest, worker);
    }

    @Override
    protected AuditStatement getInitialAuditStatement(String actionLabel) {
        return new DefaultAuditStatement(Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5,
                PCIV20Requirement.PCI10_2_2, HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD,
                HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv,
                HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
    }

}
