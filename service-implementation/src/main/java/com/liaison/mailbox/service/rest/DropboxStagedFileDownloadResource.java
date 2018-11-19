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
import com.liaison.commons.message.glass.dom.GatewayType;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.commons.util.UUIDGen;
import com.liaison.commons.util.StringUtil;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.fs2.api.FS2ObjectHeaders;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.gem.util.GEMConstants;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.dropbox.DropboxAuthenticationService;
import com.liaison.mailbox.service.dropbox.DropboxStagedFilesService;
import com.liaison.mailbox.service.dropbox.filter.ConveyorAuthZ;
import com.liaison.mailbox.service.dto.configuration.response.DropBoxUnStagedFileResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.response.DropboxAuthAndGetManifestResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.glass.util.TransactionVisibilityClient;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.TenancyKeyUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This is the gateway for the mailbox processor configuration services.
 *
 * @author santoshc
 */
@AppConfigurationResource
@Path("config/dropbox/stagedFiles/{stagedFileId}")
@Api(value = "config/dropbox/stagedFiles/{stagedFileId}", description = "Gateway for the dropbox services.")
public class DropboxStagedFileDownloadResource extends AuditedResource {
    private static final Logger LOG = LogManager.getLogger(DropboxStagedFileDownloadResource.class);

