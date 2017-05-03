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

/**
 * This is the gateway processors state monitoring and interrupt.
 *
 * @author OFS
 */

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@AppConfigurationResource
@Path("config/mailbox/searchprocessor")
@Api(value = "config/mailbox/searchprocessor", description = "Administration of processor services")
public class ProcessorSearchResource extends AuditedResource {

    private static final Logger LOG = LogManager.getLogger(ProcessorSearchResource.class);

    /**
     * REST service to get all the processors
     *
     * @param request input request
     * @return Response seach response
     */
    @GET
    @ApiOperation(value = "Get All Processors", notes = "get all the processors", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    public Response searchProcessor(@Context HttpServletRequest request,
                                    @QueryParam(value = "page") @ApiParam(name = "page", required = false, value = "page") final String page,
                                    @QueryParam(value = "pagesize") @ApiParam(name = "pagesize", required = false, value = "pagesize") final String pageSize,
                                    @QueryParam(value = "sortField") @ApiParam(name = "sortField", required = false, value = "sortField") final String sortField,
                                    @QueryParam(value = "sortDirection") @ApiParam(name = "sortDirection", required = false, value = "sortDirection") final String sortDirection,
                                    @QueryParam(value = "mbxName") @ApiParam(name = "mbxName", required = false, value = "mbxName") final String mbxName,
                                    @QueryParam(value = "mbxGuid") @ApiParam(name = "mbxGuid", required = false, value = "mbxGuid") final String mbxGuid,
                                    @QueryParam(value = "pipelineId") @ApiParam(name = "pipelineId", required = false, value = "pipelineId") final String pipelineId,
                                    @QueryParam(value = "folderPath") @ApiParam(name = "folderPath", required = false, value = "folderPath") final String folderPath,
                                    @QueryParam(value = "profileName") @ApiParam(name = "profileName", required = false, value = "profileName") final String profileName,
                                    @QueryParam(value = "protocol") @ApiParam(name = "protocol", required = false, value = "protocol") final String protocol,
                                    @QueryParam(value = "prcsrType") @ApiParam(name = "prcsrType", required = false, value = "prcsrType") final String prcsrType,
                                    @QueryParam(value = "prcsrName") @ApiParam(name = "prcsrName", required = false, value = "prcsrName") final String prcsrName,
                                    @QueryParam(value = "prcsrGuid") @ApiParam(name = "prcsrGuid", required = false, value = "prcsrGuid") final String prcsrGuid,
                                    @QueryParam(value = "scriptName") @ApiParam(name = "scriptName", required = false, value = "scriptName") final String scriptName,
                                    @QueryParam(value = "clusterType") @ApiParam(name = "clusterType", required = false, value = "clusterType") final String clusterType,
                                    @QueryParam(value = "matchMode") @ApiParam(name = "matchMode", required = false, value = "matchMode") final String matchMode) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

                ProcessorConfigurationService processor = new ProcessorConfigurationService();
                GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
                searchFilter.setPage(page);
                searchFilter.setPageSize(pageSize);
                searchFilter.setSortField(sortField);
                searchFilter.setSortDirection(sortDirection);
                searchFilter.setMbxName(mbxName);
                searchFilter.setMbxGuid(mbxGuid);
                searchFilter.setPipelineId(pipelineId);
                searchFilter.setFolderPath(folderPath);
                searchFilter.setProfileName(profileName);
                searchFilter.setProtocol(protocol);
                searchFilter.setProcessorType(prcsrType);
                searchFilter.setProcessorName(prcsrName);
                searchFilter.setProcessorGuid(prcsrGuid);
                searchFilter.setScriptName(scriptName);
                searchFilter.setClusterType(clusterType);
                searchFilter.setMatchMode(matchMode);

                // Get all the processors
                return processor.searchProcessor(searchFilter);
            }
        };
        worker.actionLabel = "ProcessorSearchResource.searchProcessor()";
        worker.queryParams.put(AuditedResource.HEADER_GUID, AuditedResource.MULTIPLE);

        // hand the delegate to the framework for calling
        return process(request, worker);
    }

    @Override
    protected AuditStatement getInitialAuditStatement(String actionLabel) {
        return new DefaultAuditStatement(Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5,
                PCIV20Requirement.PCI10_2_2, HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD,
                HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv,
                HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
    }

}