    @GET
    @ApiOperation(value = "download staged file", notes = "download staged file", position = 1)
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    @ConveyorAuthZ
    public Response downloadStagedFile(@Context final HttpServletRequest serviceRequest,
                                       @PathParam(value = "stagedFileId") @ApiParam(name = "stagedfileid", required = true, value = "staged file id") final String stagedFileId,
                                       @HeaderParam(MailBoxConstants.MANIFEST_DTO) String manifestJson,
                                       @HeaderParam(MailBoxConstants.UM_AUTH_TOKEN) String token) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() throws Exception {
                LOG.debug("Entering into download staged file service");

                DropboxStagedFilesService stagedFileService = new DropboxStagedFilesService();

                TransactionVisibilityClient transactionVisibilityClient = new TransactionVisibilityClient();
                GlassMessage glassMessage = new GlassMessage();
                String loginId = "unknown";

                try {

                    // get login id and auth token from mailbox token
                    loginId = serviceRequest.getHeader(MailBoxConstants.DROPBOX_LOGIN_ID);

                    // getting manifest
                    GEMManifestResponse manifestResponse = new Gson().fromJson(manifestJson, GEMManifestResponse.class);

                    if (StringUtil.isNullOrEmptyAfterTrim(stagedFileId)) {
                        throw new MailBoxServicesException("Staged file id is Mandatory", Response.Status.BAD_REQUEST);
                    }

                    List<String> tenancyKeys = TenancyKeyUtil.getTenancyKeyGuids(manifestResponse.getManifest());
                    if (tenancyKeys.isEmpty()) {
                        throw new MailBoxServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED, Response.Status.BAD_REQUEST);
                    }

                    // validate file id belongs to any user organisation
                    String spectrumUrl = stagedFileService.validateIfFileIdBelongsToAnyOrganisation(stagedFileId, tenancyKeys, glassMessage);
                    if (spectrumUrl == null) {
                        LOG.error(MailBoxUtil.constructMessage(null, null, "Given staged file id - {} does not belong to any user organisation."), stagedFileId);
                        throw new MailBoxServicesException(Messages.STAGE_FILEID_NOT_BELONG_TO_ORGANISATION, Response.Status.BAD_REQUEST);
                    }

                    String processId = UUIDGen.getCustomUUID();
                    glassMessage.setCategory(ProcessorType.DROPBOXPROCESSOR);
                    glassMessage.setProtocol(Protocol.DROPBOXPROCESSOR.getCode());
                    glassMessage.setStatus(ExecutionState.COMPLETED);
                    glassMessage.setOutAgent(GatewayType.REST);
                    glassMessage.setProcessId(processId);
                    glassMessage.setGlobalPId(getStagedFileGlobalProcessId(spectrumUrl));
                    
                    // Log Second Corner Time stamp
                    glassMessage.logSecondCornerTimestamp();

                    // Log time stamp
                    glassMessage.logBeginTimestamp(MailBoxConstants.DROPBOX_FILE_TRANSFER);

                    // Log running status
                    glassMessage.logProcessingStatus(StatusType.RUNNING,
                            MailBoxConstants.DROPBOX_SERVICE_NAME + ": User " + loginId +
                                    " file download", MailBoxConstants.DROPBOXPROCESSOR);

                    // getting the file stream from spectrum for the given file id
                    InputStream payload = StorageUtilities.retrievePayload(spectrumUrl);

                    transactionVisibilityClient.logToGlass(glassMessage);

                    glassMessage.logProcessingStatus(StatusType.SUCCESS,
                            MailBoxConstants.DROPBOX_SERVICE_NAME + ": User " + loginId +
                                    " file download", MailBoxConstants.DROPBOXPROCESSOR);

                    // response message construction
                    ResponseBuilder builder = constructResponse(loginId, token, manifestResponse, payload);

                    // set public signer guid in response header based on gem manifest response
                    if (!MailBoxUtil.isEmpty(manifestResponse.getPublicKeyGroupGuid())) {
                        builder.header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GROUP_GUID, manifestResponse.getPublicKeyGroupGuid());
                    } else if (!MailBoxUtil.isEmpty(manifestResponse.getPublicKeyGuid())) {
                        builder.header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID, manifestResponse.getPublicKeyGuid());
                    }

                    LOG.debug("Exit from download staged file service.");
                    return builder.build();
                } catch (MailBoxServicesException e) {
                    // Log Failed status
                    glassMessage.logProcessingStatus(StatusType.ERROR,
                            MailBoxConstants.DROPBOX_SERVICE_NAME + ": User " + loginId +
                                    " file download", MailBoxConstants.DROPBOXPROCESSOR, ExceptionUtils.getStackTrace(e));
                    throw new LiaisonRuntimeException(e.getMessage(), e);
                } finally {
                    // Log time stamp
                    glassMessage.logEndTimestamp(MailBoxConstants.DROPBOX_FILE_TRANSFER);
                }
            }
        };
        worker.actionLabel = "DropboxStagedFileDownloadResource.downloadStagedFile()";
        //Added the guid
        worker.queryParams.put(AuditedResource.HEADER_GUID, stagedFileId);

        // hand the delegate to the framework for calling
        return process(serviceRequest, worker);
    }

    @DELETE
    @ApiOperation(value = "deactivate specific staged file", notes = "retrieve id of deactivated file", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.DropboxTransferContentResponseDTO.class)
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    @ConveyorAuthZ
    public Response deleteStagedFile(@Context final HttpServletRequest serviceRequest,
                                     @PathParam(value = "stagedFileId") @ApiParam(name = "stagedFileId", required = true, value = "staged file id") final String stagedFileId,
                                     @QueryParam(value = HARD_DELETE) @ApiParam(name = HARD_DELETE, required = false, value = "boolean to hard delete the staged file entity") final String hardDelete,
                                     @HeaderParam(MailBoxConstants.MANIFEST_DTO) String manifestJson,
                                     @HeaderParam(MailBoxConstants.UM_AUTH_TOKEN) String token) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() throws Exception {
                DropboxStagedFilesService stagedFileService = new DropboxStagedFilesService();

                try {

                    // get login id and auth token from mailbox token
                    String loginId = serviceRequest.getHeader(MailBoxConstants.DROPBOX_LOGIN_ID);
                    String authenticationToken = serviceRequest.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN);
                    String aclManifest = serviceRequest.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER);
                    dropboxMandatoryValidation(loginId, authenticationToken, aclManifest);

                    // getting manifest
                    GEMManifestResponse manifestResponse = new Gson().fromJson(manifestJson, GEMManifestResponse.class);

                    DropBoxUnStagedFileResponseDTO dropBoxUnStagedFileResponseDTO =
                            stagedFileService.getDroppedStagedFileResponse(manifestResponse.getManifest(), stagedFileId, Boolean.parseBoolean(hardDelete), loginId);

                    // response message construction
                    String responseBody = MailBoxUtil.marshalToJSON(dropBoxUnStagedFileResponseDTO);
                    ResponseBuilder builder = constructResponse(loginId, token, manifestResponse, responseBody);
                    return builder.build();

                } catch (IOException | JAXBException e) {
                    throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage(), e);
                }
            }
        };
        worker.actionLabel = "DropboxFileTransferResource.deleteStagedFile()";
        //Added the guid
        worker.queryParams.put(AuditedResource.HEADER_GUID, stagedFileId);
        worker.queryParams.put(AuditedResource.HARD_DELETE, hardDelete);

        // hand the delegate to the framework for calling
        return process(serviceRequest, worker);
    }

    @Override
    protected AuditStatement getInitialAuditStatement(String actionLabel) {
        return new DefaultAuditStatement(Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5, PCIV20Requirement.PCI10_2_2, HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD, HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv, HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
    }

    /**
     * Reads the global process ID from the payload's FS2 headers
     *
     * @param spectrumUrl Spectrum URL
     * @return String globalProcessId
     */
    private String getStagedFileGlobalProcessId(String spectrumUrl) {
        FS2ObjectHeaders fs2ObjectHeaders = StorageUtilities.retrievePayloadHeaders(spectrumUrl);
        return fs2ObjectHeaders.getHeaders().get(StorageUtilities.GLOBAL_PROCESS_ID_HEADER).get(0);
    }
}
